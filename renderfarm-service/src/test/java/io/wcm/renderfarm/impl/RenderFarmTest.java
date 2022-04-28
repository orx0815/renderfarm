package io.wcm.renderfarm.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import io.wcm.renderfarm.api.test.RenderModelTestImpl;
import io.wcm.renderfarm.api.test.RenderModelTestImpl.*;
import io.wcm.renderfarm.tools.InkscapeToBatikPreprocessor;

/**
 * generates a pdf page and checks for text and images in it
 */
public class RenderFarmTest {

  private static final String UNIT_TEST_PLAYGROUND_TEXT = "UnitTest Playground text";

  /**
   * Test of renderPdf method, of class RenderFarm.
   *
   * @throws java.io.IOException
   */
  @Test
  public void testRenderPdf() throws Exception {

    RenderModelTestImpl templatesAndData = buildRenderModel();

    RenderFarm instance = new RenderFarm();
    try {
      InputStream result = instance.renderPdf(templatesAndData);

      PDDocument pdf = PDDocument.load(result);

      checkTextContent(pdf);

      checkImageCount(pdf);

      // uncomment in case you want to look at pdf with human eye
      File file = new File("./target/test-classes/RenderFarmTest.pdf");
      pdf.save(file);
      System.out.println("Test pdf saved to " + file.getAbsolutePath());

      return;
    }
    catch (AssertionError ae) {
      ae.printStackTrace();
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
    fail();
  }

  private void checkImageCount(PDDocument pdf) throws IOException {
    PDPage renderedPage = pdf.getDocumentCatalog().getPages().get(0);
    PDResources pdResources = renderedPage.getResources();

    int imageCounter = 0;
    for (COSName c : pdResources.getXObjectNames()) {
      PDXObject o = pdResources.getXObject(c);
      if (o instanceof org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject) {
        imageCounter++;
      }
    }
    assertTrue("expected one images in Pdf page", imageCounter == 1);
  }

  private void checkTextContent(PDDocument pdf) throws IOException {
    PDFTextStripper textStripper = new PDFTextStripper();
    String extractedText = textStripper.getText(pdf);
    assertTrue(extractedText.contains(UNIT_TEST_PLAYGROUND_TEXT));
  }

  private RenderModelTestImpl buildRenderModel() throws IOException, ParserConfigurationException, SAXException {

    RenderModelTestImpl templatesAndData = new RenderModelTestImpl();
    PageModelImpl pageModel = new PageModelImpl();

    String template = "/svgTemplates/unittestTemplate.svg";

    ByteArrayInputStream byteInputStream = preprocessTemplate(template);

    pageModel.setSvgInputStream(byteInputStream);

    // one text
    RenderModelTestImpl.DynaTextImpl text = new DynaTextImpl();
    text.setElementId("testline");
    text.setTextValue(UNIT_TEST_PLAYGROUND_TEXT);
    pageModel.addDynamicText(text);

    // one images
    DynaImageImpl paintTileOne = new DynaImageImpl();
    paintTileOne.setElementId("bg_image");
    paintTileOne.setImageBinary(getClass().getResource("/staticImgs/light-toned-deposit-in-the-aureum-chaos-region-on-mars.jpg").openStream());
    pageModel.addDynamicJpeg(paintTileOne);

    // on one page
    templatesAndData.addPage(pageModel);
    return templatesAndData;
  }

  // only preprocessed templates should be deployed
  private ByteArrayInputStream preprocessTemplate(String template) throws IOException, SAXException, DOMException, ParserConfigurationException, IllegalArgumentException, TransformerFactoryConfigurationError {
    InputStream inkscapeSvgInStream = getClass().getResource(template).openStream();

    // Transform Inkscape-FlowText to batik
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document inkscapeSvgDom = dBuilder.parse(inkscapeSvgInStream);
    InkscapeToBatikPreprocessor.transformForBatik(inkscapeSvgDom);

    // format nicely and build Instream for engine
    String prettyPrint = InkscapeToBatikPreprocessor.prettyPrint(inkscapeSvgDom);
    ByteArrayInputStream byteInputStream = new ByteArrayInputStream(prettyPrint.getBytes(StandardCharsets.UTF_8));
    return byteInputStream;
  }

}
