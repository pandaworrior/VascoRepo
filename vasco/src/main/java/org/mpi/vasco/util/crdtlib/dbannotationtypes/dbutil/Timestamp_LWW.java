package org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil;

// TODO: Auto-generated Javadoc
/**
 * The Class Timestamp_LWW.
 */
public class Timestamp_LWW {

	/** The Constant timestamp_LWW_Name. */
	final static String timestamp_LWW_Name = "_SP_ts";

	/**
	 * Gets the _ set_ timestamp_ lww.
	 *
	 * @return the _ set_ timestamp_ lww
	 */
	public String get_Set_Timestamp_LWW() {
		return timestamp_LWW_Name + " = ?";
	}

	/**
	 * Gets the _ set_ lw w_ clause.
	 *
	 * @return the _ set_ lw w_ clause
	 */
	public String get_Set_LWW_Clause() {
		return timestamp_LWW_Name + " <= ?";
	}

	/**
	 * Gets the _ data_ field_ name.
	 *
	 * @return the _ data_ field_ name
	 */
	public String get_Data_Field_Name() {
		return timestamp_LWW_Name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * @see java.lang.Object#toString()
	 * @return
	 */
	public String toString() {
		return "LWW_Timestamp: " + timestamp_LWW_Name;
	}
}
