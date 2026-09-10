package com.atolcd.gis.gpx;

import org.locationtech.jts.geom.Geometry;

public interface ISpatialElement extends IElement {

  void setComment(String comment);

  void setSource(String source);

  void setType(String type);

  String getComment();

  String getSource();

  String getType();

  Geometry getGeometry();
}
