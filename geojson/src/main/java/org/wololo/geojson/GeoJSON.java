package org.wololo.geojson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public abstract class GeoJSON {
  private static final ObjectMapper mapper = new ObjectMapper();

  @JsonProperty("type")
  private String type;

  @JsonCreator
  public GeoJSON() {
    setType(getClass().getSimpleName());
  }

  public String toString() {
    try {
      return mapper.writeValueAsString(this);
    } catch (JacksonException e) {
      return "Unhandled exception occured when serializing this instance";
    }
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public static ObjectMapper getMapper() {
    return mapper;
  }
}
