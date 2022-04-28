package io.wcm.renderfarm.example.model;

import io.wcm.renderfarm.api.RenderModel;
import io.wcm.renderfarm.api.RenderModel.*;
import io.wcm.renderfarm.example.util.SvgDomUtils;
import io.wcm.renderfarm.tools.InkscapeToBatikPreprocessor;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Rendermodel for first page with random image
 */
class PageOneModel implements RenderModel.PageModel {
  
  private static final String SHOWCASE_INKSCAPE_SVG = "showcase/inkscape.svg";

  /**
   * We do little dynamic scripting here and manipulate the svg before. 
   * Svg only shows litle part of the background image, we randomly choose the part
   * @return 
   */
  @Override
  public InputStream getSvgStream() {
    
    InputStream instreamFromBundleRes = SvgDomUtils.getInstreamFromBundleRes(SHOWCASE_INKSCAPE_SVG);
    
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document svgDom = dBuilder.parse(instreamFromBundleRes);
      
      Element bgImageElement = SvgDomUtils.getElementByTagAndId(svgDom.getDocumentElement(), "image", "background_image");
      
      Double randomX = ThreadLocalRandom.current().nextDouble(-2926.5623, 0);
      Double randomY = ThreadLocalRandom.current().nextDouble(-1761.019, 308.26773);
      
      bgImageElement.setAttribute("x", randomX.toString());
      bgImageElement.setAttribute("y", randomY.toString());
      
      InkscapeToBatikPreprocessor.transformForBatik(svgDom);
      
      InputStream instreamFromDom = SvgDomUtils.domToInstream(svgDom);
      return instreamFromDom;
    }
    catch (ParserConfigurationException | SAXException | IOException | TransformerException ex) {
      Logger.getLogger(PageOneModel.class.getName()).log(Level.SEVERE, "Faild scripting svg", ex);
    }
    return null;
  }
  
  @Override
  public List<? extends DynaImage> getDynamicJpegs() {
    ArrayList<DynaImage> imgs = new ArrayList<>();
    imgs.add(new DynaImage() {
      @Override
      public String getElementId() {
        return "background_image";
      }

      @Override
      public InputStream getImageBinary() {
        return SvgDomUtils.getInstreamFromBundleRes("showcase/bitmaps/Sahara Desert From the Space Station EarthKAM.jpg");
      }
    });
    return imgs;
  }

  @Override
  public List<? extends DynaText> getDynamicTexts() {
    ArrayList<DynaText> texts = new ArrayList<>();
    texts.add(new DynaText() {
      @Override
      public String getElementId() {
        return "halloWelt";
      }

      @Override
      public String getTextValue() {
        return "Hello osgi-world. " + new Date().toString();
      }
    });
    return texts;
  }

  @Override
  public List<? extends DynaFlowtext> getDynamicFlowtexts() {
    return new ArrayList<DynaFlowtext>();
  }

  @Override
  public List<String> getIdsOfElementsToDelete() {
    return new ArrayList<String>();
  }

  @Override
  public InputStream getStaticPdfPage() {
    return null;
  }

  @Override
  public String getTemplate() {
    return SHOWCASE_INKSCAPE_SVG;
  }
  
}
