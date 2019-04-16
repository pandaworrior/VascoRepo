/********************************************************************
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
********************************************************************/
/**
 * 
 */
package org.mpi.vasco.util.crdtlib.dbannotationtypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Date;

import org.mpi.vasco.util.commonfunc.StringOperations;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.*;
/*
import util.crdtlib.datatypes.primitivetypes.LwwBoolean;
import util.crdtlib.datatypes.primitivetypes.LwwDateTime;
import util.crdtlib.datatypes.primitivetypes.LwwDouble;
import util.crdtlib.datatypes.primitivetypes.LwwFloat;
import util.crdtlib.datatypes.primitivetypes.LwwInteger;
import util.crdtlib.datatypes.primitivetypes.LwwString;
import util.crdtlib.datatypes.primitivetypes.NormalBoolean;
import util.crdtlib.datatypes.primitivetypes.NormalDateTime;
import util.crdtlib.datatypes.primitivetypes.NormalDouble;
import util.crdtlib.datatypes.primitivetypes.NormalFloat;
import util.crdtlib.datatypes.primitivetypes.NormalInteger;
import util.crdtlib.datatypes.primitivetypes.NormalString;
import util.crdtlib.datatypes.primitivetypes.NumberDeltaDateTime;
import util.crdtlib.datatypes.primitivetypes.NumberDeltaDouble;
import util.crdtlib.datatypes.primitivetypes.NumberDeltaFloat;
import util.crdtlib.datatypes.primitivetypes.NumberDeltaInteger;
import util.crdtlib.datatypes.primitivetypes.PrimitiveType;

*/
import org.mpi.vasco.util.debug.Debug;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.CrdtDataFieldType;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DataField;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseFunction;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating Crdt objects.
 */
public class CrdtFactory {

	/**
	 * Gets the proper crdt object.
	 *
	 * @param crdtType the crdt type
	 * @param originalType the original type
	 * @return the proper crdt object
	 */
	public static String getProperCrdtObject(CrdtDataFieldType crdtType, String originalType){
		switch(crdtType){
			case LWWINTEGER:
				return "LwwInteger";
			case LWWFLOAT:
				return "LwwFloat";
			case LWWDOUBLE:
				return "LwwDouble";
			case LWWSTRING:
				return "LwwString";
			case LWWDATETIME:
				return "LwwDateTime";
			case LWWLOGICALTIMESTAMP:
				return "LwwLogicalTimestamp";
			case LWWBOOLEAN:
			case LWWDELETEDFLAG:
				return "LwwBoolean";
			case NUMDELTAINTEGER:
				return "NumberDeltaInteger";
			case NUMDELTAFLOAT:
				return "NumberDeltaFloat";
			case NUMDELTADOUBLE:
				return "NumberDeltaDouble";
			case NUMDELTADATETIME:
				return "NumberDeltaDateTime";
			case NONCRDTFIELD:
				return "Normal"+getNormalDataType(originalType);
			case NORMALINTEGER:
				return "NormalInteger";
			case NORMALBOOLEAN:
				return "NormalBoolean";
			case NORMALFLOAT:
				return "NormalFloat";
			case NORMALDOUBLE:
				return "NormalDouble";
			case NORMALSTRING:
				return "NormalString";
			case NORMALDATETIME:
				return "NormalDateTime";
				default:
					System.err.println("not reachable " + crdtType + " " + originalType);
					throw new RuntimeException("not such crdt type");
		}
	}
	
	/**
	 * Gets the lww logical timestamp crdt type string.
	 *
	 * @return the lww logical timestamp crdt type string
	 */
	public static String getLwwLogicalTimestampCrdtTypeString(){
		return "LwwLogicalTimestamp";
	}
	
	/**
	 * Gets the lww deleted flag.
	 *
	 * @return the lww deleted flag
	 */
	public static String getLwwDeletedFlag(){
		return "LwwBoolean";
	}
	
	/**
	 * Checks if is normal data type.
	 *
	 * @param crdtType the crdt type
	 * @return true, if is normal data type
	 */
	public static boolean isNormalDataType(CrdtDataFieldType crdtType){
		switch(crdtType){
		case NONCRDTFIELD:
		case NORMALINTEGER:
		case NORMALBOOLEAN:
		case NORMALFLOAT:
		case NORMALDOUBLE:
		case NORMALSTRING:
		case NORMALDATETIME:
			return true;
			default:
				return false;
		}
	}
	
