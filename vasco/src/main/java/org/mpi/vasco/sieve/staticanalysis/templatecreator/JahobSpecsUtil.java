/***************************************************************
Project name: georeplication
Class file name: JahabSpecsUtil.java
Created at 2:02:18 PM by chengli

Copyright (c) 2013 chengli.
All rights reserved. This program and the accompanying materials
are made available under the terms of the GNU Public License v2.0
which accompanies this distribution, and is available at
http://www.gnu.org/licenses/old-licenses/gpl-2.0.html

Contributors:
    chengli - initial API and implementation

Contact:
    To distribute or use this code requires prior specific permission.
    In this case, please contact chengli@mpi-sws.org.
****************************************************************/

package org.mpi.vasco.sieve.staticanalysis.templatecreator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import org.mpi.vasco.util.annotationparser.Invariant;
import org.mpi.vasco.util.annotationparser.SchemaParser;
import org.mpi.vasco.util.commonfunc.StringOperations;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.CrdtFactory;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.CrdtDataFieldType;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DataField;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class JahobSpecsUtil.
 *
 * @author chengli
 */
public class JahobSpecsUtil {
	
	/** The Constant verifiedCrdtDirPath. */
	public static final String verifiedCrdtDirPath = "/var/tmp/workspace/georeplication/src/util/crdtlib/verifieddatatypes";
	
	/** The Constant timeMeasure. */
	public static final String timeMeasure = "/usr/bin/time";
	
	/** The Constant jahobExecutor. */
	public static final String jahobExecutor = "jahob.opt";
	
	/** The Constant timeOut. */
	public static final String timeOut = " -timeout 10 ";
	
	/** The Constant parameters. */
	public static final String parameters = "-sastvc -hidden";
	
	public static final String templateVerifyParameters = "-printvc sifter -novcsplit -sastvc";
	
	/** The Constant provers. */
	public static final String provers = "-usedp z3 spass:ArithAxioms:OrderAxioms cvcl isa bapa";
	
	
	/** The Constant modifyPrefix. */
	public static final String modifyPrefix = " modifies ";
	
	/** The Constant ensurePrefix. */
	public static final String ensurePrefix = " ensures \"";
	
	/** The Constant requirePrefix. */
	public static final String requirePrefix = " requires \"";
	
	public static final String loopInvPrefix = "inv ";
	
	/** The Constant tableInit. */
	public static final String tableInit = "init";
	
	/** The Constant tableSize. */
	public static final String tableSize = "csize";
	
	/** The Constant tableContents. */
	public static final String tableContents = "contents";
	
	public static final String[] JAHOB_KEYWORDS = {"comment"};
	
	
	public static boolean isJahobKeyWord(String _str) {
		for(int i = 0 ; i < JAHOB_KEYWORDS.length; i++) {
			if(_str.equals(JAHOB_KEYWORDS[i])) {
				return true;
			}
		}
		return false;
	}
	
	public static String getJahobEscapeString(String _str) {
		if(isJahobKeyWord(_str)) {
			return _str + "1";
		}else {
			return _str;
		}
	}
	
	/**
	 * Generate field specification.
	 *
	 * @param className the class name
	 * @param fieldName the field name
	 * @return the string
	 */
	public static String generateFieldSpecification(String className, String fieldName) {
		String _str = "public encap specvar " + className +"_" +fieldName + " :: \"obj\"\n\t";
		_str = _str + "vardefs \""+className +"_" +fieldName+" == "+getJahobEscapeString(fieldName)+"\"";
		return _str;
	}
	
	/**
	 * Gets the jahob record field.
	 *
	 * @param className the class name
	 * @param fieldName the field name
	 * @return the jahob record field
	 */
	public static final String getJahobRecordField(String className, String fieldName) {
		return className + "_" + fieldName;
	}
	
	/**
	 * Gets the jahob record field lts.
	 *
	 * @param className the class name
	 * @param df the df
	 * @return the jahob record field lts
	 */
	public static final String getJahobRecordFieldLts(String className, DataField df) {
		String _str = className + "_" + df.get_Data_Field_Name() + "..";
		_str += getJahobSpecvarLogicalTimestampString(df.get_Crdt_Data_Type());
		return _str;
	}
	
	/**
	 * Gets the modify field.
	 *
	 * @param className the class name
	 * @param fieldName the field name
	 * @return the modify field
	 */
	public static String getModifyField(String className, String fieldName) {
		return "\""+className + "_" + fieldName+"\"";
	}
	
