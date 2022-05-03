package org.motorbrot.renderfarm.api;

import java.io.InputStream;

/**
 * Generates binary Pdf from Svg-Templates and Texts and Images
 */
public interface PdfRenderService {

  /**
   * @param templatesAndData model
   * @return binary Pdf
   */
  InputStream renderPdf(RenderModel templatesAndData);
  
}
