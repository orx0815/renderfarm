
package org.motorbrot.renderfarm.misc.cli.gsonmodel;

import org.motorbrot.renderfarm.api.RenderModel.DynaFlowtext;
import org.motorbrot.renderfarm.api.RenderModel.DynaImage;
import org.motorbrot.renderfarm.api.RenderModel.DynaText;
import org.motorbrot.renderfarm.api.RenderModel.PageModel;
import org.motorbrot.renderfarm.tools.InkscapeToBatikPreprocessor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * hold template and dynamic text and images for a single page
 */
public class Page implements PageModel {

  // fields set by gson from json
  private String template;
  private String staticPdfpage;
  private final List<DynamicText> dynamicTexts = new ArrayList<>();
  private final  List<DynamicFlowtext> dynamicFlowtexts = new ArrayList<>();
  private final  List<DynamicJpeg> dynamicJpegs = new ArrayList<>();
  private final  List<String> idsOfElementsToDelete = new ArrayList<>();
  
  @Override
  public InputStream getSvgStream() {
    InputStream is = null;
    try {

      File xmlFile = new File(template);
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document inkscapeSvgDom = dBuilder.parse(xmlFile);
      
      boolean isChanged = InkscapeToBatikPreprocessor.transformForBatik(inkscapeSvgDom);
      
      if (isChanged) {
        File batikXmlFile = new File(xmlFile.getParentFile(), "_4batik_" + xmlFile.getName());
        // convert only once if file is older then 10sec
        synchronized (Page.class) {
          if (!batikXmlFile.exists() || (System.currentTimeMillis() - batikXmlFile.lastModified() > 10000)) {

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(batikXmlFile), StandardCharsets.UTF_8);
            String serializedSvg = InkscapeToBatikPreprocessor.prettyPrint(inkscapeSvgDom);
            outputStreamWriter.write(serializedSvg);
            outputStreamWriter.close();
          }
          is = new FileInputStream(batikXmlFile);
        }
      }
      else {
        is = new FileInputStream(xmlFile);
      }
    }
    catch (IOException | SAXException | ParserConfigurationException ex) {
      Logger.getLogger(DynamicJpeg.class.getName()).log(Level.SEVERE, null, ex);
    }
    return is;
  } 

  @Override
  public InputStream getStaticPdfPage() {
    FileInputStream fis = null;
    if (staticPdfpage != null) {
      try {
        fis = new FileInputStream(staticPdfpage);
      }
      catch (FileNotFoundException ex) {
        Logger.getLogger(DynamicJpeg.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    return fis;
  }
  
  @Override
  public List<? extends DynaText> getDynamicTexts() {
    return dynamicTexts;
  }
  
  @Override
  public List<? extends DynaFlowtext> getDynamicFlowtexts() {
    return dynamicFlowtexts;
  }

  @Override
  public List<? extends DynaImage> getDynamicJpegs() {
    return dynamicJpegs;
  }

  @Override
  public String getTemplate() {
    return template;
  }

  @Override
  public List<String> getIdsOfElementsToDelete() {
    return idsOfElementsToDelete;
  }

}
