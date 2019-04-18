/*
 * This class defines methods to parse a create table statement, for example
 * it will return the table name, and return attributes list.
 */
package org.mpi.vasco.util.annotationparser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.util.crdtlib.dbannotationtypes.AosetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.ArsetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.AusetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.CrdtFactory;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.READONLY_Table;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.UosetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.CrdtTableType;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DataField;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.RuntimeExceptionType;

// TODO: Auto-generated Javadoc
/**
 * The Class CreateStatementParser.
 */
public class CreateStatementParser {

	/**
	 * Checks if is _ create_ table_ statement.
	 *
	 * @param schemaStr the schema str
	 * @return true, if is _ create_ table_ statement
	 */
	public static boolean is_Create_Table_Statement(String schemaStr) {
		if (schemaStr.toLowerCase().contains("create table") == true)
			return true;
		return false;
	}

	/**
	 * Create_ table_ instance.
	 *
	 * @param schemaStr the schema str
	 * @return the database table
	 */
	public static DatabaseTable create_Table_Instance(String schemaStr) {
		if (is_Create_Table_Statement(schemaStr) == false)
			return null;
		else {
			String tableTitleStr = get_Table_Title_String(schemaStr);
			String bodyStr = get_Table_Body_String(schemaStr);

			CrdtTableType tableType = get_Table_Type(tableTitleStr);
			String tableName = get_Table_Name(tableTitleStr);

			Debug.println("Table title " + tableTitleStr);
			Debug.println("Body str " + bodyStr);
			LinkedHashMap<String, DataField> hMp = get_Data_Field_HashMap(
					tableName, bodyStr);

			DatabaseTable dT = null;

			switch (tableType) {
			case NONCRDTTABLE:
				dT = new READONLY_Table(tableName, hMp);
				break;
			case AOSETTABLE:
				dT = new AosetTable(tableName, hMp);
				break;
			case ARSETTABLE:
				ArsetTable.addLwwDeletedFlagDataField(tableName, hMp);
				dT = new ArsetTable(tableName, hMp);
				break;
			case UOSETTABLE:
				dT = new UosetTable(tableName, hMp);
				break;
			case AUSETTABLE:
				dT = new AusetTable(tableName, hMp);
				break;
			default:
				try {
					throw new RuntimeException("Unknown table annotation type");
				} catch (RuntimeException e) {
					e.printStackTrace();
					System.exit(RuntimeExceptionType.UNKNOWNTABLEANNOTYPE);
				}
			}
			return dT;
		}
	}

	/**
	 * Gets the _ table_ title_ string.
	 *
	 * @param schemaStr the schema str
	 * @return the _ table_ title_ string
	 */
	public static String get_Table_Title_String(String schemaStr) {
		int endIndex = schemaStr.indexOf("(");
		return schemaStr.substring(0, endIndex).replaceAll("'", "");
	}

	/**
	 * Gets the _ table_ body_ string.
	 *
	 * @param schemaStr the schema str
	 * @return the _ table_ body_ string
	 */
	public static String get_Table_Body_String(String schemaStr) {
		int startIndex = schemaStr.indexOf("(");
		int endIndex = schemaStr.lastIndexOf(")");

		if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
			throw_Wrong_Format_Exception(schemaStr);
		}