	/**
	 * Gets the modify field. for used in the same class
	 *
	 * @param className the class name
	 * @param fieldName the field name
	 * @param crdtType the crdt type
	 * @return the modify field
	 */
	public static String getModifyField(CrdtDataFieldType crdtType) {
		String crdtName = CrdtFactory.getProperCrdtObject(crdtType, "");
		if(CrdtFactory.isLwwLogicalTimestamp(crdtType)) {
			return "\"" + crdtName + "." +getJahobSpecvarLogicalTimestampString(crdtType)+"\"";
		}else {
			String _str = "\""+crdtName+"."+getJahobSpecvarValueString(crdtType)+"\"";
			if(CrdtFactory.isLwwType(crdtType)) {
				return _str + ",\""+crdtName+"."+getJahobSpecvarLogicalTimestampString(crdtType)+"\"";
			}else {
				return _str + ",\""+crdtName+"."+getJahobSpecvarDeltaString(crdtType)+"\"";
			}
		}
	}
	//for use in the different class
	//format: RecordType.RecordType_AttributeName.Value
	public static String getModifyFieldFromDifferentClass(String className, String fieldName, CrdtDataFieldType crdtType) {
		String _str = "";
		if(CrdtFactory.isLwwLogicalTimestamp(crdtType)) {
			return _str + className + "." + className + "_" + fieldName+"."+getJahobSpecvarLogicalTimestampString(crdtType)+"\"";
		}else {
			_str = "\""+className + "." + className + "_" + fieldName+"."+getJahobSpecvarValueString(crdtType)+"\"";
			if(CrdtFactory.isLwwType(crdtType)) {
				return _str + ",\""+className + "." + className + "_" + fieldName+"."+getJahobSpecvarLogicalTimestampString(crdtType)+"\"";
			}else {
				return _str + ",\""+className + "." + className + "_" +  fieldName+"."+getJahobSpecvarDeltaString(crdtType)+"\"";
			}
		}
	}
	
	/**
	 * Gets the modify field for lww logical timestamp.
	 *
	 * @param className the class name
	 * @param fieldName the field name
	 * @param crdtType the crdt type
	 * @return the modify field for lww logical timestamp
	 */
	public static String getModifyFieldForLwwLogicalTimestamp(String className, String fieldName, CrdtDataFieldType crdtType) {
		return "\""+className + "_" + fieldName+".."+getJahobSpecvarLogicalTimestampString(crdtType)+"\"";
	}
	
	/**
	 * Gets the ensure field equal.
	 *
	 * @param className the class name
	 * @param fieldName the field name
	 * @param varName the var name
	 * @return the ensure field equal
	 */
	public static String getEnsureFieldEqual(String className, String fieldName, String varName) {
		return className + "_"+ fieldName +" = "+ varName;
	}
	
	public static String getLessthanAndEqualStr(String var1, String var2) {
		return var1 +" <= "+ var2;
	}
	
	public static String getGreaterStr(String var1, String var2) {
		return var1 +" > "+ var2;
	}
	
	/**
	 * Gets the require not null clause.
	 *
	 * @param argName the arg name
	 * @return the require not null clause
	 */
	public static String getRequireNotNullClause(String argName) {
		return argName + " ~= null";
	}
	
	/**
	 * Gets the class parameter.
	 *
	 * @param className the class name
	 * @return the class parameter
	 */
	public static String getClassParameter(String className) {
		return " -class " + className;
	}
	
	/**
	 * Gets the required class string.
	 *
	 * @param classFilePath the class file path
	 * @param fieldList the field list
	 * @return the required class string
	 */
	public static String getRequiredClassString(String classFilePath, List<DataField> fieldList) {
		String classStr = classFilePath + " " + getRequiredPrimitiveTypeClassString(fieldList);
		return classStr;
	}
	
	/**
	 * Gets the required primitive type class string.
	 *
	 * @param fieldList the field list
	 * @return the required primitive type class string
	 */
	public static String getRequiredPrimitiveTypeClassString(List<DataField> fieldList) {
		String classStr = " ";
		List<String> crdtTypeStrs = new ArrayList<String>();
		for(DataField df : fieldList) {
			String crdtImplTypeString = CrdtFactory.getProperCrdtObject(df.get_Crdt_Data_Type(), df.get_Data_Type());
			if(!StringOperations.isStringSeen(crdtImplTypeString, crdtTypeStrs)) {
				classStr += verifiedCrdtDirPath + "/" +crdtImplTypeString +".java ";
				crdtTypeStrs.add(crdtImplTypeString);
			}
		}
		return classStr;
	}
	
	/**
	 * Gets the verification exec command.
	 *
	 * @param className the class name
	 * @param classFilePath the class file path
	 * @param fieldList the field list
	 * @return the verification exec command
	 */
	public static String getVerificationExecCommand(String className, String classFilePath, List<DataField> fieldList) {
		String _str = timeMeasure +" " +jahobExecutor +" " + getRequiredClassString(classFilePath, fieldList) + " ";
		_str+= parameters + " " + timeOut + " " + getClassParameter(className) +" " + provers ;
		return _str;
	}
	
	/**
	 * Gets the jahob specvar value string.
	 *
	 * @param crdtType the crdt type
	 * @return the jahob specvar value string
	 */
	public static String getJahobSpecvarValueString(CrdtDataFieldType crdtType) {
		String _str = "_value";
		switch(crdtType){
		case LWWINTEGER:
			return "li"+_str;
		case LWWFLOAT:
			return "lf"+_str;
		case LWWDOUBLE:
			return "ld"+_str;
		case LWWSTRING:
			return "ls"+_str;
		case LWWDATETIME:
			return "ldt"+_str;
		case LWWBOOLEAN:
		case LWWDELETEDFLAG:
			return "lb"+_str;
		case NUMDELTAINTEGER:
			return "ndi"+_str;
		case NUMDELTAFLOAT:
			return "ndf"+_str;
		case NUMDELTADOUBLE:
			return "ndd"+_str;
		case NUMDELTADATETIME:
			return "nddt"+_str;
		case NORMALINTEGER:
			return "ni"+_str;
		case NORMALBOOLEAN:
			return "nb"+_str;
		case NORMALFLOAT:
			return "nf"+_str;
		case NORMALDOUBLE:
			return "nd"+_str;
		case NORMALSTRING:
			return "ns"+_str;
		case NORMALDATETIME:
			return "ndt"+_str;
			default:
				System.err.println("not reachable " + crdtType);
				throw new RuntimeException("not such crdt type");
		}
	}
	
