
package io.wcm.renderfarm.misc.cli.gsonmodel;

import io.wcm.renderfarm.api.RenderModel;
import io.wcm.renderfarm.api.RenderModel.PageModel;

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
