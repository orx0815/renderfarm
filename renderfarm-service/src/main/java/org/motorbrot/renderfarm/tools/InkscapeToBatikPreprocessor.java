package org.motorbrot.renderfarm.tools;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.motorbrot.renderfarm.impl.RenderFarm;

/**
 * Inkscape and batik have developed flowtext-support on different svg-1.2 W3C Working Draft
 * As there is not final w3c-spec, both won't change their implementation yet. 10 years have passed 
 * https://www.w3.org/TR/SVG12/
 */
public final class InkscapeToBatikPreprocessor {

  // this path style property confuses batik
  private static final String INKSCAPE_WEIRD_FONTSPECIFICATION = "-inkscape-font-specification:'";
  
  private InkscapeToBatikPreprocessor() {
    // Utility-class, no instance
  }
  
  /**
   * transforms flowRoot-elements coming from Inkscape into a form that batik needs
   * @param dom XML-DOM Object to transform
   * @return true if changes were made 
   * @throws IllegalArgumentException
   * @throws TransformerFactoryConfigurationError
   * @throws DOMException 
   */
  public static boolean transformForBatik(Document dom) throws IllegalArgumentException, TransformerFactoryConfigurationError, DOMException {
    
    boolean hasChangeOccurred = false;
    
    // check, if inkscape has made a svg-1.2 (w3c-spec still in draft) flowText
    NodeList flowRoots = dom.getElementsByTagName("flowRoot");
    if (flowRoots.getLength() > 0) {
      // svg-version 1.1 won't even parse the dom with FlowText
      dom.getDocumentElement().setAttribute("version", "1.2");

      for (int i = 0; i < flowRoots.getLength(); i++) {
        Element flowRoot = (Element)flowRoots.item(i);

        NodeList flowRegions = flowRoot.getElementsByTagName("flowRegion");
        for (int j = 0; j < flowRegions.getLength(); j++) {
          Element flowRegion = (Element)flowRegions.item(j);

          // correct inkscape-bug with background of rect element
          // http://stackoverflow.com/a/7824631
          NodeList rects = flowRegion.getElementsByTagName("rect");
          for (int k = 0; k < rects.getLength(); k++) {
            Element rect = (Element)rects.item(k);
            String styleAtt = rect.getAttribute("style");
            rect.setAttribute("style", "fill:none;" + styleAtt);
          }

        }

        // wrap all flowParams into flowDiv for batik
        // https://mail-archives.apache.org/mod_mbox/xmlgraphics-batik-users/200704.mbox/%3C461C1DEB.7040401@gmx.de%3E
        NodeList flowParas = flowRoot.getElementsByTagName("flowPara");
        if (flowParas.getLength() > 0) {
          Element flowDiv = dom.createElement("flowDiv");
          List<Node> nodesToRemove = new ArrayList<>();
          
          for (int j = 0; j < flowParas.getLength(); j++) {
            Node flowPara = flowParas.item(j);
            if (flowPara.getParentNode().getNodeName().equals("flowRoot")) {
              Node cloneNode = flowPara.cloneNode(true);
              nodesToRemove.add(flowPara);
              flowDiv.appendChild(cloneNode);
            }
          }
          for (Node nodeToDelete : nodesToRemove) {
            flowRoot.removeChild(nodeToDelete);
          }
          flowRoot.appendChild(flowDiv);
        }
        changeTextAlignStyle(flowRoot);
      }
      
      NodeList textElements = dom.getElementsByTagName("text");
      if (textElements != null && textElements.getLength() > 0) {
        for (int i = 0; i < textElements.getLength(); i++) {
          Element textElement = (Element)textElements.item(i);
          changeTextAlignStyle(textElement);
        }
      }
      
      hasChangeOccurred = true;
    }
    
    // remove weird fontSpec
    NodeList paths = dom.getElementsByTagName("path");
    for (int i = 0; i < paths.getLength(); i++) {
      Element path = (Element) paths.item(i);
      String style = path.getAttribute("style");
      int indexOfWeirdFontSpecStart = style.indexOf(INKSCAPE_WEIRD_FONTSPECIFICATION);
      if (indexOfWeirdFontSpecStart >=0) {
        
        String goodStart = style.substring(0, indexOfWeirdFontSpecStart);
        int indexOfWeirdFontSpecEnd = style.indexOf("';", indexOfWeirdFontSpecStart)+2;
        String goodEnd = style.substring(indexOfWeirdFontSpecEnd, style.length());
        path.setAttribute("style", goodStart + goodEnd);
        hasChangeOccurred = true;
      }
    }
    
    
    
    return hasChangeOccurred;
  }

  private static void changeTextAlignStyle(Element textElement) throws DOMException {
    // replace Inkscape's text-align "left|center|right" to batik's "start|middle|end"
    // http://osdir.com/ml/batik-users-xmlgraphics.apache.org/2010-01/msg00032.html
    String styleStr = textElement.getAttribute("style");
    styleStr = styleStr.replaceAll("text-align:left;", "text-align:start;");
    styleStr = styleStr.replaceAll("text-align:center;", "text-align:middle;");
    styleStr = styleStr.replaceAll("text-align:right;", "text-align:end;");
    textElement.setAttribute("style", styleStr);
  }

  /**
   * creates xml from DOM
   * @param dom
   * @return xml
   * @throws IllegalArgumentException
   * @throws TransformerFactoryConfigurationError
   */
   // toDo: move transformer
  public static String prettyPrint(Document dom) throws IllegalArgumentException, TransformerFactoryConfigurationError {
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      StreamResult result = new StreamResult(new StringWriter());
      DOMSource source = new DOMSource(dom);
      transformer.transform(source, result);
      String xmlString = result.getWriter().toString();
      return xmlString;
    }
    catch (TransformerException ex) {
      Logger.getLogger(RenderFarm.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

}
