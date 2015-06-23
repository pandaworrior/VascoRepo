package org.mpi.vasco.util.crdtlib.dbannotationtypes;

import java.sql.ResultSet;

import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.CrdtDataFieldType;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DataField;

// TODO: Auto-generated Javadoc
/**
 * The Class LWW_DELETEDFLAG.
 */
public class LWW_DELETEDFLAG extends DataField {
	
	/** The Constant deleted_Flag_Name. */
	public final static String deleted_Flag_Name = "_SP_del";
	
	/** The Constant default_value. */
	public final static boolean default_value = false;

	/**
	 * Instantiates a new lww deletedflag.
	 *
	 * @param tN the t n
	 * @param dT the d t
	 * @param iPK the i pk
	 * @param iFK the i fk
	 * @param iAIC the i aic
	 * @param position the position
	 */
	public LWW_DELETEDFLAG(String tN, String dT, boolean iPK,
			boolean iFK, boolean iAIC, int position) {
		super(CrdtDataFieldType.LWWDELETEDFLAG, deleted_Flag_Name, tN, dT, iPK, iFK, iAIC,
				position);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.dbannotationtypes.dbutil.DataField#get_Crdt_Form(java.sql.ResultSet, java.lang.String)
	 */
	/**
	 * @see util.crdtlib.dbannotationtypes.dbutil.DataField#get_Crdt_Form(java.sql.ResultSet, java.lang.String)
	 * @param rs
	 * @param Value
	 * @return
	 */
	@Override
	public String get_Crdt_Form(ResultSet rs, String Value) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see crdts.basics.Data_Field#get_Crdt_Form(java.lang.String)
	 */
	/**
	 * @see util.crdtlib.dbannotationtypes.dbutil.DataField#get_Crdt_Form(java.lang.String)
	 * @param Value
	 * @return
	 */
	@Override
	public String get_Crdt_Form(String Value) {
		// TODO Auto-generated method stub
		Value = Value.trim();
		if (Value.indexOf("'") == 0
				&& Value.lastIndexOf("'") == (Value.length() - 1))
			Value = Value.substring(1, Value.length() - 1);
		return this.get_Data_Field_Name() + " = '" + Value + "'";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * crdts.basics.Data_Field#get_Value_In_Correct_Format(java.lang.String)
	 */
	/**
	 * @see util.crdtlib.dbannotationtypes.dbutil.DataField#get_Value_In_Correct_Format(java.lang.String)
	 * @param Value
	 * @return
	 */
	@Override
	public String get_Value_In_Correct_Format(String Value) {
		// TODO Auto-generated method stub
		if (Value.indexOf("'") == 0
				&& Value.lastIndexOf("'") == Value.length() - 1)
			return Value;
		return "'" + Value + "'";
	}
	
	/**
	 * Gets the _ mark_ deleted.
	 *
	 * @return the _ mark_ deleted
	 */
	public String get_Mark_Deleted() {
		return deleted_Flag_Name + " = " + true;
	}

	/**
	 * Gets the _ unmark_ deleted.
	 *
	 * @return the _ unmark_ deleted
	 */
	public String get_Unmark_Deleted() {
		return deleted_Flag_Name + " = " + false;
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.dbannotationtypes.dbutil.DataField#get_Default_Value()
	 */
	/**
	 * @see util.crdtlib.dbannotationtypes.dbutil.DataField#get_Default_Value()
	 * @return
	 */
	public String get_Default_Value() {
		return "false";
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.dbannotationtypes.dbutil.DataField#get_Data_Field_Name()
	 */
	/**
	 * @see util.crdtlib.dbannotationtypes.dbutil.DataField#get_Data_Field_Name()
	 * @return
	 */
	public String get_Data_Field_Name() {
		return deleted_Flag_Name;
	}
}
