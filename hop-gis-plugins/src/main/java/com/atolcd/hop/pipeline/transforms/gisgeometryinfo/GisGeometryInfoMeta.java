package com.atolcd.hop.pipeline.transforms.gisgeometryinfo;

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

import com.atolcd.hop.core.row.value.ValueMetaGeometry;
import com.atolcd.hop.pipeline.transforms.gisfileinput.GisFileInputDialog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopPluginException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaBase;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.eclipse.swt.widgets.Shell;

@Transform(
    id = "GisGeometryInfo",
    name = "i18n::GisGeometryInfo.Shell.Name",
    description = "i18n::GisGeometryInfo.Shell.Description",
    image = "GisGeometryInfo.png",
    categoryDescription = "i18n::GisGeometryInfo.Shell.CategoryDescription",
    documentationUrl = "",
    keywords = "i18n::GisGeometryInfo.keywords")
public class GisGeometryInfoMeta extends BaseTransformMeta<GisGeometryInfo, GisGeometryInfoData> {

  private HashMap<String, Integer> infosTypes;

  @HopMetadataProperty(injectionKeyDescription = "GisGeometryInfo.GeometryFieldName.Label")
  private String geometryFieldName;

  @HopMetadataProperty(groupKey = "outputs", key = "output")
  private List<GisGeometryInfoOutputField> outputFieldList;

  public GisGeometryInfoMeta() {

    super();
    this.infosTypes = new HashMap<String, Integer>();
    this.outputFieldList = new ArrayList<GisGeometryInfoOutputField>();

    this.infosTypes.put("NULL_OR_EMPTY", ValueMetaBase.TYPE_BOOLEAN);
    this.infosTypes.put("AREA", ValueMetaBase.TYPE_NUMBER);
    this.infosTypes.put("LENGTH", ValueMetaBase.TYPE_NUMBER);
    this.infosTypes.put("DIMENSION", ValueMetaBase.TYPE_INTEGER);
    this.infosTypes.put("SRID", ValueMetaBase.TYPE_INTEGER);
    this.infosTypes.put("GEOMETRY_TYPE", ValueMetaBase.TYPE_STRING);
    this.infosTypes.put("GEOMETRY_COUNT", ValueMetaBase.TYPE_INTEGER);
    this.infosTypes.put("GEOMETRY_VERTEX_COUNT", ValueMetaBase.TYPE_INTEGER);
    this.infosTypes.put("X_MIN", ValueMetaBase.TYPE_NUMBER);
    this.infosTypes.put("Y_MIN", ValueMetaBase.TYPE_NUMBER);
    this.infosTypes.put("Z_MIN", ValueMetaBase.TYPE_NUMBER);
    this.infosTypes.put("X_MAX", ValueMetaBase.TYPE_NUMBER);
    this.infosTypes.put("Y_MAX", ValueMetaBase.TYPE_NUMBER);
    this.infosTypes.put("Z_MAX", ValueMetaBase.TYPE_NUMBER);
  }

  public HashMap<String, Integer> getInfosTypes() {
    return infosTypes;
  }

  public void setInfosTypes(HashMap<String, Integer> infosTypes) {
    this.infosTypes = infosTypes;
  }

  public String getGeometryFieldName() {
    return geometryFieldName;
  }

  public void setGeometryFieldName(String geometryFieldName) {
    this.geometryFieldName = geometryFieldName;
  }

  // IMPORTANT: Hop's reflection-based (de-)serialization looks up getters/setters
  // by the exact field name (here: outputFieldList) - NOT by the
  // @HopMetadataProperty annotation or any other method name.
  // These two methods are therefore mandatory, in addition to the
  // LinkedHashMap wrapper API below (which remains for dialog/runtime use).
  public List<GisGeometryInfoOutputField> getOutputFieldList() {
    return outputFieldList;
  }

  public void setOutputFieldList(List<GisGeometryInfoOutputField> outputFieldList) {
    this.outputFieldList = outputFieldList;
  }

  // Public API remains unchanged (LinkedHashMap<String,String>) so that
  // the dialog and runtime class do not need to be adapted. Internally,
  // outputFieldList (see above) is now serialized instead, since Hop 2.18
  // can no longer annotate maps, only lists of POJOs.
  public LinkedHashMap<String, String> getOutputFields() {
    LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
    for (GisGeometryInfoOutputField field : outputFieldList) {
      map.put(field.getInfoKey(), field.getInfoFieldname());
    }
    return map;
  }

  public void setOutputFields(LinkedHashMap<String, String> outputFields) {
    this.outputFieldList = new ArrayList<GisGeometryInfoOutputField>();
    for (Entry<String, String> entry : outputFields.entrySet()) {
      this.outputFieldList.add(new GisGeometryInfoOutputField(entry.getKey(), entry.getValue()));
    }
  }

  // Note: getXml() has been removed. Since Apache Hop 2.18,
  // BaseTransformMeta.getXml() is no longer called - serialization now
  // happens exclusively via reflection using the
  // @HopMetadataProperty annotations above.

  @Override
  public void getFields(
      IRowMeta r,
      String origin,
      IRowMeta[] info,
      TransformMeta nextStep,
      IVariables space,
      IHopMetadataProvider metadataProvider) {

    for (GisGeometryInfoOutputField output : outputFieldList) {

      String fieldName = output.getInfoFieldname();
      int valueMetaType = infosTypes.get(output.getInfoKey());

      IValueMeta valueMeta = null;

      if (valueMetaType == ValueMetaGeometry.TYPE_GEOMETRY) {
        valueMeta = new ValueMetaGeometry(fieldName);
      } else {
        try {
          valueMeta = ValueMetaFactory.createValueMeta(fieldName, valueMetaType);
        } catch (HopPluginException e) {
          throw new RuntimeException(e);
        }
      }

      valueMeta.setOrigin(origin);
      r.addValueMeta(valueMeta);
    }
  }

  public Object clone() {

    Object retval = super.clone();
    return retval;
  }

  // Note: loadXml() has been removed - for the same reason as getXml()
  // (see above). CAUTION: .hpl files already saved in the old format do not
  // contain these tags - saving them once more will fix this
  // permanently.

  public void setDefault() {}

  public void check(
      List<ICheckResult> remarks,
      PipelineMeta transmeta,
      TransformMeta stepMeta,
      IRowMeta prev,
      String[] input,
      String[] output,
      IRowMeta info) {

    CheckResult cr;

    if (input.length > 0) {

      cr =
          new CheckResult(
              CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta);
      remarks.add(cr);

    } else {

      cr =
          new CheckResult(
              CheckResult.TYPE_RESULT_ERROR, "No input received from other steps.", stepMeta);
      remarks.add(cr);
    }
  }

  public ITransformDialog getDialog(
      Shell shell, IVariables variables, ITransformMeta meta, PipelineMeta transMeta, String name) {
    return new GisFileInputDialog(shell, variables, meta, transMeta, name);
  }
}
