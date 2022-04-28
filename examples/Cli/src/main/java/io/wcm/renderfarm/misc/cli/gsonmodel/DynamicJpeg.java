
package io.wcm.renderfarm.misc.cli.gsonmodel;

import io.wcm.renderfarm.api.RenderModel.DynaImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DynamicJpeg implements DynaImage {

  private String elementId;
  private String source;

  @Override
  public String getElementId() {
    return elementId;
  }
    
  @Override
  public InputStream getImageBinary() {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(source);
    }
    catch (FileNotFoundException ex) {
      Logger.getLogger(DynamicJpeg.class.getName()).log(Level.SEVERE, null, ex);
    }
    return fis;
  }  

}