	/**
	 * Gets the jahob specvar delta string.
	 *
	 * @param crdtType the crdt type
	 * @return the jahob specvar delta string
	 */
	public static String getJahobSpecvarDeltaString(CrdtDataFieldType crdtType) {
		String _str = "_delta";
		switch(crdtType){
		case NUMDELTAINTEGER:
			return "ndi"+_str;
		case NUMDELTAFLOAT:
			return "ndf"+_str;
		case NUMDELTADOUBLE:
			return "ndd"+_str;
		case NUMDELTADATETIME:
			return "nddt"+_str;
			default:
				System.err.println("not reachable " + crdtType);
				throw new RuntimeException("not such crdt type");
		}
	}
	
	/**
	 * Gets the jahob specvar logical timestamp string.
	 *
	 * @param crdtType the crdt type
	 * @return the jahob specvar logical timestamp string
	 */
	public static String getJahobSpecvarLogicalTimestampString(CrdtDataFieldType crdtType) {
		String _str = "_lts";
		switch(crdtType){
		case LWWINTEGER:
			return "li"+_str;
		case LWWFLOAT:
			return "lf"+_str;
		case LWWDOUBLE:
			return "ld"+_str;
		case LWWSTRING:
			return "ls"+_str;
		case LWWDATETIME:
			return "ldt"+_str;
		case LWWBOOLEAN:
		case LWWDELETEDFLAG:
			return "lb"+_str;
		case LWWLOGICALTIMESTAMP:
			return "lts_ts";
			default:
				System.err.println("not reachable " + crdtType);
				throw new RuntimeException("not such crdt type");
		}
	}
	
	/**
	 * Gets the jahob specvar logical timestamp value string.
	 *
	 * @param crdtType the crdt type
	 * @return the jahob specvar logical timestamp value string
	 */
	public static String getJahobSpecvarLogicalTimestampValueString(CrdtDataFieldType crdtType) {
		String _str = "_lts..lts_ts";
		switch(crdtType){
		case LWWINTEGER:
			return "li"+_str;
		case LWWFLOAT:
			return "lf"+_str;
		case LWWDOUBLE:
			return "ld"+_str;
		case LWWSTRING:
			return "ls"+_str;
		case LWWDATETIME:
			return "ldt"+_str;
		case LWWBOOLEAN:
		case LWWDELETEDFLAG:
			return "lb"+_str;
		case LWWLOGICALTIMESTAMP:
			return "lts_ts";
			default:
				System.err.println("not reachable " + crdtType);
				throw new RuntimeException("not such crdt type");
		}
	}
	
	/**
	 * Gets the ensure clause for number delta.
	 *
	 * @param className the class name
	 * @param df the df
	 * @param argName the arg name
	 * @return the ensure clause for number delta
	 */
	public static String getEnsureClauseForNumberDelta(String className, DataField df, String argName) {
		CrdtDataFieldType crdtType = df.get_Crdt_Data_Type();
		String _str = "(" + className + "_" + df.get_Data_Field_Name() +".." +  getJahobSpecvarValueString(crdtType);
		_str +=" = " + className + "_" + df.get_Data_Field_Name() +"..(old " +  getJahobSpecvarValueString(crdtType) +")";
		_str += " + " + argName + ".." + getJahobSpecvarDeltaString(crdtType) + ")";
		return _str;
	}
	
	/**
	 * Gets the ensure clause for lww logical timestamp.
	 *
	 * @param className the class name
	 * @param df the df
	 * @param argName the arg name
	 * @return the ensure clause for lww logical timestamp
	 */
	public static String getEnsureClauseForLwwLogicalTimestamp(String className, DataField df, String argName) {
		CrdtDataFieldType crdtType = df.get_Crdt_Data_Type();
		String _str = "(old " + className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarLogicalTimestampValueString(crdtType);
		_str += "<=" + argName + ".." + getJahobSpecvarLogicalTimestampValueString(crdtType);
		_str += "-->" +  className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarLogicalTimestampString(crdtType);
		_str += "=" + argName + ".." + getJahobSpecvarLogicalTimestampString(crdtType) + ")";
		_str += "&\n\t";
		_str += "(old " + className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarLogicalTimestampValueString(crdtType);
		_str += ">" + argName + ".." + getJahobSpecvarLogicalTimestampValueString(crdtType);
		_str += "-->" +  className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarLogicalTimestampString(crdtType);
		_str += "=" + " old " +className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarLogicalTimestampString(crdtType) + ")";
		return _str;
	}
	
