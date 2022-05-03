package org.motorbrot.renderfarm.api;

import java.io.InputStream;
import java.util.List;

/**
 * Interface with all templates and data sent to RenderEngine
 */
public interface RenderModel {

  /**
   * @return filename of resulting Pdf
   */
  String getOutputFileName();

  /**
   * @return model for each page
   */
  List<? extends PageModel> getPages();

  /**
   * Model for single page
   */
  interface PageModel {

    List<? extends DynaImage> getDynamicJpegs();

    List<? extends DynaText> getDynamicTexts();
    
    List<? extends DynaFlowtext> getDynamicFlowtexts();
    
    List<String> getIdsOfElementsToDelete();

    InputStream getSvgStream();
    
    /**
     * @return static page already in pdf-format
     */
    InputStream getStaticPdfPage();
    
    /**
     * Used only for debugging purposes, to be able to see in which svg a TranscoderException occurred.
     * @return name/filepath for template 
     */
    String getTemplate();

  }

  /**
   * Model for dynamic text replacements
   */
  interface DynaText {

    /** @return xml id of element to change in svg */
    String getElementId();

    String getTextValue();

  }
  
  /**
   * Model for flowtext (svg 1.2) with paragraphs. 
   */
  interface DynaFlowtext {

    /** @return xml-id of the flowPara element (inside svg1.2 flowRoot element). Will get duplicated for more paragraphs*/
    String getElementId();

    /** @return the paragraphs. A linebreak inside a flowtext is done by putting the newline into new String in array */
    String[] getFlowParas();

  }

  /**
   * Model for dynamic image replacements
   */
  interface DynaImage {

    /** @return xml id of element in svg to replace image*/
    String getElementId();

    InputStream getImageBinary();

  }

}
