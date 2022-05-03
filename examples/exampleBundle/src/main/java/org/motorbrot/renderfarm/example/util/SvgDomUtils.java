package org.motorbrot.renderfarm.example.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.osgi.framework.FrameworkUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Utils for svg/xml handling
 */
public final class SvgDomUtils {

  // static helpers only
  private SvgDomUtils() {
  }
  
  /**
   * Creates a ByteArrayInputStream from xml
   * @param svgDom to serialize 
   * @return a ByteArrayInputStream 
   * @throws TransformerFactoryConfigurationError when things are wrong in TransformerFactory
   * @throws TransformerException when things go wrong transforming
   */
  public static InputStream domToInstream(Document svgDom) throws TransformerFactoryConfigurationError, TransformerException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Source xmlSource = new DOMSource(svgDom);
    Result outputTarget = new StreamResult(outputStream);
    TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
    InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
    return is;
  }
  
  /**
   *  svgDom.getElementById only works with batik-dom, doesn't work with xerces here.
   *  (From JavaDoc) Note: Attributes with the name "ID" or "id" are not of type ID unless so defined.
   * @param svgDom Element to search in
   * @param tagname of element to find
   * @param elementId value of id-attribute of element to find
   * @return Element found, or null
   */
  public static Element getElementByTagAndId(Element svgDom, String tagname, String elementId) {
    Element elementFound = null;
    NodeList elementsByTagName = svgDom.getElementsByTagName(tagname);
    for (int i = 0; i < elementsByTagName.getLength(); i++) {
      Element para = (Element)elementsByTagName.item(i);
      String id = para.getAttribute("id");
      if (elementId.equals(id)) {
        elementFound = para;
        break;
      }
    }
    return elementFound;
  }
  
  /**
   * get Resource bundled with application as InputStream 
   * @param resourceName The name of the resource.
   * @return InputStream from osgi-bundle resource, or null
   */
  public static InputStream getInstreamFromBundleRes(String resourceName) {
    URL resUrl = FrameworkUtil.getBundle(SvgDomUtils.class).getResource(resourceName);
    if (resUrl != null) {
      try {
        return resUrl.openStream();
      }
      catch (IOException ex) {
        Logger.getLogger(SvgDomUtils.class.getName()).log(Level.SEVERE, "Failed to load " + resourceName, ex);
      }
    }
    Logger.getLogger(SvgDomUtils.class.getName()).log(Level.SEVERE, "Could not load " + resourceName);
    return null;
  }
  
}