	/**
	 * Gets the ensure clause for lww.
	 *
	 * @param className the class name
	 * @param df the df
	 * @param argName the arg name
	 * @param lwwdf the lwwdf
	 * @param lwwtsName the lwwts name
	 * @return the ensure clause for lww
	 */
	public static String getEnsureClauseForLww(String className, DataField df, String argName, DataField lwwdf, 
			String lwwtsName) {
		CrdtDataFieldType crdtType = df.get_Crdt_Data_Type();
		CrdtDataFieldType tsCrdtType = lwwdf.get_Crdt_Data_Type();
		String _str = "(old " + className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarLogicalTimestampValueString(crdtType);
		_str += "<=" + lwwtsName + ".." + getJahobSpecvarLogicalTimestampValueString(tsCrdtType);
		_str += "-->" + "(" + className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarValueString(crdtType);
		_str += "=" + argName + ".." + getJahobSpecvarValueString(crdtType) + ")";
		_str += " & \n\t";
		_str += "(" + className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarLogicalTimestampString(crdtType);
		_str += "=" + lwwtsName + "))";
		_str += "&\n\t";
		_str += "(old " + className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarLogicalTimestampValueString(crdtType);
		_str += ">" + lwwtsName + ".." + getJahobSpecvarLogicalTimestampValueString(tsCrdtType);
		_str += "-->" + "(" + className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarValueString(crdtType);
		_str += "=" + " old " +className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarValueString(crdtType) + ")";
		_str += " &\n\t";
		_str += "(" + className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarLogicalTimestampString(crdtType);
		_str += "=" + " old " +className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarLogicalTimestampString(crdtType) + "))";
		return _str;
	}
	
	public static String getEnsureClauseForLwwDeletedFlag(String className, DataField df, DataField lwwdf, 
			String lwwtsName, String trueValue) {
		CrdtDataFieldType crdtType = df.get_Crdt_Data_Type();
		CrdtDataFieldType tsCrdtType = lwwdf.get_Crdt_Data_Type();
		String _str = "(old " + className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarLogicalTimestampValueString(crdtType);
		_str += "<=" + lwwtsName + ".." + getJahobSpecvarLogicalTimestampValueString(tsCrdtType);
		_str += "-->" + "(" + className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarValueString(crdtType);
		_str += "=" + trueValue + ")";
		_str += " & \n\t";
		_str += "(" + className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarLogicalTimestampString(crdtType);
		_str += "=" + lwwtsName + "))";
		_str += "&\n\t";
		_str += "(old " + className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarLogicalTimestampValueString(crdtType);
		_str += ">" + lwwtsName + ".." + getJahobSpecvarLogicalTimestampValueString(tsCrdtType);
		_str += "-->" + "(" + className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarValueString(crdtType);
		_str += "=" + " old " +className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarValueString(crdtType) + ")";
		_str += " &\n\t";
		_str += "(" + className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarLogicalTimestampString(crdtType);
		_str += "=" + " old " +className + "_" + df.get_Data_Field_Name() +".." +getJahobSpecvarLogicalTimestampString(crdtType) + "))";
		return _str;
	}
	
	//specifications for tables
	
	/**
	 * Gets the table init specs.
	 *
	 * @param tableName the table name
	 * @return the table init specs
	 */
	public static String getTableInitSpecs(String tableName) {
		String tableInitStr = tableName + "_" + tableInit;
		return "public ghost specvar " + tableInitStr + " :: \"bool\" = \"False\";";
	}
	
	/**
	 * Gets the table size specs.
	 *
	 * @param tableName the table name
	 * @return the table size specs
	 */
	public static String getTableSizeSpecs(String tableName) {
		String tableSizeStr = tableName + "_" + tableSize;
		return "public encap specvar " + tableSizeStr + " :: int;";
	}
	
	/**
	 * Gets the table size vardefs.
	 *
	 * @param tableName the table name
	 * @return the table size vardefs
	 */
	public static String getTableSizeVardefs(String tableName) {
		return "vardefs \""+tableName + "_" + tableSize+" == size"+"\"";
	}
	
	/**
	 * Gets the table contents specs.
	 *
	 * @param tableName the table name
	 * @return the table contents specs
	 */
	public static String getTableContentsSpecs(String tableName) {
		String tableContentsStr = tableName + "_" + tableContents;
		return "public encap specvar " + tableContentsStr + " :: \"(int * obj) set\" = \"{}\";";
	}
	
	/**
	 * Gets the table contents vardefs.
	 *
	 * @param tableName the table name
	 * @return the table contents vardefs
	 */
	public static String getTableContentsVardefs(String tableName) {
		return "vardefs \""+tableName + "_" + tableContents+" == {(i, elem). 0 <= i & i < size & elem = table.[i]}"+"\"";
	}
	
	//specifications for table constructor
	/**
	 * Gets the table not init.
	 *
	 * @param tableName the table name
	 * @return the table not init
	 */
	public static String getTableNotInit(String tableName) {
		return "~" + tableName + "_" + tableInit;
	}
	
	/**
	 * Gets the table init.
	 *
	 * @param tableName the table name
	 * @return the table init
	 */
	public static String getTableInit(String tableName) {
		return tableName + "_" + tableInit;
	}
	
	
	public static String getTableContents(String tableName) {
		return tableName + "_" + tableContents;
	}
	
	/**
	 * Gets the table contents empty.
	 *
	 * @param tableName the table name
	 * @return the table contents empty
	 */
	public static String getTableContentsEmpty(String tableName) {
		return getTableContents(tableName) + " = {}";
	}
	
	/**
	 * Gets the table size.
	 *
	 * @param tableName the table name
	 * @return the table size
	 */
	public static String getTableSize(String tableName) {
		return tableName +"_" + tableSize;
	}
	
	/**
	 * Gets the table size zero.
	 *
	 * @param tableName the table name
	 * @return the table size zero
	 */
	public static String getTableSizeZero(String tableName) {
		return getTableSize(tableName) + " = 0";
	}
	
	/**
	 * Gets the size equal result.
	 *
	 * @param tableName the table name
	 * @return the size equal result
	 */
	public static String getSizeEqualResult(String tableName) {
		return "result = " + tableName + "_"  + tableSize;
	}
	
