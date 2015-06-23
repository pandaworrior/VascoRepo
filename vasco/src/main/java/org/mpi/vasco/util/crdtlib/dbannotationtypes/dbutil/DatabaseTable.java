package org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mpi.vasco.util.crdtlib.dbannotationtypes.CrdtFactory;

import org.mpi.vasco.util.debug.Debug;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;

// TODO: Auto-generated Javadoc
/**
 * The Class DatabaseTable.
 */
public abstract class DatabaseTable {

	/** The table name. */
	String tableName;

	/** The crdt table type. */
	CrdtTableType crdtTableType;

	/** The data field map. */
	protected LinkedHashMap<String, DataField> dataFieldMap; // keep track of
																// all data
																// fields
	/** The sorted data field map. */
	protected HashMap<Integer, DataField> sortedDataFieldMap; // a list of data
	// fields with
	// their
	// position
	// sorted
	/** The primary key map. */
	protected LinkedHashMap<String, DataField> primaryKeyMap; // keep track of
	// all primary
	// key fields
	/** The is contain auto increment field. */
	boolean isContainAutoIncrementField;

	/** The lww deleted flag. */
	protected DataField lwwDeletedFlag = null;

	/** The lww logical timestamp. */
	protected DataField lwwLogicalTimestamp = null;

	/** The timestamp lww. */
	protected static Timestamp_LWW timestampLWW = new Timestamp_LWW();

	// for transforming sql queries

	/** The c jsql parser. */
	protected static CCJSqlParserManager cJsqlParser = new CCJSqlParserManager();
	
	/** The num of hidden fields. */
	private int numOfHiddenFields;
	
	/** The primary key string. It is used to assemble a select to fetch all primary keys for a certain condition */
	private String primaryKeyString;

	/**
	 * Instantiates a new database table.
	 */
	DatabaseTable() {
		this.tableName = null;
		dataFieldMap = new LinkedHashMap<String, DataField>();
		this.primaryKeyMap = new LinkedHashMap<String, DataField>();
		this.isContainAutoIncrementField = false;
		this.setNumOfHiddenFields(0);
		this.setPrimaryKeyString("");
	}

