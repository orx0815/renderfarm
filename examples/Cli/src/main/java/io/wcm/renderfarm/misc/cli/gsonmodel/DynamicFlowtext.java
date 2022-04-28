
package io.wcm.renderfarm.misc.cli.gsonmodel;

import io.wcm.renderfarm.api.RenderModel.DynaFlowtext;




/**
 * Holds id of svg-element that gets its flowtext replaced by flowparas
 */
public class DynamicFlowtext implements DynaFlowtext {

  private String elementId;
  private String[] flowParas;

  @Override
  public String getElementId() {
    return elementId;
  }

  @Override
  public String[] getFlowParas() {
    return flowParas;
  }

        
}
