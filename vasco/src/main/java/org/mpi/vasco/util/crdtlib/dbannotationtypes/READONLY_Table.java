package org.mpi.vasco.util.crdtlib.dbannotationtypes;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.CrdtDataFieldType;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.CrdtTableType;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DataField;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.RuntimeExceptionType;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;

// TODO: Auto-generated Javadoc
/**
 * The Class READONLY_Table.
 */
public class READONLY_Table extends DatabaseTable {

	/**
	 * Instantiates a new rEADONL y_ table.
	 *
	 * @param tN the t n
	 * @param dHM the d hm
	 */
	public READONLY_Table(String tN, LinkedHashMap<String, DataField> dHM) {
		super(tN, CrdtTableType.NONCRDTTABLE, dHM);
		// TODO Auto-generated constructor stub
		Iterator<Map.Entry<String, DataField>> it = dHM.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, DataField> entry = (Map.Entry<String, DataField>) it
					.next();
			DataField df = entry.getValue();
			CrdtDataFieldType crdtType = df.get_Crdt_Data_Type();
			if (!(crdtType == CrdtDataFieldType.NONCRDTFIELD
					|| crdtType == CrdtDataFieldType.NORMALBOOLEAN 
					|| crdtType == CrdtDataFieldType.NORMALDATETIME
					|| crdtType == CrdtDataFieldType.NORMALDOUBLE
					|| crdtType == CrdtDataFieldType.NORMALFLOAT
					|| crdtType == CrdtDataFieldType.NORMALINTEGER
					|| crdtType == CrdtDataFieldType.NORMALSTRING)) {
				try {
					throw new RuntimeException(
							"Attributes in a readonly table should not be annotated!");
				} catch (RuntimeException e) {
					e.printStackTrace();
					System.exit(RuntimeExceptionType.READONLYTBLWRONGANNO);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.dbannotationtypes.dbutil.DatabaseTable#toString()
	 */
	/**
	 * @see util.crdtlib.dbannotationtypes.dbutil.DatabaseTable#toString()
	 * @return
	 */
	public String toString() {
		return super.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * crdts.basics.Database_Table#transform_Insert(net.sf.jsqlparser.statement
	 * .insert.Insert, java.lang.String)
	 */
	/**
	 * @see util.crdtlib.dbannotationtypes.dbutil.DatabaseTable#transform_Insert(net.sf.jsqlparser.statement.insert.Insert, java.lang.String)
	 * @param insertStatement
	 * @param insertQuery
	 * @return
	 * @throws JSQLParserException
	 */
	@Override
	public String[] transform_Insert(Insert insertStatement, String insertQuery)
			throws JSQLParserException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see crdts.basics.Database_Table#transform_Update(java.sql.ResultSet,
	 * net.sf.jsqlparser.statement.update.Update, java.lang.String)
	 */
	/**
	 * @see util.crdtlib.dbannotationtypes.dbutil.DatabaseTable#transform_Update(java.sql.ResultSet, net.sf.jsqlparser.statement.update.Update, java.lang.String)
	 * @param rs
	 * @param updateStatement
	 * @param updateQuery
	 * @return
	 * @throws JSQLParserException
	 */
	@Override
	public String[] transform_Update(ResultSet rs, Update updateStatement,
			String updateQuery) throws JSQLParserException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * crdts.basics.Database_Table#transform_Delete(net.sf.jsqlparser.statement
	 * .delete.Delete, java.lang.String)
	 */
	/**
	 * @see util.crdtlib.dbannotationtypes.dbutil.DatabaseTable#transform_Delete(net.sf.jsqlparser.statement.delete.Delete, java.lang.String)
	 * @param deleteStatement
	 * @param deleteQuery
	 * @return
	 * @throws JSQLParserException
	 */
	@Override
	public String[] transform_Delete(Delete deleteStatement, String deleteQuery)
			throws JSQLParserException {
		// TODO Auto-generated method stub
		return null;
	}
}