	/**
	 * Gets the normal data type.
	 *
	 * @param normalDBType the normal db type
	 * @return the normal data type
	 */
	public static String getNormalDataType(String normalDBType){
		if(normalDBType.toLowerCase().equals("int")){
			return "Integer";
		}else if(normalDBType.toLowerCase().equals("varchar")){
			return "String";
		}else if(normalDBType.toLowerCase().equals("float")){
			return "float";
		}else if(normalDBType.toLowerCase().equals("datetime")){
			return "DateTime";
		}else{
			throw new RuntimeException("not implemented normal db type " + normalDBType);
		}
	}
	
	/**
	 * Checks if is lww type.
	 *
	 * @param crdtType the crdt type
	 * @return true, if is lww type
	 */
	public static boolean isLwwType(CrdtDataFieldType crdtType){
		switch(crdtType){
		case LWWINTEGER:
		case LWWFLOAT:
		case LWWDOUBLE:
		case LWWSTRING:
		case LWWDATETIME:
		case LWWBOOLEAN:
		case LWWDELETEDFLAG:
			return true;
			default:
				return false;
		}
	}
	
	/**
	 * Checks if is number delta.
	 *
	 * @param crdtType the crdt type
	 * @return true, if is number delta
	 */
	public static boolean isNumberDelta(CrdtDataFieldType crdtType) {
		switch(crdtType){
		case NUMDELTAINTEGER:
		case NUMDELTAFLOAT:
		case NUMDELTADOUBLE:
		case NUMDELTADATETIME:
			return true;
			default:
				return false;
		}
	}
	
	/**
	 * Checks if is lww logical timestamp.
	 *
	 * @param crdtType the crdt type
	 * @return true, if is lww logical timestamp
	 */
	public static boolean isLwwLogicalTimestamp(CrdtDataFieldType crdtType){
		switch(crdtType){
		case LWWLOGICALTIMESTAMP:
			return true;
			default:
				return false;
		}
	}
	
	/**
	 * Checks if is lww deleted flag.
	 *
	 * @param crdtType the crdt type
	 * @return true, if is lww deleted flag
	 */
	public static boolean isLwwDeletedFlag(CrdtDataFieldType crdtType){
		switch(crdtType){
		case LWWDELETEDFLAG:
			return true;
			default:
				return false;
		}
	}
	
	//for runtime shadow operation generation
	
