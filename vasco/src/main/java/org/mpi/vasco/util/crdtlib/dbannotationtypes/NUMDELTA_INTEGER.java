package org.mpi.vasco.util.crdtlib.dbannotationtypes;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.CrdtDataFieldType;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DataField;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.RuntimeExceptionType;

// TODO: Auto-generated Javadoc
/**
 * The Class NUMDELTA_INTEGER.
 */
public class NUMDELTA_INTEGER extends DataField {

	/**
	 * Instantiates a new numdelta integer.
	 *
	 * @param dFN the d fn
	 * @param tN the t n
	 * @param dT the d t
	 * @param iPK the i pk
	 * @param iFK the i fk
	 * @param iAIC the i aic
	 * @param position the position
	 */
	public NUMDELTA_INTEGER(String dFN, String tN, String dT, boolean iPK,
			boolean iFK, boolean iAIC, int position) {
		super(CrdtDataFieldType.NUMDELTAINTEGER, dFN, tN, dT, iPK, iFK,
				iAIC, position);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Apply_ delta.
	 *
	 * @param delta the delta
	 * @return the string
	 */
	public String apply_Delta(int delta) {
		String transformedSql = "";
		if (delta >= 0)
			transformedSql = this.get_Data_Field_Name() + " = "
					+ this.get_Data_Field_Name() + "+" + delta;
		else
			transformedSql = this.get_Data_Field_Name() + " = "
					+ this.get_Data_Field_Name() + delta;
		return transformedSql;
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

		if (rs == null) {
			try {
				throw new RuntimeException("ResultSet is null!");
			} catch (RuntimeException e) {
				System.exit(RuntimeExceptionType.NULLPOINTER);
			}
		}

		try {
			rs.next();
			int originalValue = rs.getInt(this.get_Data_Field_Name());
			int finalValue = Integer.parseInt(Value);
			int delta = finalValue - originalValue;
			rs.beforeFirst();
			return apply_Delta(delta);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(RuntimeExceptionType.SQLRESULTSETNOTFOUND);
		}
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
		return apply_Delta(Integer.parseInt(Value));
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
		return Value;
	}

}
