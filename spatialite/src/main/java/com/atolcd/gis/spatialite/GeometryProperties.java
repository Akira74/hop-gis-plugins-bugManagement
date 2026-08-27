package com.atolcd.gis.spatialite;

public class GeometryProperties {

  private final int geometryType;
  private final int coordDimension;
  private final int srid;
  private final boolean spatialIndexEnabled;

  public static int TYPE_GEOMETRY = 0;
  public static int TYPE_POINT = 1;
  public static int TYPE_LINESTRING = 2;
  public static int TYPE_POLYGON = 3;
  public static int TYPE_MULTIPOINT = 4;
  public static int TYPE_MULTILINESTRING = 5;
  public static int TYPE_MULTIPOLYGON = 6;
  public static int TYPE_GEOMETRYCOLLECTION = 7;

  public static int DIMENSION_2D = 2;
  public static int DIMENSION_3D = 3;
  public static int DIMENSION_4D = 4;

  public GeometryProperties(
      int geometryType, int coordDimension, int srid, boolean spatialIndexEnabled) {
    this.geometryType = geometryType;
    this.coordDimension = coordDimension;
    this.srid = srid;
    this.spatialIndexEnabled = spatialIndexEnabled;
  }

  public int getGeometryType() {
    return geometryType;
  }

  public int getCoordDimension() {
    return coordDimension;
  }

  public int getSrid() {
    return srid;
  }

  public boolean isSpatialIndexEnabled() {
    return spatialIndexEnabled;
  }

  public static int fromJTSGeometryType(String JTSGeometryType) {

    if (JTSGeometryType.equalsIgnoreCase("POINT")) {
      return TYPE_POINT;

    } else if (JTSGeometryType.equalsIgnoreCase("LINESTRING")) {
      return TYPE_LINESTRING;

    } else if (JTSGeometryType.equalsIgnoreCase("POLYGON")) {
      return TYPE_POLYGON;

    } else if (JTSGeometryType.equalsIgnoreCase("MULTIPOINT")) {
      return TYPE_MULTIPOINT;

    } else if (JTSGeometryType.equalsIgnoreCase("MULTILINESTRING")) {
      return TYPE_MULTILINESTRING;

    } else if (JTSGeometryType.equalsIgnoreCase("MULTIPOLYGON")) {
      return TYPE_MULTIPOLYGON;

    } else if (JTSGeometryType.equalsIgnoreCase("GEOMETRYCOLLECTION")) {
      return TYPE_GEOMETRYCOLLECTION;

    } else {
      return TYPE_GEOMETRY;
    }
  }

  public String getJTSGeometryType() {

    if (geometryType == TYPE_POINT) {
      return "POINT";

    } else if (geometryType == TYPE_LINESTRING) {
      return "LINESTRING";

    } else if (geometryType == TYPE_POLYGON) {
      return "POLYGON";

    } else if (geometryType == TYPE_MULTIPOINT) {
      return "MULTIPOINT";

    } else if (geometryType == TYPE_MULTILINESTRING) {
      return "MULTILINESTRING";

    } else if (geometryType == TYPE_MULTIPOLYGON) {
      return "MULTIPOLYGON";

    } else if (geometryType == TYPE_GEOMETRYCOLLECTION) {
      return "GEOMETRYCOLLECTION";

    } else {
      return "GEOMETRY";
    }
  }
}
