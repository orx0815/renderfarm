
package io.wcm.renderfarm.misc.cli;

import com.google.gson.Gson;
import io.wcm.renderfarm.impl.RenderFarm;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.pdfbox.io.IOUtils;
import io.wcm.renderfarm.misc.cli.gsonmodel.FilebasedRenderModel;

/**
 * Commandline Interface to run PdfRenderService locally
 */
public final class Cli {

  private Cli() {
  }

  /**
   * takes all *_pdfCfg.json from current directory and renders pdfs
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {

    File dir = new File(".");
    File[] files = dir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith("_pdfCfg.json");
      }
    });

    long tStampGlobal = System.currentTimeMillis();

    RenderFarm renderFarm = new RenderFarm();

    // run them parallel to simulate a multithreaded server behaviour
    Stream.of(files).parallel().forEach(file -> renderPdf(file, renderFarm));

    System.out.println("Total Time spend: " + (System.currentTimeMillis() - tStampGlobal) + " ms.\n");

  }

  private static void renderPdf(File cfgJson, RenderFarm renderFarm) {

    System.out.println("Generating pdf from " + cfgJson.getName() + " ...");

    try {
      Gson gson = new Gson();

      // JSON to Java object, read it from a file.
      FilebasedRenderModel gsonConfig = gson.fromJson(new FileReader(cfgJson), FilebasedRenderModel.class);

      long tStamp = System.currentTimeMillis();

      // get pdf binary
      InputStream renderPdfIn = renderFarm.renderPdf(gsonConfig);

      // and write it to file
      OutputStream outStream = new FileOutputStream(gsonConfig.getOutputFileName());

      byte[] buffer = new byte[8 * 1024];
      int bytesRead;
      while ((bytesRead = renderPdfIn.read(buffer)) != -1) {
        outStream.write(buffer, 0, bytesRead);
      }
      IOUtils.closeQuietly(renderPdfIn);
      IOUtils.closeQuietly(outStream);

      System.out.println("Finnished " + gsonConfig.getOutputFileName() + " in " + (System.currentTimeMillis() - tStamp) + " ms.\n");
    }
    catch (IOException ex) {
      Logger.getLogger(Cli.class.getName()).log(Level.SEVERE, "Error rendering " + cfgJson.getName() + " " + ex.getMessage(), ex);
    }
    catch (Throwable t) {
      Logger.getLogger(Cli.class.getName()).log(Level.SEVERE, "Error rendering " + cfgJson.getName() + " " + t.getMessage(), t);
    }
  }
}
