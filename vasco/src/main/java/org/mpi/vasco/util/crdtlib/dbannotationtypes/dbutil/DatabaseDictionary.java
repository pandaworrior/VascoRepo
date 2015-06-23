/**
 * 
 */
package org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil;

// TODO: Auto-generated Javadoc
/**
 * The Class DatabaseDictionary.
 */
public class DatabaseDictionary {

	/** The Constant dataTypeList. */
	final static String[] dataTypeList = { "INT", "FLOAT", "DOUBLE", "BOOL",
			"BOOLEAN", "DATE", "DATETIME", "TIMESTAMP", "CHAR", "VARCHAR" ,
			"REAL", "INTEGER", "TEXT"};
	
	/** The Constant otherKeyWordList. */
	final static String[] otherKeyWordList = { "", "" };

	/**
	 * Gets the _ data_ type.
	 *
	 * @param defStr the def str
	 * @return the _ data_ type
	 */
	public static String get_Data_Type(String defStr) {
		String[] subStrs = defStr.split(" ");
		for (int i = 0; i < subStrs.length; i++) {
			String typeStr = subStrs[i];
			int endIndex = subStrs[i].indexOf("(");
			if (endIndex != -1)
				typeStr = subStrs[i].substring(0, endIndex);
			for (int j = 0; j < dataTypeList.length; j++) {
				if (typeStr.toUpperCase().equalsIgnoreCase(dataTypeList[j]))
					return dataTypeList[j];
			}
		}
		return "";
	}

}
