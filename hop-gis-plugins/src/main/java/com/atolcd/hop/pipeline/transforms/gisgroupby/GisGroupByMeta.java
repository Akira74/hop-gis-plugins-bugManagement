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

import com.atolcd.hop.core.row.value.ValueMetaGeometry;
import java.util.ArrayList;
import java.util.List;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopPluginException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;

@Transform(
    id = "GisGroupBy",
    name = "i18n::GisGroupBy.Shell.Name",
    description = "i18n::GisGroupBy.Shell.Description",
    image = "GisGroupBy.png",
    categoryDescription = "i18n::GisGroupBy.Shell.CategoryDescription",
    documentationUrl = "",
    keywords = "i18n::GisGroupBy.keywords")
public class GisGroupByMeta extends BaseTransformMeta<GisGroupBy, GisGroupByData> {

  private static final Class<?> PKG = GisGroupByMeta.class;

  public static final int TYPE_GROUP_NONE = 0;

  public static final int TYPE_GROUP_SUM = 1;

  public static final int TYPE_GROUP_AVERAGE = 2;

  public static final int TYPE_GROUP_MEDIAN = 3;

  public static final int TYPE_GROUP_PERCENTILE = 4;

  public static final int TYPE_GROUP_MIN = 5;

  public static final int TYPE_GROUP_MAX = 6;

  public static final int TYPE_GROUP_COUNT_ALL = 7;

  public static final int TYPE_GROUP_CONCAT_COMMA = 8;

  public static final int TYPE_GROUP_FIRST = 9;

  public static final int TYPE_GROUP_LAST = 10;

  public static final int TYPE_GROUP_FIRST_INCL_NULL = 11;

  public static final int TYPE_GROUP_LAST_INCL_NULL = 12;

  public static final int TYPE_GROUP_CUMULATIVE_SUM = 13;

  public static final int TYPE_GROUP_CUMULATIVE_AVERAGE = 14;

  public static final int TYPE_GROUP_STANDARD_DEVIATION = 15;

  public static final int TYPE_GROUP_CONCAT_STRING = 16;

  public static final int TYPE_GROUP_COUNT_DISTINCT = 17;

  public static final int TYPE_GROUP_COUNT_ANY = 18;

  // GIS : Ajout d'opérateurs d'aggrégation
  public static final int TYPE_GROUP_GEOMETRY_UNION = 19;
  public static final int TYPE_GROUP_GEOMETRY_EXTENT = 20;
  public static final int TYPE_GROUP_GEOMETRY_AGG = 21;
  public static final int TYPE_GROUP_GEOMETRY_DISSOLVE = 22;

  // GIS : Ajout d'opérateurs d'aggrégation
  public static final String[] typeGroupCode = /*
                                                  * WARNING: DO NOT TRANSLATE
                                                  * THIS. WE ARE SERIOUS, DON'T
                                                  * TRANSLATE!
                                                  */ {
    "-",
    "SUM",
    "AVERAGE",
    "MEDIAN",
    "PERCENTILE",
    "MIN",
    "MAX",
    "COUNT_ALL",
    "CONCAT_COMMA",
    "FIRST",
    "LAST",
    "FIRST_INCL_NULL",
    "LAST_INCL_NULL",
    "CUM_SUM",
    "CUM_AVG",
    "STD_DEV",
    "CONCAT_STRING",
    "COUNT_DISTINCT",
    "COUNT_ANY",
    "GEOMETRY_UNION",
    "GEOMETRY_EXTENT",
    "GEOMETRY_AGG",
    "GEOMETRY_DISSOLVE"
  };