	/**
	 * Instantiates a new database table.
	 * 
	 * @param tN
	 *            the t n
	 * @param cTT
	 *            the c tt
	 * @param dHM
	 *            the d hm
	 */
	protected DatabaseTable(String tN, CrdtTableType cTT,
			LinkedHashMap<String, DataField> dHM) {
		this.tableName = tN;
		this.crdtTableType = cTT;
		dataFieldMap = dHM;
		this.primaryKeyMap = new LinkedHashMap<String, DataField>();
		this.sortedDataFieldMap = new HashMap<Integer, DataField>();
		this.isContainAutoIncrementField = false;
		this.setNumOfHiddenFields(0);
		Iterator<Map.Entry<String, DataField>> it = dHM.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, DataField> entry = (Map.Entry<String, DataField>) it
					.next();
			if (entry.getValue().isPrimaryKey == true) {
				this.add_Primary_Key(entry.getValue());
			}
			if (entry.getValue().is_AutoIncrement() == true
					&& this.is_AutoIncremental() == false) {
				this.isContainAutoIncrementField = true;
			}
			this.sortedDataFieldMap.put(new Integer(entry.getValue()
					.get_Position()), entry.getValue());
			this.setLwwLogicalTimestampDataField(entry.getValue());
			this.setLwwDeletedFlagDataField(entry.getValue());
		}
		this.setPrimaryKeyString(this.assemblePrimaryKeyString());
	}

	// abstract methods
	/**
	 * Transform_ insert.
	 * 
	 * @param insertStatement
	 *            the insert statement
	 * @param insertQuery
	 *            the insert query
	 * @return the string[]
	 * @throws JSQLParserException
	 *             the jSQL parser exception
	 */
	public abstract String[] transform_Insert(Insert insertStatement,
			String insertQuery) throws JSQLParserException;

	/**
	 * Transform_ update.
	 * 
	 * @param rs
	 *            the rs
	 * @param updateStatement
	 *            the update statement
	 * @param updateQuery
	 *            the update query
	 * @return the string[]
	 * @throws JSQLParserException
	 *             the jSQL parser exception
	 */
	public abstract String[] transform_Update(ResultSet rs,
			Update updateStatement, String updateQuery)
			throws JSQLParserException;

	/**
	 * Transform_ delete.
	 * 
	 * @param deleteStatement
	 *            the delete statement
	 * @param deleteQuery
	 *            the delete query
	 * @return the string[]
	 * @throws JSQLParserException
	 *             the jSQL parser exception
	 */
	public abstract String[] transform_Delete(Delete deleteStatement,
			String deleteQuery) throws JSQLParserException;

	/**
	 * Add_ data_ field.
	 * 
	 * @param dF
	 *            the d f
	 */
	public void add_Data_Field(DataField dF) {

		if (dataFieldMap == null) {
			try {
				throw new RuntimeException(
						"data field map has not been initialized!");
			} catch (RuntimeException e) {
				System.exit(RuntimeExceptionType.NOINITIALIZATION);
			}
		}
		if (dataFieldMap.containsKey(dF.get_Data_Field_Name()) == true) {
			try {
				throw new RuntimeException("data field map has duplication!");
			} catch (RuntimeException e) {
				System.exit(RuntimeExceptionType.HASHMAPDUPLICATE);
			}
		}

		dataFieldMap.put(dF.get_Data_Field_Name(), dF);
		this.setLwwLogicalTimestampDataField(dF);
		this.setLwwDeletedFlagDataField(dF);
	}

	/**
	 * Sets the lww logical timestamp data field.
	 * 
	 * @param df
	 *            the new lww logical timestamp data field
	 */
	private void setLwwLogicalTimestampDataField(DataField df) {
		if (CrdtFactory.isLwwLogicalTimestamp(df.get_Crdt_Data_Type())) {
			this.lwwLogicalTimestamp = df;
			this.setNumOfHiddenFields(this.getNumOfHiddenFields()+1);
		}
	}

	/**
	 * Sets the lww deleted flag data field.
	 * 
	 * @param df
	 *            the new lww deleted flag data field
	 */
	private void setLwwDeletedFlagDataField(DataField df) {
		if (CrdtFactory.isLwwDeletedFlag(df.get_Crdt_Data_Type())) {
			this.lwwDeletedFlag = df;
			this.setNumOfHiddenFields(this.getNumOfHiddenFields()+1);
		}
	}

	/**
	 * Add_ primary_ key.
	 * 
	 * @param pK
	 *            the p k
	 */
	public void add_Primary_Key(DataField pK) {
		if (this.primaryKeyMap == null) {
			try {
				throw new RuntimeException(
						"primary key map has not been initialized!");
			} catch (RuntimeException e) {
				System.exit(RuntimeExceptionType.NOINITIALIZATION);
			}
		}
		if (this.primaryKeyMap.containsKey(pK.get_Data_Field_Name()) == true) {
			try {
				throw new RuntimeException("primary key map has duplication!");
			} catch (RuntimeException e) {
				System.exit(RuntimeExceptionType.HASHMAPDUPLICATE);
			}
		}

		this.primaryKeyMap.put(pK.get_Data_Field_Name(), pK);
		if (pK.is_AutoIncrement() == true) {
			this.isContainAutoIncrementField = true;
		}
	}

	/**
	 * Gets the _ table_ name.
	 * 
	 * @return the _ table_ name
	 */
	public String get_Table_Name() {
		return this.tableName;
	}

	/**
	 * Gets the _ crd t_ table_ type.
	 * 
	 * @return the _ crd t_ table_ type
	 */
	public CrdtTableType get_CRDT_Table_Type() {
		return this.crdtTableType;
	}

	/**
	 * Gets the _ data_ field_ list.
	 * 
	 * @return the _ data_ field_ list
	 */
	public HashMap<String, DataField> get_Data_Field_List() {
		return dataFieldMap;
	}

	/**
	 * Gets the data field list.
	 * 
	 * @return the data field list
	 */
	public List<DataField> getDataFieldList() {
		Collection<DataField> dataFields = this.dataFieldMap.values();
		if (dataFields instanceof List) {
			return (List<DataField>) dataFields;
		} else {
			return new ArrayList<DataField>(dataFields);
		}
	}

	/**
	 * Gets the modifiable data field list.
	 * 
	 * @return the modifiable data field list
	 */
	public List<DataField> getModifiableDataFieldList() {
		List<DataField> modifiableDataFieldList = new ArrayList<DataField>();
		Iterator<Entry<String, DataField>> it = this.dataFieldMap.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<String, DataField> itEntry = it.next();
			DataField df = itEntry.getValue();
			if (df.isPrimaryKey == false
					&& CrdtFactory.isNormalDataType(df.get_Crdt_Data_Type()) == false) {
				modifiableDataFieldList.add(df);
			}
		}
		return modifiableDataFieldList;
	}

	/**
	 * Gets the _ primary_ key_ list.
	 * 
	 * @return the _ primary_ key_ list
	 */
	public HashMap<String, DataField> get_Primary_Key_List() {
		return this.primaryKeyMap;
	}

	/**
	 * Gets the primary key data field list.
	 * 
	 * @return the primary key data field list
	 */
	public List<DataField> getPrimaryKeyDataFieldList() {
		Collection<DataField> dataFields = this.primaryKeyMap.values();
		if (dataFields instanceof List) {
			return (List<DataField>) dataFields;
		} else {
			return new ArrayList<DataField>(dataFields);
		}
	}

	/**
	 * Gets the _ primary_ key_ name_ list.
	 * 
	 * @return the _ primary_ key_ name_ list
	 */
	public Set<String> get_Primary_Key_Name_List() {
		return this.primaryKeyMap.keySet();
	}

	/**
	 * Gets the _ data_ field_ count.
	 * 
	 * @return the _ data_ field_ count
	 */
	public int get_Data_Field_Count() {
		int tempCount = dataFieldMap.size() - this.getNumOfHiddenFields();
		if(tempCount <= 0) {
			throw new RuntimeException("You have zero or negative number of data fields");
		}
		return tempCount;
	}

	/**
	 * Gets the _ data_ field.
	 * 
	 * @param dTN
	 *            the d tn
	 * @return the _ data_ field
	 */
	public DataField get_Data_Field(String dTN) {

		if (dataFieldMap == null) {
			try {
				throw new RuntimeException(
						"data field map has not been initialized!");
			} catch (RuntimeException e) {
				System.exit(RuntimeExceptionType.NOINITIALIZATION);
			}
		}

		if (dataFieldMap.containsKey(dTN) == false) {
			try {
				throw new RuntimeException("record is not found " + dTN + "!");
			} catch (RuntimeException e) {
				System.exit(RuntimeExceptionType.HASHMAPNOEXIST);
			}
		}

		return dataFieldMap.get(dTN);

	}

	/**
	 * Gets the _ data_ field.
	 * 
	 * @param dfIndex
	 *            the df index
	 * @return the _ data_ field
	 */
	public DataField get_Data_Field(int dfIndex) {

		if (sortedDataFieldMap == null) {
			try {
				throw new RuntimeException(
						"data field map has not been initialized!");
			} catch (RuntimeException e) {
				System.exit(RuntimeExceptionType.NOINITIALIZATION);
			}
		}

		if (sortedDataFieldMap.size() <= dfIndex) {
			try {
				throw new RuntimeException(
						"data field index beyond the size of data file map!");
			} catch (RuntimeException e) {
				System.exit(RuntimeExceptionType.OUTOFRANGE);
			}
		}

		if (sortedDataFieldMap.containsKey(new Integer(dfIndex)) == false) {
			try {
				throw new RuntimeException("record is not found for " + dfIndex
						+ " index!");
			} catch (RuntimeException e) {
				System.exit(RuntimeExceptionType.HASHMAPNOEXIST);
			}
		}

		return sortedDataFieldMap.get(new Integer(dfIndex));

	}

	/**
	 * Gets the deleted flag.
	 * 
	 * @return the deleted flag
	 */
	public DataField getDeletedFlag() {
		return this.lwwDeletedFlag;
	}
	
	/**
	 * Gets the lww ts.
	 *
	 * @return the lww ts
	 */
	public DataField getLwwTs() {
		return this.lwwLogicalTimestamp;
	}

	/**
	 * Gets the _ primary_ key.
	 * 
	 * @param pKN
	 *            the kn
	 * @return the _ primary_ key
	 */
	public DataField get_Primary_Key(String pKN) {

		if (this.primaryKeyMap == null) {
			try {
				throw new RuntimeException(
						"primary key map has not been initialized!");
			} catch (RuntimeException e) {
				System.exit(RuntimeExceptionType.NOINITIALIZATION);
			}
		}

		if (this.primaryKeyMap.containsKey(pKN) == false) {
			try {
				throw new RuntimeException("record is not found " + pKN + "!");
			} catch (RuntimeException e) {
				System.exit(RuntimeExceptionType.HASHMAPNOEXIST);
			}
		}

		return this.primaryKeyMap.get(pKN);
	}

	/**
	 * Checks if is _ auto incremental.
	 * 
	 * @return true, if is _ auto incremental
	 */
	public boolean is_AutoIncremental() {
		return this.isContainAutoIncrementField;
	}
	
	/**
	 * Checks if is _ readonly.
	 *
	 * @return true, if is _ readonly
	 */
	public boolean is_Readonly() {
		if(this.crdtTableType == CrdtTableType.NONCRDTTABLE) {
			return true;
		}
		return false;
	}

	/**
	 * Find mising data field.
	 * 
	 * @param colList
	 *            the col list
	 * @param valueList
	 *            the value list
	 * @return the sets the
	 */
	public Set<String> findMisingDataField(List<String> colList,
			List<String> valueList) {
		if (get_Data_Field_Count() == colList.size()
				|| get_Data_Field_Count() == valueList.size()) {
			Debug.println("This query doesn't miss any data field!");
			return null;
		}
		if (colList.size() > 0) {
			assert (valueList.size() == colList.size());
			Set<String> dfNameSet = new HashSet<String>();
			Set<String> colSet = new HashSet<String>(colList);
			int i = 0;
			Iterator<Entry<Integer, DataField>> it = sortedDataFieldMap.entrySet().iterator();
			while(it.hasNext() && i < get_Data_Field_Count()) {
				Entry<Integer, DataField> en = (Entry<Integer, DataField>) it.next();
				if(!colSet.contains(en.getValue().get_Data_Field_Name())) {
					dfNameSet.add(en.getValue().get_Data_Field_Name());
				}
				i++;
			}
			Debug.println("We identify the missing data fields "
					+ dfNameSet.toString());
			return dfNameSet;
		} else {
			assert (get_Data_Field_Count() == valueList.size());
			return null;
		}
	}
	
	/**
	 * Checks if is primary key missing from where clause.
	 *
	 * @param whereClauseStr the where clause str
	 * @return true, if is primary key missing from where clause
	 */
	public boolean isPrimaryKeyMissingFromWhereClause(String whereClauseStr) {
		HashMap<String, DataField> pkDFs = this.get_Primary_Key_List();
		Iterator<Map.Entry<String, DataField>> pkIt = pkDFs.entrySet()
				.iterator();
		while (pkIt.hasNext()) {
			DataField pk = pkIt.next().getValue();
			if (!whereClauseStr.contains(pk.get_Data_Field_Name())) {
				Debug.println("You missing primary key in the where clause of this statement");
				return true;
			}
		}
		Debug.println("You didn't miss any primary key in the where clause of this statement");
		return false;
	}

	/*
	 * If an update or delete are not specified by all primary keys, we need to
	 * first fetch them from database
	 */
	/**
	 * Generated primary key query.
	 * 
	 * @param whereClauseStr
	 *            the where clause str
	 * @return the string
	 */
	public String generatedPrimaryKeyQuery(String whereClauseStr) {
			StringBuilder selectStr = new StringBuilder("select ");
			selectStr.append(this.getPrimaryKeyString());
			selectStr.append(" from ");
			selectStr.append(this.get_Table_Name());
			selectStr.append(" ");
			selectStr.append(" where ");
			selectStr.append(whereClauseStr);
			Debug.println("Primary key selection is " + selectStr.toString());
			return selectStr.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/**
	 * To string.
	 *
	 * @return the string
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String myString = "TableName: " + this.tableName + " \n";
		myString += "primary key maps --> \n";

		Iterator<Map.Entry<String, DataField>> itPk = this.primaryKeyMap
				.entrySet().iterator();
		while (itPk.hasNext()) {
			Map.Entry<String, DataField> entry = (Map.Entry<String, DataField>) itPk
					.next();
			myString += entry.getValue().toString() + " \n ";
		}

		myString += "data field maps -------->\n";

		Iterator<Map.Entry<String, DataField>> itDf = dataFieldMap.entrySet()
				.iterator();
		while (itDf.hasNext()) {
			Map.Entry<String, DataField> entry = (Map.Entry<String, DataField>) itDf
					.next();
			myString += entry.getValue().toString() + " \n ";
		}

		myString += " is contained AutoIncremental fields: "
				+ this.isContainAutoIncrementField + "\n";
		if (this.lwwLogicalTimestamp != null) {
			myString += " logicalTimestamp: " + lwwLogicalTimestamp.toString()
					+ "\n";
		}

		return myString;

	}

	/**
	 * Sets the num of hidden fields.
	 *
	 * @param numOfHiddenFields the numOfHiddenFields to set
	 */
	public void setNumOfHiddenFields(int numOfHiddenFields) {
		this.numOfHiddenFields = numOfHiddenFields;
	}

	/**
	 * Gets the num of hidden fields.
	 *
	 * @return the numOfHiddenFields
	 */
	public int getNumOfHiddenFields() {
		return numOfHiddenFields;
	}

	/**
	 * Sets the primary key string.
	 *
	 * @param primaryKeyString the primaryKeyString to set
	 */
	public void setPrimaryKeyString(String primaryKeyString) {
		this.primaryKeyString = primaryKeyString;
	}

	/**
	 * Gets the primary key string.
	 *
	 * @return the primaryKeyString
	 */
	public String getPrimaryKeyString() {
		if(this.primaryKeyString.equals("")) {
			this.setPrimaryKeyString(this.assemblePrimaryKeyString());
		}
		return primaryKeyString;
	}
	
	/**
	 * Assemble primary key string.
	 *
	 * @return the string
	 */
	public String assemblePrimaryKeyString() {
		StringBuilder pkStrBuilder = new StringBuilder("");
		Iterator<String> it = this.get_Primary_Key_Name_List().iterator();
		int index = 0;
		while(it.hasNext()) {
			String singlePkStr = it.next();
			if(index == 0) {
				pkStrBuilder.append(singlePkStr);
			}else {
				pkStrBuilder.append(",");
				pkStrBuilder.append(singlePkStr);
			}
			index++;
		}
		return pkStrBuilder.toString();
	}

}
