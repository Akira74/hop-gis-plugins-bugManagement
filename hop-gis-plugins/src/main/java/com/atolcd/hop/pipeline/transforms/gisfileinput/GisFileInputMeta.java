package com.atolcd.hop.pipeline.transforms.gisfileinput;

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

import com.atolcd.hop.gis.io.AbstractFileReader;
import com.atolcd.hop.gis.io.DXFReader;
import com.atolcd.hop.gis.io.GPXReader;
import com.atolcd.hop.gis.io.GeoJSONReader;
import com.atolcd.hop.gis.io.GeoPackageReader;
import com.atolcd.hop.gis.io.MapInfoReader;
import com.atolcd.hop.gis.io.ShapefileReader;
import com.atolcd.hop.gis.io.SpatialiteReader;
import com.atolcd.hop.gis.io.features.FeatureConverter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.value.ValueMetaBase;
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
    id = "GisFileInput",
    name = "i18n::GisFileInput.Shell.Name",
    description = "i18n::GisFileInput.Shell.Description",
    image = "GisFileInput.png",
    categoryDescription = "i18n::GisFileInput.Shell.CategoryDescription",
    documentationUrl = "",
    keywords = "i18n::GisFileInput.keywords")
public class GisFileInputMeta extends BaseTransformMeta<GisFileInput, GisFileInputData> {

  private static final Class<?> PKG = GisFileInputMeta.class;

  private final HashMap<String, GisInputFormatDef> inputFormatDefs;

  @HopMetadataProperty(injectionKeyDescription = "GisFileInput.FileFormat.Label")
  private String inputFormat;

  @HopMetadataProperty(groupKey = "params", key = "param")
  private List<GisInputFormatParameter> inputFormatParameters;

  @HopMetadataProperty(injectionKeyDescription = "GisFileInput.FileName.Label")
  private String inputFileName;

  @HopMetadataProperty(injectionKeyDescription = "GisFileInput.GeometryFieldName.Label")
  private String geometryFieldName;

  @HopMetadataProperty(injectionKeyDescription = "GisFileInput.Encoding.Label")
  private String encoding;

  @HopMetadataProperty(key = "rowLimit", injectionKeyDescription = "GisFileInput.RowLimit.Label")
  private Long rowLimit;

  /**
   * Generic synchronization switch: when active, the transform waits before opening the file for at
   * least one row (or the end of the data stream) from an incoming hop. The row content itself is
   * ignored - the only purpose is to ensure that an upstream transform (e.g. "Execute a process"
   * with OGR/GDAL) has verifiably finished/started before this file is read. Without an incoming
   * hop, this option has no effect (getRow() then immediately returns null).
   */
  @HopMetadataProperty(key = "waitForPreviousTransform")
  private boolean waitForPreviousTransform;