  public static final String[] typeGroupLongDesc = {
    "-",
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.SUM"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.AVERAGE"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.MEDIAN"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.PERCENTILE"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.MIN"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.MAX"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.CONCAT_ALL"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.CONCAT_COMMA"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.FIRST"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.LAST"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.FIRST_INCL_NULL"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.LAST_INCL_NULL"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.CUMUMALTIVE_SUM"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.CUMUMALTIVE_AVERAGE"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.STANDARD_DEVIATION"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.CONCAT_STRING"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.COUNT_DISTINCT"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.COUNT_ANY"),
    // GIS : Ajout d'opérateurs d'aggrégation
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.GEOMETRY_UNION"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.GEOMETRY_EXTENT"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.GEOMETRY_AGG"),
    BaseMessages.getString(PKG, "GroupByMeta.TypeGroupLongDesc.GEOMETRY_DISSOLVE")
  };

  /** All rows need to pass, adding an extra row at the end of each group/block. */
  @HopMetadataProperty(key = "all_rows")
  private boolean passAllRows;

  /** Directory to store the temp files */
  @HopMetadataProperty(key = "directory")
  private String directory;

  /** Temp files prefix... */
  @HopMetadataProperty(key = "prefix")
  private String prefix;

  /** Indicate that some rows don't need to be considered : TODO: make work in GUI & worker */
  @HopMetadataProperty(key = "ignore_aggregate")
  private boolean aggregateIgnored;

  /**
   * name of the boolean field that indicates we need to ignore the row : TODO: make work in GUI &
   * worker
   */
  @HopMetadataProperty(key = "field_ignore")
  private String aggregateIgnoredField;

  /** Fields to group over */
  @HopMetadataProperty(groupKey = "group", key = "field")
  private List<GisGroupByField> groupFields;

  /** Aggregate field definitions (aggregate name, subject, type, value field) */
  @HopMetadataProperty(groupKey = "fields", key = "field")
  private List<GisGroupByAggregateField> aggregateFields;

  /** Add a linenr in the group, resetting to 0 in a new group. */
  @HopMetadataProperty(key = "add_linenr")
  private boolean addingLineNrInGroup;

  /** The fieldname that will contain the added integer field */
  @HopMetadataProperty(key = "linenr_fieldname")
  private String lineNrInGroupField;

  /**
   * Flag to indicate that we always give back one row. Defaults to true for existing
   * transformations.
   */
  @HopMetadataProperty(key = "give_back_row")
  private boolean alwaysGivingBackOneRow;

  public GisGroupByMeta() {
    super(); // allocate BaseStepMeta
    this.groupFields = new ArrayList<GisGroupByField>();
    this.aggregateFields = new ArrayList<GisGroupByAggregateField>();
  }

  /**
   * @return Returns the aggregateField as an array (derived from aggregateFields, safe for
   *     read-only use).
   */
  public String[] getAggregateField() {
    String[] result = new String[aggregateFields.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = aggregateFields.get(i).getAggregateField();
    }
    return result;
  }

  /**
   * @return Returns the aggregateIgnored.
   */
  public boolean isAggregateIgnored() {
    return aggregateIgnored;
  }

  /**
   * @param aggregateIgnored The aggregateIgnored to set.
   */
  public void setAggregateIgnored(boolean aggregateIgnored) {
    this.aggregateIgnored = aggregateIgnored;
  }

  /**
   * @return Returns the aggregateIgnoredField.
   */
  public String getAggregateIgnoredField() {
    return aggregateIgnoredField;
  }

  /**
   * @param aggregateIgnoredField The aggregateIgnoredField to set.
   */
  public void setAggregateIgnoredField(String aggregateIgnoredField) {
    this.aggregateIgnoredField = aggregateIgnoredField;
  }

  /**
   * @return Returns the aggregateType as an array (derived from aggregateFields, safe for read-only
   *     use).
   */
  public int[] getAggregateType() {
    int[] result = new int[aggregateFields.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = getType(aggregateFields.get(i).getTypeDesc());
    }
    return result;
  }

  /**
   * @return Returns the groupField as an array (derived from groupFields, safe for read-only use).
   */
  public String[] getGroupField() {
    String[] result = new String[groupFields.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = groupFields.get(i).getName();
    }
    return result;
  }

  /**
   * @return Returns the passAllRows.
   */
  public boolean passAllRows() {
    return passAllRows;
  }

  /**
   * @param passAllRows The passAllRows to set.
   */
  public void setPassAllRows(boolean passAllRows) {
    this.passAllRows = passAllRows;
  }

  /**
   * @return Returns the subjectField as an array (derived from aggregateFields, safe for read-only
   *     use).
   */
  public String[] getSubjectField() {
    String[] result = new String[aggregateFields.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = aggregateFields.get(i).getSubjectField();
    }
    return result;
  }

  /**
   * @return Returns the valueField as an array (derived from aggregateFields, safe for read-only
   *     use).
   */
  public String[] getValueField() {
    String[] result = new String[aggregateFields.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = aggregateFields.get(i).getValueField();
    }
    return result;
  }

  // New, recommended list-based API - direct access to the
  // actually serialized fields. GisGroupByDialog uses this API
  // for writing (see ok()), since the old array getters above create a
  // NEW array on every call, so an in-place write access like
  // `getGroupField()[i] = ...` would no longer be persisted.
  public List<GisGroupByField> getGroupFields() {
    return groupFields;
  }

  public void setGroupFields(List<GisGroupByField> groupFields) {
    this.groupFields = groupFields;
  }

  public List<GisGroupByAggregateField> getAggregateFields() {
    return aggregateFields;
  }

  public void setAggregateFields(List<GisGroupByAggregateField> aggregateFields) {
    this.aggregateFields = aggregateFields;
  }

  public void allocate(int sizegroup, int nrfields) {
    groupFields = new ArrayList<GisGroupByField>();
    for (int i = 0; i < sizegroup; i++) {
      groupFields.add(new GisGroupByField());
    }
    aggregateFields = new ArrayList<GisGroupByAggregateField>();
    for (int i = 0; i < nrfields; i++) {
      aggregateFields.add(new GisGroupByAggregateField());
    }
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  public static final int getType(String desc) {
    for (int i = 0; i < typeGroupCode.length; i++) {
      if (typeGroupCode[i].equalsIgnoreCase(desc)) {
        return i;
      }
    }
    for (int i = 0; i < typeGroupLongDesc.length; i++) {
      if (typeGroupLongDesc[i].equalsIgnoreCase(desc)) {
        return i;
      }
    }
    return 0;
  }

  public static final String getTypeDesc(int i) {
    if (i < 0 || i >= typeGroupCode.length) {
      return null;
    }
    return typeGroupCode[i];
  }

  public static final String getTypeDescLong(int i) {
    if (i < 0 || i >= typeGroupLongDesc.length) {
      return null;
    }
    return typeGroupLongDesc[i];
  }

  public void setDefault() {
    directory = "%%java.io.tmpdir%%";
    prefix = "grp";

    passAllRows = false;
    aggregateIgnored = false;
    aggregateIgnoredField = null;

    int sizegroup = 0;
    int nrfields = 0;

    allocate(sizegroup, nrfields);
  }

  @Override
  public void getFields(
      IRowMeta r,
      String origin,
      IRowMeta[] info,
      TransformMeta nextStep,
      IVariables space,
      IHopMetadataProvider metaStore) {
    // re-assemble a new row of metadata
    //
    IRowMeta fields = new RowMeta();

    if (!passAllRows) {
      // Add the grouping fields in the correct order...
      //
      for (int i = 0; i < groupFields.size(); i++) {
        IValueMeta valueMeta = r.searchValueMeta(groupFields.get(i).getName());
        if (valueMeta != null) {
          fields.addValueMeta(valueMeta);
        }
      }
    } else {
      // Add all the original fields from the incoming row meta
      //
      fields.addRowMeta(r);
    }

    // Re-add aggregates
    //
    for (int i = 0; i < aggregateFields.size(); i++) {
      GisGroupByAggregateField aggField = aggregateFields.get(i);
      int aggregateTypeI = getType(aggField.getTypeDesc());
      IValueMeta subj = r.searchValueMeta(aggField.getSubjectField());
      if (subj != null || aggregateTypeI == TYPE_GROUP_COUNT_ANY) {
        String value_name = aggField.getAggregateField();
        int value_type = IValueMeta.TYPE_NONE;
        int length = -1;
        int precision = -1;

        switch (aggregateTypeI) {
          case TYPE_GROUP_SUM:
          case TYPE_GROUP_AVERAGE:
          case TYPE_GROUP_CUMULATIVE_SUM:
          case TYPE_GROUP_CUMULATIVE_AVERAGE:
          case TYPE_GROUP_FIRST:
          case TYPE_GROUP_LAST:
          case TYPE_GROUP_FIRST_INCL_NULL:
          case TYPE_GROUP_LAST_INCL_NULL:
          case TYPE_GROUP_MIN:
          case TYPE_GROUP_MAX:
            value_type = subj.getType();
            break;
          case TYPE_GROUP_COUNT_DISTINCT:
          case TYPE_GROUP_COUNT_ANY:
          case TYPE_GROUP_COUNT_ALL:
            value_type = IValueMeta.TYPE_INTEGER;
            break;
          case TYPE_GROUP_CONCAT_COMMA:
            value_type = IValueMeta.TYPE_STRING;
            break;
          case TYPE_GROUP_STANDARD_DEVIATION:
          case TYPE_GROUP_MEDIAN:
            value_type = IValueMeta.TYPE_NUMBER;
            break;
          case TYPE_GROUP_CONCAT_STRING:
            value_type = IValueMeta.TYPE_STRING;
            break;

            // GIS : Ajout d'opérateurs d'aggrégation
          case TYPE_GROUP_GEOMETRY_UNION:
          case TYPE_GROUP_GEOMETRY_EXTENT:
          case TYPE_GROUP_GEOMETRY_AGG:
          case TYPE_GROUP_GEOMETRY_DISSOLVE:
            value_type = ValueMetaGeometry.TYPE_GEOMETRY;
            break;

          default:
            break;
        }

        // Change type from integer to number in case off averages for
        // cumulative average
        //
        if (aggregateTypeI == TYPE_GROUP_CUMULATIVE_AVERAGE
            && value_type == IValueMeta.TYPE_INTEGER) {
          value_type = IValueMeta.TYPE_NUMBER;
          precision = -1;
          length = -1;
        } else if (aggregateTypeI == TYPE_GROUP_COUNT_ALL
            || aggregateTypeI == TYPE_GROUP_COUNT_DISTINCT
            || aggregateTypeI == TYPE_GROUP_COUNT_ANY) {
          length = IValueMeta.DEFAULT_INTEGER_LENGTH;
          precision = 0;
        } else if (aggregateTypeI == TYPE_GROUP_SUM
            && value_type != IValueMeta.TYPE_INTEGER
            && value_type != IValueMeta.TYPE_NUMBER
            && value_type != IValueMeta.TYPE_BIGNUMBER) {
          // If it ain't numeric, we change it to Number
          //
          value_type = IValueMeta.TYPE_NUMBER;
          precision = -1;
          length = -1;
        }

        if (value_type != IValueMeta.TYPE_NONE) {
          IValueMeta v = null;
          try {
            v = ValueMetaFactory.createValueMeta(value_name, value_type);
          } catch (HopPluginException e) {
            throw new RuntimeException(e);
          }
          v.setOrigin(origin);
          v.setLength(length, precision);
          fields.addValueMeta(v);
        }
      }
    }

    if (passAllRows) {
      // If we pass all rows, we can add a line nr in the group...
      if (addingLineNrInGroup && !lineNrInGroupField.isEmpty()) {
        IValueMeta lineNr = null;
        try {
          lineNr = ValueMetaFactory.createValueMeta(lineNrInGroupField, IValueMeta.TYPE_INTEGER);
        } catch (HopPluginException e) {
          throw new RuntimeException(e);
        }
        lineNr.setLength(IValueMeta.DEFAULT_INTEGER_LENGTH, 0);
        lineNr.setOrigin(origin);
        fields.addValueMeta(lineNr);
      }
    }

    // Now that we have all the fields we want, we should clear the original
    // row and replace the values...
    //
    r.clear();
    r.addRowMeta(fields);
  }

  // Note: getXml() and loadXml() have been removed. Since Apache Hop 2.18,
  // these overrides are no longer called - serialization now happens
  // exclusively via reflection using the
  // @HopMetadataProperty annotations above. CAUTION: .hpl files already
  // saved in the old format do not contain these tags - saving them once
  // more fixes this permanently.

  @Override
  public void check(
      List<ICheckResult> remarks,
      PipelineMeta transMeta,
      TransformMeta stepMeta,
      IRowMeta prev,
      String[] input,
      String[] output,
      IRowMeta info,
      IVariables space,
      IHopMetadataProvider metaStore) {
    CheckResult cr;

    if (input.length > 0) {
      cr =
          new CheckResult(
              ICheckResult.TYPE_RESULT_OK,
              BaseMessages.getString(PKG, "GroupByMeta.CheckResult.ReceivingInfoOK"),
              stepMeta);
      remarks.add(cr);
    } else {
      cr =
          new CheckResult(
              ICheckResult.TYPE_RESULT_ERROR,
              BaseMessages.getString(PKG, "GroupByMeta.CheckResult.NoInputError"),
              stepMeta);
      remarks.add(cr);
    }
  }

  /**
   * @return Returns the directory.
   */
  public String getDirectory() {
    return directory;
  }

  /**
   * @param directory The directory to set.
   */
  public void setDirectory(String directory) {
    this.directory = directory;
  }

  /**
   * @return Returns the prefix.
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * @param prefix The prefix to set.
   */
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  /**
   * @return the addingLineNrInGroup
   */
  public boolean isAddingLineNrInGroup() {
    return addingLineNrInGroup;
  }

  /**
   * @param addingLineNrInGroup the addingLineNrInGroup to set
   */
  public void setAddingLineNrInGroup(boolean addingLineNrInGroup) {
    this.addingLineNrInGroup = addingLineNrInGroup;
  }

  /**
   * @return the lineNrInGroupField
   */
  public String getLineNrInGroupField() {
    return lineNrInGroupField;
  }

  /**
   * @param lineNrInGroupField the lineNrInGroupField to set
   */
  public void setLineNrInGroupField(String lineNrInGroupField) {
    this.lineNrInGroupField = lineNrInGroupField;
  }

  /**
   * @return the alwaysGivingBackOneRow
   */
  public boolean isAlwaysGivingBackOneRow() {
    return alwaysGivingBackOneRow;
  }

  /**
   * @param alwaysGivingBackOneRow the alwaysGivingBackOneRow to set
   */
  public void setAlwaysGivingBackOneRow(boolean alwaysGivingBackOneRow) {
    this.alwaysGivingBackOneRow = alwaysGivingBackOneRow;
  }
}
