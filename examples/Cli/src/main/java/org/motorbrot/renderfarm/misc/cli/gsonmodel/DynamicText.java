
package org.motorbrot.renderfarm.misc.cli.gsonmodel;

import org.motorbrot.renderfarm.api.RenderModel.DynaText;


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
