package org.motorbrot.renderfarm.impl;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.AbstractFOPTranscoder;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.motorbrot.renderfarm.api.PdfRenderService;
import org.motorbrot.renderfarm.api.RenderModel;
import org.motorbrot.renderfarm.api.RenderModel.PageModel;
import org.motorbrot.renderfarm.api.RenderRuntimeException;

/**
 * this is supposed to run as OSGi-Service and locally via new
 */
@Component(immediate = true, service = PdfRenderService.class)
public class RenderFarm implements PdfRenderService {

  /**
   * only to avoid multi-threading issues
   */
  static final ICC_Profile ICC_PROFILE_FIELD_FOR_THREAD_ISSUES = ICC_Profile.getInstance(ColorSpace.CS_sRGB);
  
  private static volatile Configuration transcoderFontConfig;

  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RenderFarm.class);


  /**
   * new RenderFarm
   */
  public RenderFarm() {

    @SuppressWarnings("unused")
    boolean isDefaultsRGB = ColorProfileUtil.isDefaultsRGB(ICC_PROFILE_FIELD_FOR_THREAD_ISSUES);
    /*
     * avoids the following when multithreaded: SEVERE: Error rendering 3gunMetal_pdfCfg.json null java.lang.NullPointerException at
     * java.awt.color.ICC_Profile.intFromBigEndian(ICC_Profile.java:1782) at java.awt.color.ICC_Profile.getProfileClass(ICC_Profile.java:1176) at
     * org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil.isDefaultsRGB(ColorProfileUtil.java:73) at
     * org.apache.fop.render.pdf.AbstractImageAdapter.setupColorProfile(AbstractImageAdapter.java:156) at
     * org.apache.fop.render.pdf.AbstractImageAdapter.setup(AbstractImageAdapter.java:100) at
     * org.apache.fop.render.pdf.ImageRenderedAdapter.setup(ImageRenderedAdapter.java:120) at org.apache.fop.pdf.PDFDocument.addImage(PDFDocument.java:872) at
     * org.apache.fop.svg.PDFGraphics2D.addRenderedImage(PDFGraphics2D.java:1208) at
     * org.apache.fop.svg.PDFGraphics2D.drawInnerRenderedImage(PDFGraphics2D.java:1179) at
     * org.apache.fop.svg.PDFGraphics2D.drawRenderedImage(PDFGraphics2D.java:1167) at
     * org.apache.xmlgraphics.image.loader.impl.ImageConverterBitmap2G2D$Graphics2DImagePainterImpl.paint(ImageConverterBitmap2G2D.java:70) at
     * org.apache.fop.svg.AbstractFOPImageElementBridge$Graphics2DNode.primitivePaint(AbstractFOPImageElementBridge.java:278) at
     * org.apache.batik.gvt.AbstractGraphicsNode.paint(Unknown Source) at org.apache.batik.gvt.CompositeGraphicsNode.primitivePaint(Unknown Source) at
     * org.apache.batik.gvt.AbstractGraphicsNode.paint(Unknown Source) at org.apache.batik.gvt.ImageNode.paint(Unknown Source) at
     * org.apache.batik.gvt.CompositeGraphicsNode.primitivePaint(Unknown Source) at org.apache.batik.gvt.CanvasGraphicsNode.primitivePaint(Unknown Source) at
     * org.apache.batik.gvt.AbstractGraphicsNode.paint(Unknown Source) at org.apache.batik.gvt.CompositeGraphicsNode.primitivePaint(Unknown Source) at
     * org.apache.batik.gvt.AbstractGraphicsNode.paint(Unknown Source) at org.apache.fop.svg.PDFTranscoder.transcode(PDFTranscoder.java:185) at
     * org.apache.batik.transcoder.XMLAbstractTranscoder.transcode(Unknown Source) at org.apache.batik.transcoder.SVGAbstractTranscoder.transcode(Unknown
     * Source) at org.motorbrot.renderfarm.impl.RenderFarm.transcodeDomToPdfPage(RenderFarm.java:145) at
     * java.util.stream.ReferencePipeline$Head.forEachOrdered(ReferencePipeline.java:590) at
     *
     * maybe try to raise fop bug because this seems quite reproduceable
     */
  }

  private BundleContext context;

  @Activate
  private void activate(BundleContext bundleContext) {
    context = bundleContext;
  }

  /**
   * @param templatesAndData model
   * @return binary pdf
   */
  @Override
  public InputStream renderPdf(RenderModel templatesAndData) {

    List<? extends RenderModel.PageModel> svgPages = templatesAndData.getPages();
    List<InputStream> singlePagePdfInputStreams = new ArrayList<>(svgPages.size());

    String xmlParser = org.apache.xerces.parsers.SAXParser.class.getName();  // use the one from jdk
    SAXSVGDocumentFactory mySaxFactory = new SAXSVGDocumentFactory(xmlParser);

    final Transcoder fopTranscoder = buildFopTranscoder(); // not Threadsafe this one

    svgPages.stream().forEachOrdered(svgPage -> {

      InputStream staticPdfPageStream = svgPage.getStaticPdfPage();
      if (staticPdfPageStream != null) {
        // just add a non-dynamic page
        singlePagePdfInputStreams.add(staticPdfPageStream);
      }
      else {

        InputStream svgInStream = svgPage.getSvgStream();
        
        try {
          // Read the input SVG into DOM
          Document dom = mySaxFactory.createDocument(null, svgInStream);

          // change text and image
          manipulateDom(dom, svgPage);

          // write pdfpage to RAM
          ByteArrayOutputStream pdfPageByteOut = transcodeDomToPdfPage(dom, fopTranscoder);

          // make inputStream from RAM
          byte[] pdfBytes = pdfPageByteOut.toByteArray();
          ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(pdfBytes);
          singlePagePdfInputStreams.add(byteArrayInputStream);
        }
        catch (IOException ex) {
          throw new RenderRuntimeException("Failed reading data for Pdf-Rendering of page " + svgPage.getTemplate(), ex);
        }
        catch (TranscoderException ex) {
          throw new RenderRuntimeException("Failed transcoding Pdf from " + svgPage.getTemplate(), ex);
        }
      }

    });

    // merge all pages into one pdf
    PDFMergerUtility pdfMerger = new PDFMergerUtility();

    PDDocumentInformation pdDocumentInformation = new PDDocumentInformation();
    pdDocumentInformation.setAuthor("org.motorbrot.renderfarm");
    pdDocumentInformation.setCreationDate(Calendar.getInstance());
    pdDocumentInformation.setModificationDate(Calendar.getInstance());
//    pdDocumentInformation.setTitle("Your Pdf");   toDo: make configurable
    pdfMerger.setDestinationDocumentInformation(pdDocumentInformation);


    // and save to RAM
    ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
    try {
      pdfMerger.setDestinationStream(bos);
      pdfMerger.addSources(singlePagePdfInputStreams);
      pdfMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
      // The memory settings depend on if you want to use RAM or Temp files.
    }
    catch (IOException ex) {
      throw new RenderRuntimeException("Failed merging singlepage Pdfs.", ex);
    }
    finally {
      singlePagePdfInputStreams.stream().forEach(inStream -> {
        IOUtils.closeQuietly(inStream);
      });
    }

    // create InputStream from RAM
    byte[] pdfBytes = bos.toByteArray();
    ByteArrayInputStream pdfBytesInputStream = new ByteArrayInputStream(pdfBytes);

    return pdfBytesInputStream;
  }

  protected void manipulateDom(Document dom, PageModel pageModel) {

    List<String> idsDeleted = new ArrayList<>(); // don't throw exception if there is dynatext/image in model within an element deleted
    pageModel.getIdsOfElementsToDelete().stream().forEach(id -> {
      Element elementById = dom.getElementById(id);
      if (elementById != null) {
        
        NodeList nodeList = elementById.getElementsByTagName("*");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)node;
                String idAttrib = e.getAttribute("id");
                if (idAttrib != null && !"".equals(idAttrib)) {
                  idsDeleted.add(idAttrib);
                }
            }
        }
        elementById.getParentNode().removeChild(elementById);
      }
      else {
        throw new RenderRuntimeException("elementById:" + id + " could not be found in Document for deletion");
      }
    });
    
    pageModel.getDynamicTexts().stream().forEach(dynaTxt -> {
      Element elementById = dom.getElementById(dynaTxt.getElementId());
      if (elementById != null) {
        elementById.setTextContent(dynaTxt.getTextValue());
      }
      else {
        if (!idsDeleted.contains(dynaTxt.getElementId())) {
          throw new RenderRuntimeException("elementById:" + dynaTxt.getElementId() + " could not be found in Document for text replacement");
        }
      }
    });
    
    
    pageModel.getDynamicFlowtexts().stream().forEach(dynaFlowTxt -> {
      Element flowParaElement = dom.getElementById(dynaFlowTxt.getElementId());
      if (flowParaElement != null) {
        
        String[] flowParaTexts = dynaFlowTxt.getFlowParas();
        if (flowParaTexts != null && flowParaTexts.length > 0) {
          flowParaElement.setTextContent(flowParaTexts[0]);
          Node parentNode = flowParaElement.getParentNode();

          // copy the single flowPara to create several Paragraphs within same flowRoot
          for (int i = 1; i < flowParaTexts.length; i++) {
            Element cloneNode = (Element)flowParaElement.cloneNode(true);
            cloneNode.setTextContent(flowParaTexts[i]);
            cloneNode.setAttribute("id", dynaFlowTxt.getElementId() + "_" + i);
            parentNode.appendChild(cloneNode);
          }
        }
        
      }
      else {
        if (!idsDeleted.contains(dynaFlowTxt.getElementId())) {
          throw new RenderRuntimeException("elementById:" + dynaFlowTxt.getElementId() + " could not be found in Document for text replacement");
        }
      }
    });

    pageModel.getDynamicJpegs().stream().forEach(dynaImage -> {
      Element elementById = dom.getElementById(dynaImage.getElementId());
      if (elementById != null) {
        try {
          String base64fromIo = getBase64fromIo(dynaImage.getImageBinary());
          elementById.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "data:image/*;base64," + base64fromIo);
        }
        catch (IOException ex) {
          throw new RenderRuntimeException("Error reading image binary for " + dynaImage.getElementId(), ex);
        }
      }
      else {
        if (!idsDeleted.contains(dynaImage.getElementId())) {
          throw new RenderRuntimeException("elementById:" + dynaImage.getElementId() + " could not be found in Document for Image replacement");
        }
      }
    });
    
  }

  private ByteArrayOutputStream transcodeDomToPdfPage(Document dom, Transcoder fopTranscoder) throws TranscoderException {
    // Read the input SVG dom into Transcoder Input
    TranscoderInput inputSvgImage = new TranscoderInput(dom);

    // use ByteArray as output
    ByteArrayOutputStream pdfOutStream = new ByteArrayOutputStream();
    TranscoderOutput transcoderOut = new TranscoderOutput(pdfOutStream);

    fopTranscoder.transcode(inputSvgImage, transcoderOut);
    return pdfOutStream;
  }

  private Transcoder buildFopTranscoder() {
    // Write output PDF
    Transcoder transcoder = new PDFTranscoder(); // not ThreadSafe

    // custom fonts via config file with relative paths
    try {
      Configuration transcoderConfig = getFontConfig();
      if (transcoderConfig != null) {
        ContainerUtil.configure(transcoder, transcoderConfig);
      }
    }
    catch (ConfigurationException ex) {
      LOG.warn("Error in font-configuration", ex);
    }
    
//    transcoder.addTranscodingHint(AbstractFOPTranscoder.KEY_AUTO_FONTS, Boolean.FALSE);
    return transcoder;
  }

  private Configuration getFontConfig() {
    Configuration transcoderConfig = RenderFarm.transcoderFontConfig;
    if (transcoderConfig == null) {
      synchronized (Configuration.class) {
        transcoderConfig = RenderFarm.transcoderFontConfig;
        if (transcoderConfig == null) {
          transcoderConfig = buildFontConfig();
          RenderFarm.transcoderFontConfig = transcoderConfig;
        }
      }
    }
    return transcoderConfig;
  }
          
  private Configuration buildFontConfig() {
    Configuration newTranscoderConfig = null;
    InputStream cfgInstream = null;
    if (context != null) {
      // handle font-config in OSGI environment
      String slingHomeEnv = context.getProperty("sling.home");
      if (slingHomeEnv != null) {
        // check if path (relative to current run-dir) to font exists
        try {
          if (new File("./fonts").isDirectory()) {
            URL url = context.getBundle().getResource("pdf-renderer-cfg.xml");
            if (url != null) {
              cfgInstream = url.openStream();
            }
          }
          // check if path (relative to sling.home) to font exists
          else if (new File(slingHomeEnv + "/fonts").isDirectory()) {
            URL url = context.getBundle().getResource("pdf-renderer-cfg-slinghome.xml");
            if (url != null) {
              String cfg = new String(IOUtils.toByteArray(url.openStream()), StandardCharsets.UTF_8);
              String envReplaced = cfg.replaceAll("#SLING_HOME#", slingHomeEnv);
              cfgInstream = new ByteArrayInputStream(envReplaced.getBytes(StandardCharsets.UTF_8));
            }
          }
        } catch (IOException ex) {
          LOG.error("Error reading steam from pdf-renderer-cfg*.xml", ex);
        }
      }
    }
    else {
      try {
        // relative directory for commandline tool
        cfgInstream = new FileInputStream("./fonts/pdf-renderer-cfg.xml");
      }
      catch (FileNotFoundException ex) {
        LOG.error("Error reading ./fonts/pdf-renderer-cfg.xml", ex);
      }
    }
    if (cfgInstream != null) {
      DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
      try {
        newTranscoderConfig = cfgBuilder.build(cfgInstream);
      }
      catch (ConfigurationException | SAXException | IOException ex) {
        LOG.error("Error in font-configuration", ex);
      }
      IOUtils.closeQuietly(cfgInstream);
    }
    else {
      LOG.error("Cannot find pdf-renderer-cfg.xml");
    }
    return newTranscoderConfig;
  }

  private static String getBase64fromIo(InputStream is) throws IOException {
    String imgBytes = null;
    try {
      byte[] bytes = IOUtils.toByteArray(is);
      imgBytes = Base64.getEncoder().encodeToString(bytes);
    }
    finally {
      IOUtils.closeQuietly(is);
    }
    return imgBytes;
  }

}