	/**
	 * Gets the table required class string.
	 *
	 * @param classFilePath the class file path
	 * @param recordFilePath the record file path
	 * @param fieldList the field list
	 * @return the table required class string
	 */
	public static String getTableRequiredClassString(String classFilePath, String recordFilePath, List<DataField> fieldList) {
		String classStr = classFilePath + " " + recordFilePath + " ";
		classStr += getRequiredPrimitiveTypeClassString(fieldList);
		return classStr;
	}
	
	/**
	 * Gets the table verification exec command.
	 *
	 * @param className the class name
	 * @param classFilePath the class file path
	 * @param recordFilePath the record file path
	 * @param fieldList the field list
	 * @return the table verification exec command
	 */
	public static String getTableVerificationExecCommand(String className, 
			String classFilePath, String recordFilePath, List<DataField> fieldList) {
		String _str = timeMeasure +" " +jahobExecutor +" " + getTableRequiredClassString(classFilePath, recordFilePath, fieldList) + " ";
		_str+= parameters + " " + timeOut + " " + getClassParameter(className) +" " + provers ;
		return _str;
	}
	
	/**
	 * Gets the table init facilities.
	 *
	 * @param tableName the table name
	 * @return the table init facilities
	 */
	public static String getTableInitFacilities(String tableName) {
		String _str= "\t//: \"" + getTableInit(tableName) + "\" := \"True\";\n";
		_str +=  "\t//: \"" + getTableSize(tableName) + "\" := \"0\";";
		return _str;
	}
	
	public static String getExistencePrefix(String tableName) {
		String _str = "EX j v. 0 <= j & j < " + getTableSize(tableName) + " & (j, v) : ";
		_str += getTableContents(tableName);
		return _str;
	}
	
	public static String getExistenceStrByPrimaryKeyList(String tableName, String recordName, List<DataField> pkList) {
		String _str ="(" + getExistencePrefix(tableName);
		for(int i = 0 ; i < pkList.size(); i++) {
			DataField df = pkList.get(i);
			_str += " & " + getTableRecordEnsureFieldEqual(recordName, df.get_Data_Field_Name(), "pk" + i);
		}
		return _str + ")";
	}
	
	public static String getNotExistencePrefix(String tableName) {
		String _str = "~EX j v. 0 <= j & j < " + getTableSize(tableName) + " & (j, v) : ";
		_str += getTableContents(tableName);
		return _str;
	}
	
	public static String getTableRecordEnsureFieldEqual(String recordName, String fieldName, String varName) {
		return "v.." + recordName + "_"+ fieldName +" = "+ varName;
	}
	
	public static String getTableRecordEnsureFieldEqualSymmetric(String recordName, String fieldName, String varName) {
		return "v.." + recordName + "_"+ fieldName +" = "+ varName + ".." +recordName + "_"+ fieldName;
	}
	
	public static String getGetRecordResultEqual(boolean existed) {
		if(existed) {
			return "result = v";
		}else {
			return "result = null";
		}
	}
	
	public static String getGetRecordLoopInvariant(String indexName, String recordName, List<DataField> primaryKeyList) {
		String _str = " 0 <= " + indexName;
		_str += " & " + "\n\t(ALL j. 0 <= j & j < " + indexName + " -->(";
		for(int i = 0 ; i < primaryKeyList.size(); i++) {
			DataField df = primaryKeyList.get(i);
			_str += "\n\t\t( table.[j].."+ recordName + "_" + df.get_Data_Field_Name();
			_str += " ~= " + "pk" + i +" ) \\<or>";
		}
		_str = _str.substring(0, _str.length() - 5);
		_str += "))";
		return _str;
	}
	
	public static String getLogicalTimestampLessthanAndEqual(String recordName, String fieldName,
			CrdtDataFieldType crdtType, String lwwts) {
		String lwwtsValueStr = lwwts+".."+getJahobSpecvarLogicalTimestampValueString(CrdtDataFieldType.LWWLOGICALTIMESTAMP);
		String fieldLwwTsValueStr = "v.." + recordName + "_" + fieldName + ".." +getJahobSpecvarLogicalTimestampValueString(crdtType);
		String _str = getLessthanAndEqualStr(fieldLwwTsValueStr, lwwtsValueStr);
		return _str;
	}
	
	public static String getLogicalTimestampGreater(String recordName, String fieldName,
			CrdtDataFieldType crdtType, String lwwts) {
		String lwwtsValueStr = lwwts+".."+getJahobSpecvarLogicalTimestampValueString(CrdtDataFieldType.LWWLOGICALTIMESTAMP);
		String fieldLwwTsValueStr = "v.." + recordName + "_" + fieldName + ".." +getJahobSpecvarLogicalTimestampValueString(crdtType);
		String _str = getGreaterStr(fieldLwwTsValueStr, lwwtsValueStr);
		return _str;
	}
	
