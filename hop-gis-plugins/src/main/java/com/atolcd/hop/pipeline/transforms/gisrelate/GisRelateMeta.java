package com.atolcd.hop.pipeline.transforms.gisrelate;

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

import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopPluginException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transform.stream.IStream;
import org.apache.hop.resource.IResourceNaming;
import org.eclipse.swt.widgets.Shell;

@Transform(
    id = "GisRelate",
    name = "i18n::GisRelate.Shell.Name",
    description = "i18n::GisRelate.Shell.Description",
    image = "GisRelate.svg",
    categoryDescription = "i18n::GisRelate.Shell.CategoryDescription",
    documentationUrl = "",
    keywords = "i18n::GisRelate.keywords")
public class GisRelateMeta extends BaseTransformMeta<GisRelate, GisRelateData> {

  private static final Class<?> PKG = GisRelateMeta.class; // Needed by Translator

  @HopMetadataProperty(injectionKeyDescription = "GisRelate.Operator.Label")
  private String operator;

  // Opérateurs avec résultat de type boolean
  private static String[] boolResultOperators =
      new String[] {
        "CONTAINS",
        "COVERED_BY",
        "COVERS",
        "CROSSES",
        "DISJOINT",
        "EQUALS",
        "EQUALS_EXACT",
        "INTERSECTS",
        "WITHIN",
        "OVERLAPS",
        "TOUCHES",
        "IS_WITHIN_DISTANCE",
        "IS_NOT_WITHIN_DISTANCE"
      };

  // Opérateurs avec résultat de type numérique
  private static String[] numericResultOperators = new String[] {"DISTANCE_MIN", "DISTANCE_MAX"};

  @HopMetadataProperty(injectionKeyDescription = "GisRelate.FirstGeometryFieldName.Label")
  private String firstGeometryFieldName;

  @HopMetadataProperty(injectionKeyDescription = "GisRelate.SecondGeometryFieldName.Label")
  private String secondGeometryFieldName;

  // Filtrage de lignes
  private static String[] returnTypes = new String[] {"ALL", "FALSE", "TRUE"};

  @HopMetadataProperty(injectionKeyDescription = "GisRelate.ReturnType.Label")
  private String returnType;

  // Pour opérateurs avec besoin de distance
  private static String[] withDistanceOperators =
      new String[] {"IS_WITHIN_DISTANCE", "IS_NOT_WITHIN_DISTANCE"};

  @HopMetadataProperty(injectionKeyDescription = "GisRelate.DistanceDynamic.Label")
  private boolean dynamicDistance;

  @HopMetadataProperty(injectionKeyDescription = "GisRelate.DistanceFieldName.ToolTip")
  private String distanceFieldName;

  @HopMetadataProperty(injectionKeyDescription = "GisRelate.DistanceValue.ToolTip")
  private String distanceValue;

  // Colonne de sortie
  @HopMetadataProperty(injectionKeyDescription = "GisRelate.OutputFieldName.Label")
  private String outputFieldName;

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public String getFirstGeometryFieldName() {
    return firstGeometryFieldName;
  }

  public String[] getBoolResultOperators() {
    return boolResultOperators;
  }

  public String[] getNumericResultOperators() {
    return numericResultOperators;
  }

  public String[] getWithDistanceOperators() {
    return withDistanceOperators;
  }

  public void setFirstGeometryFieldName(String firstGeometryFieldName) {
    this.firstGeometryFieldName = firstGeometryFieldName;
  }

  public String getSecondGeometryFieldName() {
    return secondGeometryFieldName;
  }

  public void setSecondGeometryFieldName(String secondGeometryFieldName) {
    this.secondGeometryFieldName = secondGeometryFieldName;
  }

  public String getOutputFieldName() {
    return outputFieldName;
  }

  public void setOutputFieldName(String outputFieldName) {
    this.outputFieldName = outputFieldName;
  }

  public String getReturnType() {
    return returnType;
  }

  public void setReturnType(String returnType) {
    this.returnType = returnType;
  }

  public String[] getReturnTypes() {
    return returnTypes;
  }

  public boolean isDynamicDistance() {
    return dynamicDistance;
  }

