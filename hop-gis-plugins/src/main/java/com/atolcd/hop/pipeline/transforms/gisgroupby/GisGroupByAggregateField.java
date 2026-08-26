package com.atolcd.hop.pipeline.transforms.gisgroupby;

/*
 * #%L
 * Apache Hop GIS Plugin
 * %%
 * Copyright (C) 2021 Atol CD
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.apache.hop.metadata.api.HopMetadataProperty;

// Ersetzt die vormaligen parallelen Arrays (aggregateField[], subjectField[],
// aggregateType[], valueField[]), damit Hops reflection-basierte
// (@HopMetadataProperty) Serialisierung greift. Die Tag-Namen entsprechen
// denen der alten manuellen XML-Serialisierung.
public class GisGroupByAggregateField {

  @HopMetadataProperty(key = "aggregate")
  private String aggregateField;

  @HopMetadataProperty(key = "subject")
  private String subjectField;

  // Wird als String (Code, z.B. "SUM") gespeichert statt als int, damit die
  // .hpl-Datei robust gegen interne Nummerierungsaenderungen bleibt - genau
  // wie im alten Format ueber getTypeDesc()/getType().
  @HopMetadataProperty(key = "type")
  private String typeDesc;

  @HopMetadataProperty(key = "valuefield")
  private String valueField;

  public GisGroupByAggregateField() {}

  public GisGroupByAggregateField(
      String aggregateField, String subjectField, String typeDesc, String valueField) {
    this.aggregateField = aggregateField;
    this.subjectField = subjectField;
    this.typeDesc = typeDesc;
    this.valueField = valueField;
  }

  public String getAggregateField() {
    return aggregateField;
  }

  public void setAggregateField(String aggregateField) {
    this.aggregateField = aggregateField;
  }

  public String getSubjectField() {
    return subjectField;
  }

  public void setSubjectField(String subjectField) {
    this.subjectField = subjectField;
  }

  public String getTypeDesc() {
    return typeDesc;
  }

  public void setTypeDesc(String typeDesc) {
    this.typeDesc = typeDesc;
  }

  public String getValueField() {
    return valueField;
  }

  public void setValueField(String valueField) {
    this.valueField = valueField;
  }
}