	public static String getEnsureClauseForLwwDeleteFlagForTable(String className, String fieldName,  CrdtDataFieldType crdtType,
			String lwwtsName) {
		CrdtDataFieldType tsCrdtType = CrdtDataFieldType.LWWLOGICALTIMESTAMP;
		String _str = "(old " + className + "_" + fieldName +".." +getJahobSpecvarLogicalTimestampValueString(crdtType);
		_str += "<=" + lwwtsName + ".." + getJahobSpecvarLogicalTimestampValueString(tsCrdtType);
		_str += "-->" + "(" + className + "_" + fieldName +".." +getJahobSpecvarValueString(crdtType);
		_str += "=" + 1 + ")";
		_str += " & \n\t";
		_str += "(" + className + "_" + fieldName +".." +getJahobSpecvarLogicalTimestampString(crdtType);
		_str += "=" + lwwtsName + "))";
		_str += "&\n\t";
		_str += "(old " + className + "_" + fieldName +".." +getJahobSpecvarLogicalTimestampValueString(crdtType);
		_str += ">" + lwwtsName + ".." + getJahobSpecvarLogicalTimestampValueString(tsCrdtType);
		_str += "-->" + "(" + className + "_" + fieldName +".." +getJahobSpecvarValueString(crdtType);
		_str += "=" + " old " +className + "_" + fieldName +".." +getJahobSpecvarValueString(crdtType) + ")";
		_str += " &\n\t";
		_str += "(" + className + "_" + fieldName +".." +getJahobSpecvarLogicalTimestampString(crdtType);
		_str += "=" + " old " +className + "_" + fieldName +".." +getJahobSpecvarLogicalTimestampString(crdtType) + "))";
		return _str;
	}
	
	//for unique insert
	public static String getRecordExistStr(String tableName, String arg0) {
		String _str = "(EX j. 0 <= j & j < " + getTableSize(tableName) + " & (j, "+arg0+") : ";
		_str += getTableContents(tableName) + ")";
		return _str;
	}
	
	public static String getRecordNotExistStr(String tableName, String arg0) {
		String _str = "~" + getRecordExistStr(tableName, arg0);
		return _str;
	}
	
	public static String getUniqueInsertRequireSpecs(String tableName, String arg0) {
		String _str = getTableInit(tableName) + " & " + getRequireNotNullClause(arg0) + " &";
		_str += getRecordNotExistStr(tableName, arg0);
		return _str;
	}
	
	public static String getModifyContentsAndSize(String tableName) {
		String _str = "\""+getTableContents(tableName) + "\"," + "\"" + getTableSize(tableName) + "\"";
		return _str;
	}
	
	public static String getEnsureSizeIncreaseByOne(String tableName) {
		String _str = getTableSize(tableName) + " = " + " old " + getTableSize(tableName) + " + 1";
		return _str;
	}
	
	public static String getEnsureContentEnlargedByOne(String tableName, String arg0) {
		//String _str = " ( old " + getTableSize(tableName) + ", " + arg0 +")" + "~:" + " old " + getTableContents(tableName); 
		//_str += " & " + getTableContents(tableName) + " = ";
		String _str = getTableContents(tableName) + " = ";
		_str += " old " + getTableContents(tableName) + " Un " + "{ (old "  + getTableSize(tableName) + "," + arg0 +")}";
		return _str;
	}
	
	public static String getUniqueInsertLoopInvStr(String tableName, String indexName, String newTableName) {
		String _str = " 0 <= " + indexName;
		_str += " & " + "\n\t(ALL j. 0 <= j & j < " + indexName + " -->(";
		_str += "table.[j] = " + newTableName +".[j]";
		_str += "))";
		return _str;
	}
	
	//specifications for insert
	public static String getInsertRequireSpecs(String tableName, String arg0) {
		return getUniqueInsertRequireSpecs(tableName, arg0);
	}
	
	public static String getInsertModifySpecs(String tableName, List<DataField> dfList) {
		String _str = getModifyContentsAndSize(tableName);
		for(int i = 0 ; i < dfList.size(); i++) {
			DataField df = dfList.get(i);
			if(!df.is_Primary_Key()) {
				CrdtDataFieldType crdtType = df.get_Crdt_Data_Type();
				_str += "," + getModifyField(crdtType);
			}
		}
		return _str;
	}
	
	public static String getInsertEnsure(String tableName, String recordName, String existName,
			String argName, List<DataField> dfList) {
		String _str = "";
		String exist = getExistenceStrByGivenParameter(tableName, recordName, argName, dfList) + " -->";
		exist += "(" +getEnsureClauseForAllUpdateFields(recordName,
				existName,  argName, dfList) + "))";
		//if not exists insert please
		String nonExist = "(~" + getExistenceStrByGivenParameter(tableName, recordName, argName, dfList) + "))" + " --> ";
		nonExist += "(" + getEnsureSizeIncreaseByOne(tableName) + ") & (" + getEnsureContentEnlargedByOne(
				tableName, argName) + ")";
		_str = exist + " \\<or> " + nonExist;
		return _str;
	}
	
	public static DataField getLwwLogicalTimestamp(List<DataField> dfList) {
		for(DataField df : dfList) {
			if(CrdtFactory.isLwwLogicalTimestamp(df.get_Crdt_Data_Type())) {
				return df;
			}
		}
		return null;
	}
	
