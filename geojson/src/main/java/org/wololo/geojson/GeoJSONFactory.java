package org.wololo.geojson;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class GeoJSONFactory {

  private static final ObjectMapper mapper = new ObjectMapper();

  public static GeoJSON create(File geoJson) {
    try {
      JsonNode node = mapper.readTree(geoJson);
      String type = node.get("type").asText();
      if (type.equals("FeatureCollection")) {
        return readFeatureCollection(node);
      } else if (type.equals("Feature")) {
        return readFeature(node);
      } else {
        return readGeometry(node, type);
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static FeatureCollection readFeatureCollection(JsonNode node)
      throws IOException, ClassNotFoundException {

    Feature[] features = new Feature[node.get("features").size()];

    JsonNode crsNode = node.get("crs");
    for (int i = 0; i < node.get("features").size(); i++) {
      features[i] = readFeature(node.get("features").get(i));
    }
    return new FeatureCollection(features, readCrs(crsNode));
  }

  private static Crs readCrs(JsonNode node) throws IOException, ClassNotFoundException {

    if (node != null) {

      JavaType javaType =
          mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
      Map<String, Object> properties = mapper.treeToValue(node.get("properties"), javaType);
      String type = node.get("type").asText();
      return new Crs(type, properties);

    } else {
      return null;
    }
  }

  private static Feature readFeature(JsonNode node) throws IOException, ClassNotFoundException {
    JsonNode geometryNode = node.get("geometry");
    JavaType javaType =
        mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
    Map<String, Object> properties = mapper.treeToValue(node.get("properties"), javaType);

    Geometry geometry = null;

    if (geometryNode != null && geometryNode.get("type") != null) {
      String type = geometryNode.get("type").asText();
      geometry = readGeometry(geometryNode, type);
    }

    return new Feature(geometry, properties);
  }

  private static Geometry readGeometry(JsonNode node, String type)
      throws IOException, ClassNotFoundException {
    Geometry geometry =
        (Geometry) mapper.treeToValue(node, Class.forName("org.wololo.geojson." + type));
    return geometry;
  }
}
