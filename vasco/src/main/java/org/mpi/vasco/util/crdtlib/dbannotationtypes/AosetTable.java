package org.mpi.vasco.util.crdtlib.dbannotationtypes;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.CrdtTableType;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DataField;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;
import org.mpi.vasco.util.debug.Debug;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;

// TODO: Auto-generated Javadoc
/**
 * The Class AosetTable.
 */
public class AosetTable extends DatabaseTable {

	/**
	 * Instantiates a new aoset table.
	 *
	 * @param tN the t n
	 * @param dHM the d hm
	 */
	public AosetTable(String tN, LinkedHashMap<String, DataField> dHM) {
		super(tN, CrdtTableType.AOSETTABLE, dHM);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.dbannotationtypes.dbutil.DatabaseTable#transform_Insert(net.sf.jsqlparser.statement.insert.Insert, java.lang.String)
	 */
	/**
	 * @see util.crdtlib.dbannotationtypes.dbutil.DatabaseTable#transform_Insert(net.sf.jsqlparser.statement.insert.Insert, java.lang.String)
	 * @param insertStatement
	 * @param insertQuery
	 * @return
	 * @throws JSQLParserException
	 */
	public String[] transform_Insert(Insert insertStatement, String insertQuery)
			throws JSQLParserException {
		// get tableName
		String tbName = insertStatement.getTable().getName();
		// get column list, if not empty, please add deletedflag, causality and
		// lwwts
		List colList = insertStatement.getColumns();
		// get value list, append these three into it
		String valueStr = "";

		int startIndex = insertQuery.toUpperCase().indexOf("VALUE");
		startIndex = insertQuery.indexOf("(", startIndex);
		int endIndex = insertQuery.lastIndexOf(")");
		valueStr = insertQuery.substring(startIndex + 1, endIndex);

		StringBuffer buffer = new StringBuffer();
		buffer.append("insert into ");
		buffer.append(tbName + " ");
		Iterator it = colList.iterator();
		if (colList.size() > 0) {
			buffer.append("(");
			while (it.hasNext()) {
				buffer.append(it.next() + ",");
			}
			buffer.append(((LWW_DELETEDFLAG)lwwDeletedFlag).get_Data_Field_Name() + ",");
			buffer.append(((LWW_LOGICALTIMESTAMP) lwwLogicalTimestamp).get_Data_Field_Name() + ",");
			buffer.append(timestampLWW.get_Data_Field_Name());
			buffer.append(") ");
		}

		buffer.append(" values (");
		buffer.append(valueStr + ",");
		buffer.append(((LWW_DELETEDFLAG) lwwDeletedFlag).get_Default_Value() + ",");
		buffer.append("? ,"); // for causality clock
		buffer.append("?");// for lww timestamp
		buffer.append(");");

		Debug.println("This is transformed query for AOSET insert: "
				+ buffer.toString());
		String[] transformedSqls = new String[1];
		transformedSqls[0] = buffer.toString();
		return transformedSqls;
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
