package org.mpi.vasco.util.annotationparser;

import org.mpi.vasco.util.crdtlib.dbannotationtypes.*;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.*;
/*
import util.crdtlib.dbannotationtypes.LWW_BOOLEAN;
import util.crdtlib.dbannotationtypes.LWW_DATETIME;
import util.crdtlib.dbannotationtypes.LWW_DELETEDFLAG;
import util.crdtlib.dbannotationtypes.LWW_DOUBLE;
import util.crdtlib.dbannotationtypes.LWW_FLOAT;
import util.crdtlib.dbannotationtypes.LWW_INTEGER;
import util.crdtlib.dbannotationtypes.LWW_LOGICALTIMESTAMP;
import util.crdtlib.dbannotationtypes.LWW_STRING;
import util.crdtlib.dbannotationtypes.NONCRDT_Data_Field;
import util.crdtlib.dbannotationtypes.NORMAL_BOOLEAN;
import util.crdtlib.dbannotationtypes.NORMAL_DATETIME;
import util.crdtlib.dbannotationtypes.NORMAL_DOUBLE;
import util.crdtlib.dbannotationtypes.NORMAL_FLOAT;
import util.crdtlib.dbannotationtypes.NORMAL_INTEGER;
import util.crdtlib.dbannotationtypes.NORMAL_STRING;
import util.crdtlib.dbannotationtypes.NUMDELTA_DATETIME;
import util.crdtlib.dbannotationtypes.NUMDELTA_DOUBLE;
import util.crdtlib.dbannotationtypes.NUMDELTA_FLOAT;
import util.crdtlib.dbannotationtypes.NUMDELTA_INTEGER;
import util.crdtlib.dbannotationtypes.dbutil.CrdtDataFieldType;
import util.crdtlib.dbannotationtypes.dbutil.DataField;
import util.crdtlib.dbannotationtypes.dbutil.DatabaseDictionary;
import util.crdtlib.dbannotationtypes.dbutil.RuntimeExceptionType;
*/

// TODO: Auto-generated Javadoc
/**
 * The Class DataFieldParser.
 */
public class DataFieldParser {

