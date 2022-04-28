//$URL$
//$Id$
package io.wcm.renderfarm.example.sling;

import io.wcm.renderfarm.example.model.ClasspathRenderModelImpl;
import io.wcm.renderfarm.api.PdfRenderService;
import io.wcm.renderfarm.api.RenderModel;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SlingServlet serving renderfarm pdf
 */
@SlingServlet(
        label = "Samples - Sling All Methods Servlet",
        description = "Sample implementation of a Sling All Methods Servlet.",
        paths = { "/services/renderfarm" }
)
public class PdfServlet extends SlingSafeMethodsServlet {
  
  @Reference
  private PdfRenderService pdfRenderService;

  private static final Logger mLog = LoggerFactory.getLogger(PdfServlet.class);
  

  
  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    RenderModel renderModel = new ClasspathRenderModelImpl();
    
    InputStream pdfIn = pdfRenderService.renderPdf(renderModel);
    
    response.setContentType("application/pdf");
    ServletOutputStream out = response.getOutputStream();
    byte[] buf = new byte[1000];
    for (int nChunk = pdfIn.read(buf); nChunk != -1; nChunk = pdfIn.read(buf)) {
        out.write(buf, 0, nChunk);
    }
    out.flush();
    IOUtils.closeQuietly(pdfIn);
    IOUtils.closeQuietly(out);
  }


}
