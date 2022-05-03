
package org.motorbrot.renderfarm.misc.cli.gsonmodel;

import org.motorbrot.renderfarm.api.RenderModel;
import org.motorbrot.renderfarm.api.RenderModel.PageModel;

import java.util.ArrayList;
import java.util.List;

public class FilebasedRenderModel implements RenderModel {

  private List<Page> pages = new ArrayList<>();
  private String outputFileName;

  @Override
  public List<? extends PageModel> getPages() {
    return pages;
  }

  @Override
  public String getOutputFileName() {
    return outputFileName;
  }

    
}