	/**
	 * Generate crdt primitive type.
	 *
	 * @param df the df
	 * @param value the value
	 * @param rs the rs
	 * @return the primitive type
	 * @throws SQLException the sQL exception
	 */
	public static PrimitiveType generateCrdtPrimitiveType(DateFormat dateFormat, DataField df, String value, ResultSet rs) throws SQLException {
		switch(df.get_Crdt_Data_Type()) {
		case NONCRDTFIELD:
			throw new RuntimeException("NONCRDT is depreciated");
		case NORMALINTEGER:
			return new NormalInteger(df.get_Data_Field_Name(), Integer.parseInt(value));
		case NORMALBOOLEAN:
			return new NormalBoolean(df.get_Data_Field_Name(), Boolean.parseBoolean(value));
		case NORMALFLOAT:
			return new NormalFloat(df.get_Data_Field_Name(), Float.parseFloat(value));
		case NORMALDOUBLE:
			return new NormalDouble(df.get_Data_Field_Name(), Double.parseDouble(value));
		case NORMALSTRING:
			return new NormalString(df.get_Data_Field_Name(), StringOperations.removeQuotesFromHeadTail(value));
		case NORMALDATETIME:
			return new NormalDateTime(df.get_Data_Field_Name(), DatabaseFunction.convertDateStrToLong(dateFormat, value));
		case LWWINTEGER:
			return new LwwInteger(df.get_Data_Field_Name(), Integer.parseInt(value));
		case LWWFLOAT:
			return new LwwFloat(df.get_Data_Field_Name(), Float.parseFloat(value));
		case LWWDOUBLE:
			return new LwwDouble(df.get_Data_Field_Name(), Double.parseDouble(value));
		case LWWSTRING:
			return new LwwString(df.get_Data_Field_Name(), StringOperations.removeQuotesFromHeadTail(value));
		case LWWDATETIME:
			return new LwwDateTime(df.get_Data_Field_Name(), DatabaseFunction.convertDateStrToLong(dateFormat, value));
		case LWWBOOLEAN:
			return new LwwBoolean(df.get_Data_Field_Name(), Boolean.parseBoolean(value));
		case NUMDELTAINTEGER:
			if(rs != null) {
				Debug.println("result set is not null");
				rs.beforeFirst();
				rs.next();
				int finalIValue =  Integer.parseInt(value);
				int oldIValue = rs.getInt(df.get_Data_Field_Name());
				int iDelta = finalIValue - oldIValue;
				return new NumberDeltaInteger(df.get_Data_Field_Name(), iDelta);
			}else {
				Debug.println("result set is null");
				return new NumberDeltaInteger(df.get_Data_Field_Name(), Integer.parseInt(value));
			}
		case NUMDELTAFLOAT:
			if(rs != null) {
				float finalFValue = -1;
				float oldFValue = -1;
				float fDelta = -1;
				if(value.contains("+")) {
					fDelta = Float.parseFloat(value.substring(value.indexOf('+') + 1));
				}else if(value.contains("-")) {
					fDelta = -1f * Float.parseFloat(value.substring(value.indexOf('-') + 1));
				}else {
					rs.beforeFirst();
					rs.next();
					finalFValue =  Float.parseFloat(value);
					oldFValue = rs.getFloat(df.get_Data_Field_Name());
					fDelta = finalFValue - oldFValue;
				}
				Debug.println("Delta identified is " + fDelta);
				return new NumberDeltaFloat(df.get_Data_Field_Name(), fDelta);
			}else {
				return new NumberDeltaFloat(df.get_Data_Field_Name(), Float.parseFloat(value)); 
			}
		case NUMDELTADOUBLE:
			if(rs != null) {
				rs.beforeFirst();
				rs.next();
				double finalDValue = Double.parseDouble(value);
				double oldDValue = rs.getDouble(df.get_Data_Field_Name());
				double dDelta = finalDValue - oldDValue;
				return new NumberDeltaDouble(df.get_Data_Field_Name(), dDelta);
			}else {
				return new NumberDeltaDouble(df.get_Data_Field_Name(), Double.parseDouble(value));
			}
		case NUMDELTADATETIME:
			if(rs != null) {
				rs.beforeFirst();
				rs.next();
				long finalTValue = DatabaseFunction.convertDateStrToLong(dateFormat, value);
				long oldTValue = DatabaseFunction.convertDateStrToLong(dateFormat, rs.getString(df.get_Data_Field_Name())); 
				long tDelta = finalTValue - oldTValue;
				return new NumberDeltaDateTime(df.get_Data_Field_Name(), tDelta);
			}else {
				return new NumberDeltaDateTime(df.get_Data_Field_Name(), DatabaseFunction.convertDateStrToLong(dateFormat, value));
			}
			default:
				System.err.println("cannot create primitive type" + df.toString());
				throw new RuntimeException("not such crdt type");
		}
	}
	
	/**
	 * Gets the default value for data field.
	 *
	 * @param df the df
	 * @return the default value for data field
	 */
	public static String getDefaultValueForDataField(DateFormat dateFormat, DataField df) {
		switch(df.get_Crdt_Data_Type()) {
		case NONCRDTFIELD:
			throw new RuntimeException("NONCRDT is depreciated");
		case NORMALINTEGER:
		case LWWINTEGER:
		case NUMDELTAINTEGER:
			return "0";
		case NORMALBOOLEAN:
		case LWWBOOLEAN:
			return "true";
		case NORMALFLOAT:
		case LWWFLOAT:
		case NUMDELTAFLOAT:
			return "0.0";
		case NORMALDOUBLE:
		case LWWDOUBLE:
		case NUMDELTADOUBLE:
			return "0.0";
		case NORMALSTRING:
		case LWWSTRING:
			return "'abc'";
		case NORMALDATETIME:
		case LWWDATETIME:
		case NUMDELTADATETIME:
			return "'"+DatabaseFunction.CURRENTTIMESTAMP(dateFormat)+"'";
		default:
				System.err.println("cannot get default value for primitive type" + df.toString());
				throw new RuntimeException("not such crdt type");
		}
	}
}
