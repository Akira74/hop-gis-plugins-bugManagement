package com.atolcd.gis.svg.type;

public interface IElement {

  void setId(String id);

  void setTitle(String title);

  void setDescription(String description);

  void setSvgStyle(String svgStyle);

  void setCssClass(String cssClass);

  void setTransform(String transform);

  String getId();

  String getTitle();

  String getDescription();

  String getSvgStyle();

  String getCssClass();

  String getTransform();
}
