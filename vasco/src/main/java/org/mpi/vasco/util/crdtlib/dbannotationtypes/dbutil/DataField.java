package org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil;

import java.sql.ResultSet;

import org.mpi.vasco.sieve.staticanalysis.templatecreator.JahobSpecsUtil;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.CrdtFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class DataField.
 */
public abstract class DataField {

	/** The crdt data type. */
	CrdtDataFieldType crdtDataType;
	
	/** The data field name. */
	String dataFieldName;
	
	/** The table name. */
	String tableName;
	
	/** The data type. */
	String dataType;
	
	/** The default value. */
	String defaultValue = null;

	/** The is primary key. */
	boolean isPrimaryKey;
	
	/** The is foreign key. */
	boolean isForeignKey;
	
	/** The is auto incremental. */
	boolean isAutoIncremental;
	
	/** The is allowed null. */
	boolean isAllowedNULL = false;
	
	/** The position. */
	int position = -1;

	/**
	 * Instantiates a new data field.
	 *
	 * @param cDT the c dt
	 * @param dFN the d fn
	 * @param tN the t n
	 * @param dT the d t
	 * @param iPK the i pk
	 * @param iFK the i fk
	 * @param iAIC the i aic
	 * @param pos the pos
	 */
	protected DataField(CrdtDataFieldType cDT, String dFN, String tN,
			String dT, boolean iPK, boolean iFK, boolean iAIC, int pos) {
		this.crdtDataType = cDT;
		this.dataFieldName = dFN;
		this.tableName = tN;
		this.dataType = dT;
		this.isPrimaryKey = iPK;
		this.isForeignKey = iFK;
		this.isAutoIncremental = iAIC;
		this.position = pos;
	}

	/**
	 * Gets the _ crdt_ form.
	 *
	 * @param rs the rs
	 * @param Value the value
	 * @return the _ crdt_ form
	 */
	public abstract String get_Crdt_Form(ResultSet rs, String Value);

	/**
	 * Gets the _ crdt_ form.
	 *
	 * @param Value the value
	 * @return the _ crdt_ form
	 */
	public abstract String get_Crdt_Form(String Value);

	/**
	 * Gets the _ value_ in_ correct_ format.
	 *
	 * @param Value the value
	 * @return the _ value_ in_ correct_ format
	 */
	public abstract String get_Value_In_Correct_Format(String Value);

	/**
	 * Gets the _ crdt_ data_ type.
	 *
	 * @return the _ crdt_ data_ type
	 */
	public CrdtDataFieldType get_Crdt_Data_Type() {
		return this.crdtDataType;
	}

	/**
	 * Gets the _ data_ field_ name.
	 *
	 * @return the _ data_ field_ name
	 */
	public String get_Data_Field_Name() {
		return this.dataFieldName;
	}
	
	public String get_Data_Field_Name_Escape_Jahob() {
		if(JahobSpecsUtil.isJahobKeyWord(this.get_Data_Field_Name())) {
			return this.get_Data_Field_Name()+"1";
		}else {
			return this.get_Data_Field_Name();
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
	 * Gets the _ data_ type.
	 *
	 * @return the _ data_ type
	 */
	public String get_Data_Type() {
		return this.dataType;
	}

	/**
	 * Sets the _ default_ value.
	 *
	 * @param dFV the new _ default_ value
	 */
	public void set_Default_Value(String dFV) {
		this.defaultValue = dFV;
	}

	/**
	 * Set_ nul l_ default_ value.
	 */
	public void set_NULL_Default_Value() {
		this.defaultValue = "NULL";
		this.isAllowedNULL = true;
	}

	/**
	 * Gets the _ default_ value.
	 *
	 * @return the _ default_ value
	 */
	public String get_Default_Value() {
		return this.defaultValue;
	}

	/**
	 * Checks if is _ primary_ key.
	 *
	 * @return true, if is _ primary_ key
	 */
	public boolean is_Primary_Key() {
		return this.isPrimaryKey;
	}

	/**
	 * Set_ primary_ key.
	 */
	public void set_Primary_Key() {
		this.isPrimaryKey = true;
	}

	/**
	 * Checks if is _ foreign_ key.
	 *
	 * @return true, if is _ foreign_ key
	 */
	public boolean is_Foreign_Key() {
		return this.isForeignKey;
	}

	/**
	 * Set_ foreign_ key.
	 */
	public void set_Foreign_Key() {
		this.isForeignKey = true;
	}

	/**
	 * Checks if is _ auto increment.
	 *
	 * @return true, if is _ auto increment
	 */
	public boolean is_AutoIncrement() {
		return this.isAutoIncremental;
	}

	/**
	 * Checks if is _ allowed null.
	 *
	 * @return true, if is _ allowed null
	 */
	public boolean is_AllowedNull() {
		return this.isAllowedNULL;
	}

	/**
	 * Gets the _ position.
	 *
	 * @return the _ position
	 */
	public int get_Position() {
		return this.position;
	}
	
	/**
	 * Checks if is not normal data type.
	 *
	 * @return true, if is not normal data type
	 */
	public boolean isNotNormalDataType() {
		if(CrdtFactory.isNormalDataType(this.crdtDataType)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * To string.
	 *
	 * @return the string
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String status = " TableName: " + this.tableName + "\n";
		status += " DataFieldName: " + this.dataFieldName + "\n";
		status += " DataType: " + this.dataType + "\n";
		status += " PrimaryKey: " + this.isPrimaryKey + "\n";
		status += " ForeignKey: " + this.isForeignKey + "\n";
		status += " AutoIncremental: " + this.isAutoIncremental + "\n";
		status += " IsAllowedNULL: " + this.isAllowedNULL + "\n";
		if (this.isAllowedNULL) {
			status += " DefaultValue: " + this.defaultValue + "\n";
		}
		status += " Position: " + this.position + "\n";
		status += " CrdtType: " + this.crdtDataType + "\n";
		return status;
	}

}
