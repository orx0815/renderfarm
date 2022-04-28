
package io.wcm.renderfarm.misc.cli.gsonmodel;

import io.wcm.renderfarm.api.RenderModel.DynaText;


/**
 * Holds id of svg-element that gets its textContent replaced by textValue 
 */
public class DynamicText implements DynaText {

  private String elementId;
  private String textValue;

  @Override
  public String getElementId() {
    return elementId;
  }

  @Override
  public String getTextValue() {
    return textValue;
  }

        
}