	public static String getEnsureClauseForLwwFieldsTable(String recordName, String existName, 
			String argName, List<DataField> dfList) {
		String _str = "";
		DataField logicalTs = getLwwLogicalTimestamp(dfList);
		CrdtDataFieldType tsCrdtType = logicalTs.get_Crdt_Data_Type();
		for(int i = 0; i < dfList.size(); i++) {
			DataField df = dfList.get(i);
			CrdtDataFieldType crdtType = df.get_Crdt_Data_Type();
			String fieldName = df.get_Data_Field_Name();
			String existFieldPrefix = existName + ".."+recordName + "_" + fieldName;
			String argFieldPrefix = argName + ".."+recordName + "_" + fieldName;
			String tsFieldPrefix = argName + ".." + recordName + "_" + logicalTs.get_Data_Field_Name();
			if(CrdtFactory.isLwwType(crdtType)) {
				_str += "(old " + existFieldPrefix +".." +getJahobSpecvarLogicalTimestampValueString(crdtType);
				_str += "<=" + tsFieldPrefix+ ".." + getJahobSpecvarLogicalTimestampValueString(tsCrdtType);
				_str += "-->" + "(" + existFieldPrefix +".." +getJahobSpecvarValueString(crdtType);
				_str += "=" + argFieldPrefix +".." +getJahobSpecvarValueString(crdtType)+ ")";
				_str += " & \n\t";
				_str += "(" + existFieldPrefix +".." +getJahobSpecvarLogicalTimestampString(crdtType);
				_str += "=" + tsFieldPrefix +"))";
				_str += "&\n\t";
				_str += "(old " +existFieldPrefix +".." +getJahobSpecvarLogicalTimestampValueString(crdtType);
				_str += ">" + tsFieldPrefix + ".." + getJahobSpecvarLogicalTimestampValueString(tsCrdtType);
				_str += "-->" + "(" + existFieldPrefix +".." +getJahobSpecvarValueString(crdtType);
				_str += "=" + " old " +existFieldPrefix +".." +getJahobSpecvarValueString(crdtType) + ")";
				_str += " &\n\t";
				_str += "(" + existFieldPrefix +".." +getJahobSpecvarLogicalTimestampString(crdtType);
				_str += "=" + " old " +existFieldPrefix +".." +getJahobSpecvarLogicalTimestampString(crdtType) + "))" + " &";
			}else {
				if(CrdtFactory.isLwwLogicalTimestamp(crdtType)) {
					_str += "(old " + existFieldPrefix +".." +getJahobSpecvarLogicalTimestampValueString(crdtType);
					_str += "<=" + argFieldPrefix+ ".." + getJahobSpecvarLogicalTimestampValueString(crdtType);
					_str += "-->" ;
					_str += "(" + existFieldPrefix;
					_str += "=" + argFieldPrefix +"))";
					_str += "&\n\t";
					_str += "(old " +existFieldPrefix +".." +getJahobSpecvarLogicalTimestampValueString(crdtType);
					_str += ">" + argFieldPrefix + ".." + getJahobSpecvarLogicalTimestampValueString(crdtType);
					_str += "-->" ;
					_str += "(" + existFieldPrefix;
					_str += "=" + " old " +existFieldPrefix + "))" + " &";
				}
			}
		}
		if(!_str.equals("")) {
			_str = _str.substring(0, _str.length() - 1);
		}
		return _str;
	}
	
	public static String getEnsureClauseForDeltaFieldsTable(String recordName,
			String existName, String argName, List<DataField> dfList) {
		String _str = "";
		for(int i = 0; i < dfList.size(); i++) {
			DataField df = dfList.get(i);
			CrdtDataFieldType crdtType = df.get_Crdt_Data_Type();
			if(CrdtFactory.isNumberDelta(crdtType)) {
				String existRecordFieldStr = existName + ".."+recordName;
				String argRecordFieldStr = argName + ".."+recordName;
				_str = getEnsureClauseForNumberDelta(existRecordFieldStr, df,argRecordFieldStr) + "&";
			}
		}
		if(!_str.equals("")) {
			_str = _str.substring(0, _str.length() - 1);
		}
		return _str;
	}
	
	public static String getEnsureClauseForAllUpdateFields(String recordName,
			String existName, String argName, List<DataField> dfList) {
		String lwwSpecs = getEnsureClauseForLwwFieldsTable(recordName,
				existName, argName, dfList);
		String deltaSpecs = getEnsureClauseForDeltaFieldsTable(recordName,
				existName, argName, dfList);
		if(lwwSpecs.equals("")) {
			return deltaSpecs;
		}
		
		if(deltaSpecs.equals("")) {
			return lwwSpecs;
		}
		
		return lwwSpecs + " & " +lwwSpecs;
	}
	
	public static String getExistenceStrByGivenParameter(String tableName, String recordName, String argName, 
			List<DataField> dfList) {
		String _str ="(" + getExistencePrefix(tableName);
		for(int i = 0 ; i < dfList.size(); i++) {
			DataField df = dfList.get(i);
			if(df.is_Primary_Key()) {
				_str += " & " + getTableRecordEnsureFieldEqualSymmetric(recordName, df.get_Data_Field_Name(), argName);
			}
		}
		return _str;
	}
	
	//for template specifications
	public static String getAllTableInitAndNotNull(List<SingleVariableDeclaration> tableDeclars) {
		String _str = "";
		for(SingleVariableDeclaration tableDecl : tableDeclars) {
			String tableType = tableDecl.getType().toString();
			String tableVar = tableDecl.getName().toString();
			_str += tableVar + ".." + getTableInit(tableType) +" & ";
			_str += getRequireNotNullClause(tableVar) + " & ";
		}
		if(!_str.equals("")) {
			_str = _str.substring(0, _str.length() - 2);
		}
		return _str;
	}
	
