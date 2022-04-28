package io.wcm.renderfarm.example.model;

import io.wcm.renderfarm.api.RenderModel;
import java.util.ArrayList;
import java.util.List;

/**
 * loads images and svg from bundle-resource
 */
public class ClasspathRenderModelImpl implements RenderModel {

  @Override
  public String getOutputFileName() {
    return "Sling-Example-Pdf";
  }

  @Override
  public List<? extends PageModel> getPages() {
    List<PageModel> pages = new ArrayList<>();
    PageModel page = new PageOneModel();
    pages.add(page);
    return pages;
  }
  
  
}