  public GisFileInputMeta() {
    super();

    this.inputFormatDefs = new HashMap<String, GisInputFormatDef>();
    this.inputFormatParameters = new ArrayList<GisInputFormatParameter>();

    // ESRI Shapefile
    GisInputFormatDef shpDef =
        new GisInputFormatDef("ESRI_SHP", new String[] {"*.shp;*.SHP"}, new String[] {"*.shp"});
    shpDef.addParameterDef(
        "FORCE_TO_2D", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList("TRUE", "FALSE"), "TRUE");
    shpDef.addParameterDef(
        "FORCE_TO_MULTIGEOMETRY",
        ValueMetaBase.TYPE_BOOLEAN,
        true,
        Arrays.asList("TRUE", "FALSE"),
        "FALSE");
    this.inputFormatDefs.put("ESRI_SHP", shpDef);

    // GeoJSON
    GisInputFormatDef geojsonDef =
        new GisInputFormatDef(
            "GEOJSON",
            new String[] {"*.geojson;*.GEOJSON", "*.json;*.JSON"},
            new String[] {"*.geojson", "*.json"});
    geojsonDef.addParameterDef(
        "FORCE_TO_MULTIGEOMETRY",
        ValueMetaBase.TYPE_BOOLEAN,
        true,
        Arrays.asList("TRUE", "FALSE"),
        "FALSE");
    this.inputFormatDefs.put("GEOJSON", geojsonDef);

    // Mapinfo MIF/MID
    GisInputFormatDef mapinfoDef =
        new GisInputFormatDef("MAPINFO_MIF", new String[] {"*.mif;*.MIF"}, new String[] {"*.mif"});
    mapinfoDef.addParameterDef(
        "FORCE_TO_MULTIGEOMETRY",
        ValueMetaBase.TYPE_BOOLEAN,
        true,
        Arrays.asList("TRUE", "FALSE"),
        "FALSE");
    this.inputFormatDefs.put("MAPINFO_MIF", mapinfoDef);

    // SpatialLite
    GisInputFormatDef sqlLiteDef =
        new GisInputFormatDef(
            "SPATIALITE",
            new String[] {"*.db;*.DB", "*.sqlite;*.SQLITE"},
            new String[] {"*.db", "*.sqlite"});
    sqlLiteDef.addParameterDef("DB_TABLE_NAME", ValueMetaBase.TYPE_STRING, true);
    this.inputFormatDefs.put("SPATIALITE", sqlLiteDef);

    // DXF
    GisInputFormatDef dxfDef =
        new GisInputFormatDef("DXF", new String[] {"*.dxf;*.DXF"}, new String[] {"*.dxf"});
    dxfDef.addParameterDef(
        "FORCE_TO_MULTIGEOMETRY",
        ValueMetaBase.TYPE_BOOLEAN,
        true,
        Arrays.asList("TRUE", "FALSE"),
        "FALSE");
    dxfDef.addParameterDef(
        "READ_XDATA", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList("TRUE", "FALSE"), "FALSE");
    dxfDef.addParameterDef(
        "CIRCLE_AS_POLYGON",
        ValueMetaBase.TYPE_BOOLEAN,
        true,
        Arrays.asList("TRUE", "FALSE"),
        "FALSE");
    dxfDef.addParameterDef(
        "ELLIPSE_AS_POLYGON",
        ValueMetaBase.TYPE_BOOLEAN,
        true,
        Arrays.asList("TRUE", "FALSE"),
        "FALSE");
    dxfDef.addParameterDef(
        "LINE_AS_POLYGON",
        ValueMetaBase.TYPE_BOOLEAN,
        true,
        Arrays.asList("TRUE", "FALSE"),
        "FALSE");
    this.inputFormatDefs.put("DXF", dxfDef);

    // GPX
    GisInputFormatDef gpxDef =
        new GisInputFormatDef("GPX", new String[] {"*.gpx;*.GPX"}, new String[] {"*.gpx"});
    gpxDef.addParameterDef(
        "FORCE_TO_2D", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList("TRUE", "FALSE"), "TRUE");
    this.inputFormatDefs.put("GPX", gpxDef);

    // GeoPackage
    GisInputFormatDef gpkgDef =
        new GisInputFormatDef(
            "GEOPACKAGE", new String[] {"*.gpkg;*.GPKG"}, new String[] {"*.gpkg"});
    gpkgDef.addParameterDef("DB_TABLE_NAME", ValueMetaBase.TYPE_STRING, true);
    gpkgDef.addParameterDef(
        "FORCE_TO_2D", ValueMetaBase.TYPE_BOOLEAN, true, Arrays.asList("TRUE", "FALSE"), "TRUE");
    gpkgDef.addParameterDef(
        "FORCE_TO_MULTIGEOMETRY",
        ValueMetaBase.TYPE_BOOLEAN,
        true,
        Arrays.asList("TRUE", "FALSE"),
        "FALSE");
    this.inputFormatDefs.put("GEOPACKAGE", gpkgDef);
  }

  public List<GisInputFormatParameter> getInputFormatParameters() {
    return inputFormatParameters;
  }

  public void setInputFormatParameters(List<GisInputFormatParameter> inputFormatParameters) {
    this.inputFormatParameters = inputFormatParameters;
  }

  public HashMap<String, GisInputFormatDef> getInputFormatDefs() {
    return inputFormatDefs;
  }

  public String getInputFormat() {
    return inputFormat;
  }

  public void setInputFormat(String inputFormat) {
    this.inputFormat = inputFormat;
  }

  public String getInputFileName() {
    return inputFileName;
  }

  public void setInputFileName(String inputFileName) {
    this.inputFileName = inputFileName;
  }

  public String getGeometryFieldName() {
    return geometryFieldName;
  }

