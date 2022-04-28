package io.wcm.renderfarm.api.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.wcm.renderfarm.api.RenderModel;

/**
 * An implementation of the {@link RenderModel} interface to be used in UnitTests
 */
public class RenderModelTestImpl implements RenderModel {

  private List<PageModelImpl> pages = new ArrayList<>();

  @Override
  public String getOutputFileName() {
    return null;
  }

  @Override
  public List<? extends PageModelImpl> getPages() {
    return pages;
  }

  /**
   * @param page the page to add
   */
  public void addPage(PageModelImpl page) {
    if (page != null) {
      pages.add(page);
    }
  }

  /**
   * An implementation of the PageModel with simple getter/setters
   */
  public static class PageModelImpl implements PageModel {

    private String template;
    private String staticPage;
    private List<DynaImageImpl> dynamicJpegs = new ArrayList<>();
    private List<DynaTextImpl> dynamicTexts = new ArrayList<>();
    private List<DynaFlowtextImpl> dynamicFlowtexts = new ArrayList<>();
    private List<String> idsOfElementsToDelete = new ArrayList<>();
    private InputStream svgTemplate;

    private InputStream staticPdfPage;

    /**
     * @return template or null if it's a static pdf
     */
    @Override
    public String getTemplate() {
      return template;
    }

    /**
     * @param svgTemplatePath path to svg
     */
    public void setTemplate(String svgTemplatePath) {
      this.template = svgTemplatePath;
    }

    /**
     * @return path to staticPage, or null if it's a dynamic page from svgTemplate
     */
    public String getStaticPage() {
      return staticPage;
    }

    /**
     * @param staticPage path to staticPage
     */
    public void setStaticPage(String staticPage) {
      this.staticPage = staticPage;
    }

    @Override
    public List<? extends DynaImageImpl> getDynamicJpegs() {
      return dynamicJpegs;
    }

    /**
     * @param image the image to add to the end of the list
     */
    public void addDynamicJpeg(DynaImageImpl image) {
      if (image.getSource() != null || image.getImageBinary() != null) {
        dynamicJpegs.add(image);
      }
    }

    @Override
    public List<? extends DynaText> getDynamicTexts() {
      return dynamicTexts;
    }

    /**
     * @param text the text to add to the end of the list
     */
    public void addDynamicText(DynaTextImpl text) {
      dynamicTexts.add(text);
    }
    
    @Override
    public List<? extends DynaFlowtext> getDynamicFlowtexts() {
      return dynamicFlowtexts;
    }

    /**
     * @param flowtext the text to add to the end of the list
     */
    public void addDynamicFlowtext(DynaFlowtextImpl flowtext) {
      dynamicFlowtexts.add(flowtext);
    }

    @Override
    public InputStream getSvgStream() {
      return svgTemplate;
    }

    /**
     * @param inputStream binary svg
     */
    public void setSvgInputStream(InputStream inputStream) {
      this.svgTemplate = inputStream;
    }

    @Override
    public InputStream getStaticPdfPage() {
      return this.staticPdfPage;
    }

    /**
     * @param inputStream binary pdf
     */
    public void setStaticPdfStream(InputStream inputStream) {
      this.staticPdfPage = inputStream;
    }

    @Override
    public List<String> getIdsOfElementsToDelete() {
      return idsOfElementsToDelete;
    }

  }

  /**
   * simple getter/setter impl
   */
  public static class DynaImageImpl implements DynaImage {

    private String elementId;
    private String source;
    private InputStream imageBinary;

    /**
     * @return source
     */
    public String getSource() {
      return this.source;
    }

    /**
     * @param imageUri uri to img
     */
    public void setSource(String imageUri) {
      this.source = imageUri;
    }

    @Override
    public String getElementId() {
      return elementId;
    }

    /**
     * @param elementId xml id
     */
    public void setElementId(String elementId) {
      this.elementId = elementId;
    }

    @Override
    public InputStream getImageBinary() {
      return imageBinary;
    }

    /**
     * @param inputStream binary
     */
    public void setImageBinary(InputStream inputStream) {
      this.imageBinary = inputStream;
    }

  }

  /**
   * simple getter/setter impl
   */
  public static class DynaTextImpl implements DynaText {

    private String elementId;
    private String textValue;

    @Override
    public String getElementId() {
      return elementId;
    }

    /**
     * @param elementId xml-id
     */
    public void setElementId(String elementId) {
      this.elementId = elementId;
    }

    @Override
    public String getTextValue() {
      return textValue;
    }

    /**
     * @param textValue dynamic text
     */
    public void setTextValue(String textValue) {
      this.textValue = textValue;
    }
  }
  
  /**
   * simple getter/setter impl
   */
  public static class DynaFlowtextImpl implements DynaFlowtext {

    private String elementId;
    private String[] flowParas;

    @Override
    public String getElementId() {
      return elementId;
    }

    /**
     * @param elementId xml-id
     */
    public void setElementId(String elementId) {
      this.elementId = elementId;
    }

    @Override
    public String[] getFlowParas() {
      return flowParas;
    }

    /**
     * @param flowParas dynamic flowtext
     */
    public void setFlowParas(String[] flowParas) {
      this.flowParas = flowParas;
    }
    
  }
}