		return schemaStr.substring(startIndex + 1, endIndex);
	}

	/**
	 * Gets the _ table_ type_ annotation.
	 *
	 * @param titleStr the title str
	 * @return the _ table_ type_ annotation
	 */
	private static String get_Table_Type_Annotation(String titleStr) {
		int startIndex = titleStr.indexOf("@");
		if (startIndex == -1)
			return ""; // there is no annotation
		int endIndex = titleStr.indexOf(" ", startIndex);
		String annotationStr = titleStr.substring(startIndex + 1, endIndex);
		return annotationStr;
	}

	/**
	 * Gets the _ table_ type.
	 *
	 * @param titleStr the title str
	 * @return the _ table_ type
	 */
	public static CrdtTableType get_Table_Type(String titleStr) {

		String annotStr = get_Table_Type_Annotation(titleStr);
		if (annotStr == "")
			return CrdtTableType.NONCRDTTABLE;
		else
			return CrdtTableType.valueOf(annotStr);

	}

	/**
	 * Gets the _ table_ name.
	 *
	 * @param titleStr the title str
	 * @return the _ table_ name
	 */
	public static String get_Table_Name(String titleStr) {
		if (titleStr.toLowerCase().indexOf("table") == -1) {
			throw_Wrong_Format_Exception(titleStr);
		}
		titleStr = titleStr.replaceAll("\\s+", " ");
		titleStr = titleStr.replaceAll("`", "");
		String[] subStrs = titleStr.split("\\s"); // \\s is space
		if (subStrs.length == 0) {
			throw_Wrong_Format_Exception(titleStr);
		}
		return subStrs[subStrs.length - 1];
	}
	
	/**
	 * Checks if is right comma to split.
	 *
	 * @param str the str
	 * @param beginIndex the begin index
	 * @param commaIndex the comma index
	 * @return true, if is right comma to split
	 */
	private static boolean isRightCommaToSplit(String str, int beginIndex, int commaIndex){
		int cursorIndex = commaIndex;
		int leftBracket = 0;
		int rightBracket = 0;
		while(cursorIndex >= beginIndex){
			if(str.charAt(cursorIndex) == '('){
				leftBracket++;
			}else if(str.charAt(cursorIndex) == ')'){
				rightBracket++;
			}
			cursorIndex--;
		}
		if(leftBracket == rightBracket){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Gets the _ declarations.
	 *
	 * @param bodyStr the body str
	 * @return the _ declarations
	 */
	public static String[] get_Declarations(String bodyStr) {
		bodyStr = bodyStr.replaceAll("\\s+", " ");
		List<String> declarationList = new ArrayList<String>();
		int beginIndex = 0;
		while(beginIndex < bodyStr.length()){
			int commaIndex = bodyStr.indexOf(',', beginIndex);
			if(commaIndex == -1){
				declarationList.add(bodyStr.substring(beginIndex));
				break;
			}else{
				if(isRightCommaToSplit(bodyStr, beginIndex, commaIndex)){
					declarationList.add(bodyStr.substring(beginIndex, commaIndex));
					beginIndex = commaIndex + 1;
				}else{
					//declarationBeginIndex = beginIndex;
					//beginIndex = commaIndex + 1;
					//continue;
					//search for the right bracket
					int indexOfRightBracket = bodyStr.indexOf(')', commaIndex);
					declarationList.add(bodyStr.substring(beginIndex, indexOfRightBracket + 1));
					if(bodyStr.indexOf(',', indexOfRightBracket) != -1) {
						beginIndex = bodyStr.indexOf(',', indexOfRightBracket) + 1;
					}else {
						beginIndex = indexOfRightBracket + 1;
					}
				}
			}
		}
		//search for the comma
		//String[] subStrs = bodyStr.split(",");
		if (declarationList.size() == 0) {
			throw_Wrong_Format_Exception(bodyStr);
		}
		
		String[] subStrs = new String[declarationList.size()];
		for (int i = 0; i < subStrs.length; i++) {
			subStrs[i] = declarationList.get(i).trim();
		}
		return subStrs;
	}

	/**
	 * Gets the _ attribute strs.
	 *
	 * @param declarationStrs the declaration strs
	 * @return the _ attribute strs
	 */
	public static Vector<String> get_AttributeStrs(String[] declarationStrs) {
		Vector<String> attrStrs = new Vector<String>();
		for (int i = 0; i < declarationStrs.length; i++) {
			if (!(declarationStrs[i].toUpperCase().startsWith("CONSTRAINT")
					|| declarationStrs[i].toUpperCase().startsWith(
							"PRIMARY KEY")
					|| declarationStrs[i].toUpperCase().startsWith("INDEX")
					|| declarationStrs[i].toUpperCase().startsWith("KEY")
					|| declarationStrs[i].toUpperCase().startsWith("UNIQUE") || declarationStrs[i]
					.toUpperCase().startsWith("FOREIGN KEY"))) {
				attrStrs.add(declarationStrs[i]);
				Debug.println("declaration for attribute: " + declarationStrs[i]);
			}
		}
		if (attrStrs.size() == 0) {
			throw_Wrong_Format_Exception("");
		}
		return attrStrs;
	}

	/**
	 * Gets the _ constraint strs.
	 *
	 * @param declarationStrs the declaration strs
	 * @return the _ constraint strs
	 */
	public static Vector<String> get_ConstraintStrs(String[] declarationStrs) {
		Vector<String> constraintStrs = new Vector<String>();
		for (int i = 0; i < declarationStrs.length; i++) {
			if (declarationStrs[i].toUpperCase().startsWith("PRIMARY KEY")
					|| declarationStrs[i].toUpperCase().contains("FOREIGN KEY")) {
				constraintStrs.add(declarationStrs[i]);
			}
		}
		return constraintStrs;
	}

	/**
	 * Gets the _ data_ field.
	 *
	 * @param tableName the table name
	 * @param attrStr the attr str
	 * @param position the position
	 * @return the _ data_ field
	 */
	public static DataField get_Data_Field(String tableName, String attrStr,
			int position) {
		Debug.println("tableName: " + tableName + " attrStr " + attrStr + " position " + position);
		DataField dF = DataFieldParser.create_Data_Field_Instance(tableName,
				attrStr, position);
		return dF;
	}

	/**
	 * Gets the _ data_ fields.
	 *
	 * @param tableName the table name
	 * @param attributeStrs the attribute strs
	 * @return the _ data_ fields
	 */
	public static LinkedHashMap<String, DataField> get_Data_Fields(
			String tableName, Vector<String> attributeStrs) {
		LinkedHashMap<String, DataField> hMpDF = new LinkedHashMap<String, DataField>();
		boolean isContainedLwwDataFields = false;
		for (int i = 0; i < attributeStrs.size(); i++) {
			DataField dF = CreateStatementParser.get_Data_Field(tableName,
					attributeStrs.elementAt(i), i);
			// Debug.println(dF.toString());
			hMpDF.put(dF.get_Data_Field_Name(), dF);
			if(CrdtFactory.isLwwType(dF.get_Crdt_Data_Type()) && isContainedLwwDataFields == false){
				isContainedLwwDataFields = true;
			}
		}
		if(isContainedLwwDataFields){
			DataField lwwLogicalTsDf = DataFieldParser.create_LwwLogicalTimestamp_Data_Field_Instance(tableName, attributeStrs.size());
			hMpDF.put(lwwLogicalTsDf.get_Data_Field_Name(), lwwLogicalTsDf);
		}
		return hMpDF;
	}

	/**
	 * Update_ data_ fields.
	 *
	 * @param dFs the d fs
	 * @param constraintStrs the constraint strs
	 */
	public static void update_Data_Fields(
			LinkedHashMap<String, DataField> dFs, Vector<String> constraintStrs) {
		// primary key, set primary key
		// foreign key, set foreign key

		for (int i = 0; i < constraintStrs.size(); i++) {
			if (constraintStrs.elementAt(i).toUpperCase()
					.contains("PRIMARY KEY")) {
				int startIndex = constraintStrs.elementAt(i).indexOf("(");
				int endIndex = constraintStrs.elementAt(i).indexOf(")");
				if (startIndex >= endIndex || startIndex == -1
						|| endIndex == -1) {
					throw_Wrong_Format_Exception(constraintStrs.elementAt(i));
				}
				String keyStr = constraintStrs.elementAt(i).substring(
						startIndex + 1, endIndex);
				keyStr = keyStr.replaceAll("\\s", "");
				keyStr = keyStr.replaceAll("`", "");
				String[] pKeys = keyStr.split(",");
				for (int j = 0; j < pKeys.length; j++) {
					if (dFs.containsKey(pKeys[j]) == false) {
						throw_Wrong_Format_Exception(constraintStrs
								.elementAt(i) + " " + pKeys[j]);
					}
					dFs.get(pKeys[j]).set_Primary_Key();
				}
			}
			if (constraintStrs.elementAt(i).toUpperCase()
					.contains("FOREIGN KEY")) {
				int locationIndex = constraintStrs.elementAt(i).toUpperCase()
						.indexOf("FOREIGN KEY");
				int startIndex = constraintStrs.elementAt(i).indexOf("(",
						locationIndex);
				int endIndex = constraintStrs.elementAt(i).indexOf(")",
						locationIndex);
				if (startIndex >= endIndex || startIndex == -1
						|| endIndex == -1) {
					throw_Wrong_Format_Exception(constraintStrs.elementAt(i));
				}

				String keyStr = constraintStrs.elementAt(i).substring(
						startIndex + 1, endIndex);
				keyStr = keyStr.replaceAll("\\s", "");
				keyStr = keyStr.replaceAll("`", "");
				String[] fKeys = keyStr.split(",");

				for (int t = 0; t < fKeys.length; t++) {
					if (dFs.containsKey(fKeys[t]) == false) {
						throw_Wrong_Format_Exception(constraintStrs
								.elementAt(i) + " " + fKeys[t]);
					}
					dFs.get(fKeys[t]).set_Foreign_Key();
				}
			}
		}
	}

	/**
	 * Gets the _ data_ field_ hash map.
	 *
	 * @param tableName the table name
	 * @param bodyStr the body str
	 * @return the _ data_ field_ hash map
	 */
	public static LinkedHashMap<String, DataField> get_Data_Field_HashMap(
			String tableName, String bodyStr) {
		String[] declarationStrs = get_Declarations(bodyStr);
		Vector<String> attrStrs = get_AttributeStrs(declarationStrs);
		Vector<String> consStrs = get_ConstraintStrs(declarationStrs);
		LinkedHashMap<String, DataField> hMp = get_Data_Fields(tableName,
				attrStrs);
		update_Data_Fields(hMp, consStrs);
		return hMp;
	}

	/**
	 * Throw_ wrong_ format_ exception.
	 *
	 * @param schemaStr the schema str
	 */
	private static void throw_Wrong_Format_Exception(String schemaStr) {
		try {
			throw new RuntimeException("The create table statment " + schemaStr
					+ " is in a wrong format!");
		} catch (RuntimeException e) {
			e.printStackTrace();
			System.exit(RuntimeExceptionType.WRONGCREATTABLEFORMAT);
		}
	}
}