  public void setGeometryFieldName(String geometryFieldName) {
    this.geometryFieldName = geometryFieldName;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public Long getRowLimit() {
    return rowLimit;
  }

  public void setRowLimit(Long rowLimit) {
    this.rowLimit = rowLimit;
  }

  public boolean isWaitForPreviousTransform() {
    return waitForPreviousTransform;
  }

  public void setWaitForPreviousTransform(boolean waitForPreviousTransform) {
    this.waitForPreviousTransform = waitForPreviousTransform;
  }

  // Note: getXml() has been removed. Since Apache Hop 2.18,
  // BaseTransformMeta.getXml() is no longer called (@Deprecated since 2.10.0,
  // ignored since 2.18) - serialization now happens exclusively via
  // reflection using the @HopMetadataProperty annotations above.

  @Override
  public void getFields(
      IRowMeta r,
      String origin,
      IRowMeta[] info,
      TransformMeta nextStep,
      IVariables space,
      IHopMetadataProvider metadataProvider) {

    Charset charset;
    try {
      charset = Charset.forName(encoding);
    } catch (Exception e) {
      charset = Charset.defaultCharset();
    }

    try {

      AbstractFileReader fileReader = null;

      if (inputFormat.equalsIgnoreCase("ESRI_SHP")) {
        fileReader =
            new ShapefileReader(
                space.resolve(inputFileName),
                space.resolve(geometryFieldName),
                charset.displayName());
      } else if (inputFormat.equalsIgnoreCase("GEOJSON")) {
        fileReader =
            new GeoJSONReader(
                space.resolve(inputFileName),
                space.resolve(geometryFieldName),
                charset.displayName());
      } else if (inputFormat.equalsIgnoreCase("MAPINFO_MIF")) {
        fileReader =
            new MapInfoReader(
                space.resolve(inputFileName),
                space.resolve(geometryFieldName),
                charset.displayName());
      } else if (inputFormat.equalsIgnoreCase("SPATIALITE")) {
        String tableName = space.resolve((String) getInputParameterValue("DB_TABLE_NAME"));
        fileReader =
            new SpatialiteReader(space.resolve(inputFileName), tableName, charset.displayName());
      } else if (inputFormat.equalsIgnoreCase("DXF")) {
        fileReader =
            new DXFReader(
                space.resolve(inputFileName),
                space.resolve(geometryFieldName),
                charset.displayName(),
                false,
                false,
                false,
                Boolean.valueOf(space.resolve((String) getInputParameterValue("READ_XDATA"))));
      } else if (inputFormat.equalsIgnoreCase("GPX")) {
        fileReader =
            new GPXReader(
                space.resolve(inputFileName),
                space.resolve(geometryFieldName),
                charset.displayName());
      } else if (inputFormat.equalsIgnoreCase("GEOPACKAGE")) {

        fileReader =
            new GeoPackageReader(
                space.resolve(inputFileName),
                space.resolve((String) getInputParameterValue("DB_TABLE_NAME")),
                space.resolve(geometryFieldName),
                charset.displayName());
      }
      r.addRowMeta(FeatureConverter.getRowMeta(fileReader.getFields(), origin));
    } catch (HopException e) {
      e.printStackTrace();
    }
  }

  public List<String> getParameterPredefinedValues(String formatKey, String parameterKey) {
    List<String> predefinedValues =
        inputFormatDefs.get(formatKey).getParameterDef(parameterKey).getPredefinedValues();
    Collections.sort(predefinedValues);
    return predefinedValues;
  }

  public int getParameterValueMetaType(String formatKey, String parameterKey) {

    return inputFormatDefs.get(formatKey).getParameterDef(parameterKey).getValueMetaType();
  }

  public Object getInputParameterValue(String parameterKey) {

    for (GisInputFormatParameter parameter : inputFormatParameters) {

      if (parameter.getKey().equalsIgnoreCase(parameterKey)) {
        return parameter.getValue();
      }
    }

    return null;
  }

  public Object clone() {

    Object retval = super.clone();
    return retval;
  }

  // Note: loadXml() has been removed - for the same reason as getXml()
  // (see above). Loading now happens automatically via the
  // @HopMetadataProperty annotations. CAUTION: .hpl files already saved
  // with the old (faulty) serialization contain
  // NO <rowLimit> and NO <params> - the first time these
  // legacy files are opened, rowLimit remains null. Saving them once more
  // fixes this permanently, since the correct tags are written from then on.

  public void setDefault() {

    inputFormat = "ESRI_SHP";
    rowLimit = (long) 0;
  }

  @Override
  public void check(
      List<ICheckResult> remarks,
      PipelineMeta transmeta,
      TransformMeta stepMeta,
      IRowMeta prev,
      String[] input,
      String[] output,
      IRowMeta info,
      IVariables variables,
      IHopMetadataProvider metadataProvider) {

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