	/**
	 * Create_ data_ field_ instance.
	 *
	 * @param tableName the table name
	 * @param attributeDef the attribute def
	 * @param position the position
	 * @return the data field
	 */
	public static DataField create_Data_Field_Instance(String tableName,
			String attributeDef, int position) {
		attributeDef = attributeDef.trim();// remove the empty space at the
											// beginning and the end

		CrdtDataFieldType crdtType = get_Data_Field_Type(attributeDef);
		String fieldName = get_Data_Field_Name(attributeDef);
		String dataType = get_Data_Type(attributeDef);
		boolean isPrimaryKey = is_Primary_Key(attributeDef);
		boolean isForeignKey = false;
		boolean isAutoIncremantal = is_AutoIncremental(attributeDef);

		DataField dF = null;
		switch (crdtType) {
		case NONCRDTFIELD:
			dF = new NONCRDT_Data_Field(fieldName, tableName, dataType,
					isPrimaryKey, isForeignKey, isAutoIncremantal, position);
			break;
		case LWWINTEGER:
			dF = new LWW_INTEGER(fieldName, tableName, dataType, isPrimaryKey,
					isForeignKey, isAutoIncremantal, position);
			break;
		case LWWFLOAT:
			dF = new LWW_FLOAT(fieldName, tableName, dataType, isPrimaryKey,
					isForeignKey, isAutoIncremantal, position);
			break;
		case LWWDOUBLE:
			dF = new LWW_DOUBLE(fieldName, tableName, dataType, isPrimaryKey,
					isForeignKey, isAutoIncremantal, position);
			break;
		case LWWSTRING:
			dF = new LWW_STRING(fieldName, tableName, dataType, isPrimaryKey,
					isForeignKey, isAutoIncremantal, position);
			break;
		case LWWDATETIME:
			dF = new LWW_DATETIME(fieldName, tableName, dataType, isPrimaryKey,
					isForeignKey, isAutoIncremantal, position);
			break;
		case LWWBOOLEAN:
			dF = new LWW_BOOLEAN(fieldName, tableName, dataType, isPrimaryKey,
					isForeignKey, isAutoIncremantal, position);
			break;
		case NUMDELTAINTEGER:
			dF = new NUMDELTA_INTEGER(fieldName, tableName, dataType,
					isPrimaryKey, isForeignKey, isAutoIncremantal, position);
			break;
		case NUMDELTAFLOAT:
			dF = new NUMDELTA_FLOAT(fieldName, tableName, dataType,
					isPrimaryKey, isForeignKey, isAutoIncremantal, position);
			break;
		case NUMDELTADOUBLE:
			dF = new NUMDELTA_DOUBLE(fieldName, tableName, dataType,
					isPrimaryKey, isForeignKey, isAutoIncremantal, position);
			break;
		case NUMDELTADATETIME:
			dF = new NUMDELTA_DATETIME(fieldName, tableName, dataType,
					isPrimaryKey, isForeignKey, isAutoIncremantal, position);
			break;
		case NORMALINTEGER:
			dF = new NORMAL_INTEGER(fieldName, tableName, dataType, isPrimaryKey,
					isForeignKey, isAutoIncremantal, position);
			break;
		case NORMALFLOAT:
			dF = new NORMAL_FLOAT(fieldName, tableName, dataType, isPrimaryKey,
					isForeignKey, isAutoIncremantal, position);
			break;
		case NORMALDOUBLE:
			dF = new NORMAL_DOUBLE(fieldName, tableName, dataType, isPrimaryKey,
					isForeignKey, isAutoIncremantal, position);
			break;
		case NORMALSTRING:
			dF = new NORMAL_STRING(fieldName, tableName, dataType, isPrimaryKey,
					isForeignKey, isAutoIncremantal, position);
			break;
		case NORMALDATETIME:
			dF = new NORMAL_DATETIME(fieldName, tableName, dataType, isPrimaryKey,
					isForeignKey, isAutoIncremantal, position);
			break;
		case NORMALBOOLEAN:
			dF = new NORMAL_BOOLEAN(fieldName, tableName, dataType, isPrimaryKey,
					isForeignKey, isAutoIncremantal, position);
		default:
			try {
				throw new RuntimeException("Unknown CRDT data types!"
						+ crdtType);
			} catch (RuntimeException e) {
				e.printStackTrace();
				System.exit(RuntimeExceptionType.UNKNOWNDATAFIELDANNOTYPE);
			}
		}
		set_Default_Value(dF, attributeDef);
		return dF;
	}
	
	/**
	 * Create_ lww logical timestamp_ data_ field_ instance.
	 *
	 * @param tableName the table name
	 * @param position the position
	 * @return the data field
	 */
	public static DataField create_LwwLogicalTimestamp_Data_Field_Instance(String tableName, int position) {
		String dataType = "String";
		boolean isPrimaryKey = false;
		boolean isForeignKey = false;
		boolean isAutoIncremantal = false;

		DataField dF = new LWW_LOGICALTIMESTAMP(tableName, dataType, isPrimaryKey,
				isForeignKey, isAutoIncremantal, position);
		set_Default_Value(dF, "");
		return dF;
	}
	
	/**
	 * Create_ lww deleted flag_ data_ field_ instance.
	 *
	 * @param tableName the table name
	 * @param position the position
	 * @return the data field
	 */
	public static DataField create_LwwDeletedFlag_Data_Field_Instance(String tableName, int position) {
		String dataType = "boolean";
		boolean isPrimaryKey = false;
		boolean isForeignKey = false;
		boolean isAutoIncremantal = false;

		DataField dF = new LWW_DELETEDFLAG(tableName, dataType, isPrimaryKey,
				isForeignKey, isAutoIncremantal, position);
		set_Default_Value(dF, "false");
		return dF;
	}