	public static String getAllArgumentsNotNull(List<SingleVariableDeclaration> fieldDeclars) {
		String _str = "";
		for(SingleVariableDeclaration fieldDecl : fieldDeclars) {
			String fieldName = fieldDecl.getName().toString();
			_str += getRequireNotNullClause(fieldName) +" & ";
		}
		if(!_str.equals("")) {
			_str = _str.substring(0, _str.length() - 2);
		}
		return _str;
	}
	
	public static String getModifiesClauseForAllInsertTables(List<String> insertTables) {
		String _str = "";
		for(String insertTable : insertTables) {
			String tableName = insertTable + DatabaseTableClassCreator.getTableSufixString();
			_str += "\"" + tableName +"."+ getTableContents(tableName) + "\"," ;
			_str += "\"" + tableName + "." + getTableSize(tableName) + "\",";
		}
		if(!_str.equals("")) {
			_str = _str.substring(0, _str.length() - 1);
		}
		return _str;
	}
	
	public static String getModifiesClauseForAllDeleteTables(List<String> deleteTables) {
		if(deleteTables.size() > 0) {
			return getModifyField(CrdtDataFieldType.LWWBOOLEAN);
		}else {
			return "";
		}
	}
	
	public static String getModifiesClauseForAllUpdateFields(List<DataField> updateFields) {
		String _str = "";
		int updateFieldIndex = 0;
		for(; updateFieldIndex < updateFields.size(); updateFieldIndex++) {
			DataField df = updateFields.get(updateFieldIndex);
			CrdtDataFieldType crdtType = df.get_Crdt_Data_Type();
			String subModifyStr = getModifyField(crdtType);
			if(!_str.contains(subModifyStr)) {
				_str += subModifyStr + ",";
			}
		}
		if(!_str.equals("")) {
			_str += getModifyField(CrdtDataFieldType.LWWLOGICALTIMESTAMP);
		}
		return _str;
	}
	
	public static String getModifyClauseForTemplate(List<String> insertTables,
			List<String> deleteTables, List<DataField> updateFields) {
		
		String _str = "";
		if(insertTables.size() > 0) {
			_str += getModifiesClauseForAllInsertTables(insertTables) + ",";
		}
		if(deleteTables.size() > 0) {
			_str += getModifiesClauseForAllDeleteTables(deleteTables) + ",";
		}
		if(updateFields.size() > 0) {
			_str += getModifiesClauseForAllUpdateFields(updateFields) + ",";
		}
		if(!_str.equals("")) {
			_str = _str.substring(0, _str.length() - 1);
		}
		return _str;
		
	}
	
	public static String getAllRequireTableRecordClass(String path, SchemaParser sp) {
		String _str = "";
		List<DatabaseTable> dbtList = sp.getAllTableInstances();
		for(DatabaseTable dt : dbtList) {
			if(!dt.is_Readonly()) {
				_str += path + "/" +dt.get_Table_Name() + DatabaseTableClassCreator.getTableSufixString()+".java" + " ";
				_str += path + "/" +dt.get_Table_Name() + DatabaseRecordClassCreator.getRecordSufixString()+".java" + " ";
			}
		}
		return _str;
	}
	
	public static String getAllVerifiedPrimitiveTypeClass() {
		String _str = "";
		for(CrdtDataFieldType crdtType : CrdtDataFieldType.values()) {
			if(crdtType != CrdtDataFieldType.NONCRDTFIELD) {
				String typeName = CrdtFactory.getProperCrdtObject(crdtType, "");
				String fileName =verifiedCrdtDirPath + "/" + typeName + ".java";
				if(!_str.contains(fileName)) {//avoid redundant entries
					_str += fileName + " ";
				}
			}
		}
		return _str;
	}
	
	public static String getTableVerificationExecCommandForTemplate(String fileFullPath, String folderPath, SchemaParser sp) {
		String _str = timeMeasure +" " +jahobExecutor +" " + fileFullPath + " " + getAllRequireTableRecordClass(folderPath, sp) + " ";
		_str += getAllVerifiedPrimitiveTypeClass();
		_str += templateVerifyParameters + " " + timeOut + " " + getClassParameter("shadowOptemplate") +" " + provers ;
		return _str;
	}
	
	public static List<String> findMissingTableForInvariants(HashSet<String> tableNames, List<String> invTableNames){
		List<String> tableNameList = new ArrayList<String>();
		for(String invTableName : invTableNames) {
			if(!tableNames.contains(invTableName)) {
				tableNameList.add(invTableName);
			}
		}
		return tableNameList;
	}
	
	public static String getInvSpecs(List<Invariant> invs, List<SingleVariableDeclaration> tableDecls) {
		String _str = "";
		for(Invariant inv : invs) {
			String invariantStr = inv.getInvariantStr();
			List<String> tableList = inv.getTableList();
			for(SingleVariableDeclaration tableDecl : tableDecls) {
				String tableName = tableDecl.getType().toString();
				String tableArgName = tableDecl.getName().toString();
				for(String tableN : tableList) {
					if(tableName.equals(tableN + DatabaseTableClassCreator.getTableSufixString())) {
						Debug.println("find "+ tableName +" " + tableArgName);
						invariantStr = invariantStr.replaceAll(tableName+"\\.\\.", tableArgName+"\\.\\.");
					}
				}
			}
			_str += "(" + invariantStr + ")" + " & ";
		}
		if(!_str.equals("")) {
			_str = _str.substring(0, _str.length() - 2);
		}
		return _str;
	}
	
}