  public void setDynamicDistance(boolean dynamicDistance) {
    this.dynamicDistance = dynamicDistance;
  }

  public String getDistanceFieldName() {
    return distanceFieldName;
  }

  public void setDistanceFieldName(String distanceFieldName) {
    this.distanceFieldName = distanceFieldName;
  }

  public String getDistanceValue() {
    return distanceValue;
  }

  public void setDistanceValue(String distanceValue) {
    this.distanceValue = distanceValue;
  }

  // Hinweis: getXml() wurde entfernt. Seit Apache Hop 2.18 wird
  // BaseTransformMeta.getXml() nicht mehr aufgerufen - die Serialisierung
  // erfolgt jetzt ausschliesslich reflection-basiert ueber die
  // @HopMetadataProperty-Annotationen oben.

  @Override
  public void getFields(
      IRowMeta r,
      String origin,
      IRowMeta[] info,
      TransformMeta nextStep,
      IVariables space,
      IHopMetadataProvider metadataProvider) {

    if (ArrayUtils.contains(numericResultOperators, operator)) {
      IValueMeta valueMeta = null;
      try {
        valueMeta = ValueMetaFactory.createValueMeta(outputFieldName, IValueMeta.TYPE_NUMBER);
      } catch (HopPluginException e) {
        throw new RuntimeException(e);
      }
      valueMeta.setOrigin(origin);
      r.addValueMeta(valueMeta);
    }

    if (ArrayUtils.contains(boolResultOperators, operator)) {

      if (returnType.equalsIgnoreCase("ALL")) {
        IValueMeta valueMeta = null;
        try {
          valueMeta = ValueMetaFactory.createValueMeta(outputFieldName, IValueMeta.TYPE_BOOLEAN);
        } catch (HopPluginException e) {
          throw new RuntimeException(e);
        }
        valueMeta.setOrigin(origin);
        r.addValueMeta(valueMeta);
      }
    }
  }

  public Object clone() {

    Object retval = super.clone();
    return retval;
  }

  // Hinweis: loadXml() wurde entfernt - aus demselben Grund wie getXml()
  // (siehe oben). ACHTUNG: Bereits gespeicherte .hpl-Dateien mit dem alten
  // Format enthalten diese Tags nicht - einmal neu speichern behebt das
  // dauerhaft.

  public void setDefault() {
    operator = "CONTAINS";
    returnType = "ALL";
  }

  public ITransformDialog getDialog(
      Shell shell, IVariables variables, ITransformMeta meta, PipelineMeta transMeta, String name) {
    return new GisRelateDialog(shell, variables, meta, transMeta, name);
  }

  @Override
  public void check(
      List remarks,
      PipelineMeta pipelineMeta,
      TransformMeta transformMeta,
      IRowMeta prev,
      String[] input,
      String[] output,
      IRowMeta info,
      IVariables variables,
      IHopMetadataProvider metadataProvider) {
    // TODO Auto-generated method stub
    CheckResult cr;

    if (input.length > 0) {

      cr =
          new CheckResult(
              CheckResult.TYPE_RESULT_OK,
              "Step is receiving info from other steps.",
              transformMeta);
      remarks.add(cr);

    } else {

      cr =
          new CheckResult(
              CheckResult.TYPE_RESULT_ERROR, "No input received from other steps.", transformMeta);
      remarks.add(cr);
    }
  }

  @Override
  public void analyseImpact(
      IVariables variables,
      List impact,
      PipelineMeta pipelineMeta,
      TransformMeta transformMeta,
      IRowMeta prev,
      String[] input,
      String[] output,
      IRowMeta info,
      IHopMetadataProvider metadataProvider)
      throws HopTransformException {
    // TODO Auto-generated method stub

  }

  @Override
  public String exportResources(
      IVariables variables,
      Map definitions,
      IResourceNaming iResourceNaming,
      IHopMetadataProvider metadataProvider)
      throws HopException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void searchInfoAndTargetTransforms(List transforms) {
    // TODO Verifier si on en a besoin
    List<IStream> infoStreams = getTransformIOMeta().getInfoStreams();
    for (IStream stream : infoStreams) {
      stream.setTransformMeta(
          TransformMeta.findTransform(transforms, (String) stream.getSubject()));
    }
  }
}