	/**
	 * Gets the _ annotation_ type.
	 *
	 * @param attributeDef the attribute def
	 * @return the _ annotation_ type
	 */
	public static String get_Annotation_Type(String attributeDef) {

		int startIndex = attributeDef.indexOf("@");
		if (startIndex == -1)
			return "";
		int endIndex = attributeDef.indexOf(" ");
		if (endIndex <= startIndex) {
			throw_Wrong_Format_Exception(attributeDef);
		}

		return attributeDef.substring(startIndex + 1, endIndex);
	}

	/**
	 * Checks if is _ annotated.
	 *
	 * @param attributeDef the attribute def
	 * @return true, if is _ annotated
	 */
	private static boolean is_Annotated(String attributeDef) {
		int startIndex = attributeDef.indexOf("@");
		if (startIndex == -1)
			return false;
		return true;
	}

	/**
	 * Gets the _ data_ field_ type.
	 *
	 * @param attributeDef the attribute def
	 * @return the _ data_ field_ type
	 */
	public static CrdtDataFieldType get_Data_Field_Type(String attributeDef) {
		String annotationStr = DataFieldParser
				.get_Annotation_Type(attributeDef);
		if (annotationStr == "") {
			return CrdtDataFieldType.NONCRDTFIELD;
		}
		return CrdtDataFieldType.valueOf(annotationStr);
	}

	/**
	 * Gets the _ data_ field_ name.
	 *
	 * @param attributeDef the attribute def
	 * @return the _ data_ field_ name
	 */
	public static String get_Data_Field_Name(String attributeDef) {
		String[] subStrs = attributeDef.split("\\s");
		if (is_Annotated(attributeDef) == true) {
			return subStrs[1].replaceAll("`", "");
		}
		return subStrs[0].replaceAll("`", "");
	}

	/**
	 * Gets the _ data_ type.
	 *
	 * @param attributeDef the attribute def
	 * @return the _ data_ type
	 */
	public static String get_Data_Type(String attributeDef) {
		String dataType = DatabaseDictionary.get_Data_Type(attributeDef);
		if (dataType.equals("")) {
			throw_Wrong_Format_Exception(attributeDef);
		}
		return dataType;
	}

	/**
	 * Checks if is _ primary_ key.
	 *
	 * @param attributeDef the attribute def
	 * @return true, if is _ primary_ key
	 */
	public static boolean is_Primary_Key(String attributeDef) {
		if (attributeDef.toUpperCase().contains("PRIMARY KEY"))
			return true;
		return false;
	}

	/**
	 * Checks if is _ auto incremental.
	 *
	 * @param attributeDef the attribute def
	 * @return true, if is _ auto incremental
	 */
	public static boolean is_AutoIncremental(String attributeDef) {
		if (attributeDef.toUpperCase().contains("AUTO_INCREMENT"))
			return true;
		return false;
	}

	/**
	 * Set_ default_ value.
	 *
	 * @param dF the d f
	 * @param attributeDef the attribute def
	 */
	public static void set_Default_Value(DataField dF, String attributeDef) {

		if (attributeDef.toUpperCase().contains("DEFAULT")) {
			int startIndex = attributeDef.toUpperCase().indexOf("DEFAULT");
			String defaultValue = attributeDef.substring(startIndex + 8);
			defaultValue.replaceAll("'", "");
			dF.set_Default_Value(defaultValue);
		}
		if (attributeDef.toUpperCase().contains("NULL")
				&& !attributeDef.toUpperCase().contains("NOT NULL")) {
			dF.set_Default_Value("NULL");
		}
	}

	/**
	 * Throw_ wrong_ format_ exception.
	 *
	 * @param schemaStr the schema str
	 */
	private static void throw_Wrong_Format_Exception(String schemaStr) {
		try {
			throw new RuntimeException("The attribute defintion " + schemaStr
					+ " is in a wrong format!");
		} catch (RuntimeException e) {
			e.printStackTrace();
			System.exit(RuntimeExceptionType.WRONGCREATTABLEFORMAT);
		}
	}
}
