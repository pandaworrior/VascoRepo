package org.mpi.vasco.txstore.scratchpad.rdbms.resolution;
import org.mpi.vasco.util.debug.Debug;

import java.sql.*;
import java.util.*;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import org.mpi.vasco.txstore.scratchpad.ScratchpadException;
import org.mpi.vasco.txstore.scratchpad.rdbms.DBScratchpad;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDBScratchpad;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBReadSetEntry;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSelectResult;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSingleOpPair;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSingleOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBUpdateResult;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBWriteSetEntry;
import org.mpi.vasco.txstore.scratchpad.resolution.ExecutionPolicy;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.Result;
import org.mpi.vasco.txstore.util.TimeStamp;

public abstract class AbstractDBLockExecution implements ExecutionPolicy
{
	TableDefinition def;
	Set<String[]> deletedPks;
	protected String tempTableName;
	String tempTableNameAlias;
	int tableId;
	boolean blue;
	boolean modified;
	
	AbstractDBLockExecution( boolean blue) {
		this.blue = blue;
		this.deletedPks = new HashSet<String[]>();
		this.modified = false;
	}
	
	/**
	 * Called on begin transaction
	 */
	public void beginTx(IDBScratchpad db) {
		try {
			if( modified && DBScratchpad.SQL_ENGINE == DBScratchpad.RDBMS_MYSQL)
				db.addToBatchUpdate( "delete from " + tempTableName + ";");
		} catch (SQLException e) {
			Debug.kill(e);
		}
		modified = false;
		deletedPks.clear();
	}

	/**
	 * Returns the table definition for this execution policy
	 */
	public TableDefinition getTableDefinition() {
		return def;
	}
	/**
	 * Returns the temporary table name
	 */
	public String getTempTableName() {
		return tempTableName;
	}
	/**
	 * Returns the table name
	 */
	public String getTableName() {
		return def.name;
	}
	/**
	 * Returns true if it is a blue table
	 */
	public boolean isBlue() {
		return blue;
	}
	/**
	 * Returns the alias table name
	 */
	public String getAliasTable() {
		return def.getNameAlias();
	}
	/**
	 * Add deleted to where statement
	 */
	public void addDeletedKeysWhere( StringBuffer buffer) {
		/*if(deletedPks == null || deletedPks.isEmpty()){
			Debug.println("\t-----> no deleted keys <------");
			return;
		}
		Iterator<String[]> it = deletedPks.iterator();
		while( it.hasNext()) {
			String[] pk = it.next();
			String[] pkAlias = def.getPksAlias();
			for( int i = 0; i < pk.length; i++) {
				buffer.append( " and ");
				buffer.append( pkAlias[i]);
				buffer.append( " <> '");
				buffer.append( pk[i]);
				buffer.append( "'");
			}
		}
		Debug.println("\t ---------> after removing deleted keys");
		Debug.println(buffer.toString());
		Debug.println("\t<---------after removing deleted keys");*/
	}

	
	/**
	 * Returns what should be in the from clause in select statements
	 */
	public void addFromTable( StringBuffer buffer, boolean both, String[] tableNames) {
		if( both && modified) {
			buffer.append( "(select * from ");
			buffer.append( tableNames[0]);
			buffer.append( " union select * from ");
			buffer.append( tempTableName);
			buffer.append(") as " );
			buffer.append( tableNames[1]);
		} else {
			buffer.append( def.name);
			buffer.append( " as ");
			buffer.append( tableNames[1]);
		}
	}
	
	/**
	 * Returns what should be in the from clause in select statements plus the primary key
	 */
	public void addFromTablePlusPrimaryKeyValues( StringBuffer buffer, boolean both, String[] tableNames,
			String whereClauseStr) {
		if( both && modified) {
			StringBuilder pkValueStrBuilder = new StringBuilder(""); 
			String[] subExpressionStrs = null;
			if(whereClauseStr.contains("AND")) {
				subExpressionStrs = whereClauseStr.split("AND");
			}else {
				subExpressionStrs = whereClauseStr.split("and");
			}
			
			boolean isFirst = true;
			for(int i = 0; i < subExpressionStrs.length; i++) {
				for(int j = 0; j < def.getPksPlain().length; j++) {
					String pk = def.getPksPlain()[j];
					if(subExpressionStrs[i].contains(pk)) {
						Debug.println("I identified one primary key from your where clause " +pk);
						if(subExpressionStrs[i].contains("=")) {
							String tempStr = subExpressionStrs[i].replaceAll("\\s+", "");
							Debug.println("I remove all space " + tempStr);
							int indexOfEqualSign = tempStr.indexOf('=');
							if(indexOfEqualSign < tempStr.length() - 1) {
								String valuePart = tempStr.substring(indexOfEqualSign + 1);
								if(this.isInteger(valuePart)) {
									Debug.println("We identified an integer");
									if(!isFirst) {
										pkValueStrBuilder.append("AND");
									}else {
										isFirst = false;
									}
									pkValueStrBuilder.append(tempStr.subSequence(0, indexOfEqualSign));
									pkValueStrBuilder.append("=");
									pkValueStrBuilder.append(valuePart);
								}
							}
						}
					}
				}
			}
			
			buffer.append( "(select * from ");
			buffer.append( tableNames[0]);
			if(!pkValueStrBuilder.toString().equals("")) {
				buffer.append(" where ");
				buffer.append(pkValueStrBuilder.toString());
			}
			buffer.append( " union select * from ");
			buffer.append( tempTableName);
			if(!pkValueStrBuilder.toString().equals("")) {
				buffer.append(" where ");
				buffer.append(pkValueStrBuilder.toString());
			}
			buffer.append(") as " );
			buffer.append( tableNames[1]);
		} else {
			buffer.append( def.name);
			buffer.append( " as ");
			buffer.append( tableNames[1]);
		}
	}
	
	/**
	 * Returns what should be in the what clause in select statements
	 */
	public void addKeyVVBothTable( StringBuffer buffer, String tableAlias) {
//		return def.getPkListAlias() + "," + def.getNameAlias() + "." + DBScratchpad.SCRATCHPAD_COL_VV;
		String[] pks = def.getPksPlain();
		for( int i = 0; i < pks.length; i++) {
			buffer.append( tableAlias);
			buffer.append( ".");
			buffer.append( pks[i]);
			buffer.append( ",");
		}
		buffer.append( tableAlias);
		buffer.append( ".");
		buffer.append( DBScratchpad.SCRATCHPAD_COL_VV);
	}

	/**
	 * Process ResultSet and add it to the list of results.
	 * @throws SQLException 
	 */
	protected void addToResultList( ResultSet rs, List<String[]> result, IDBScratchpad db, boolean addToReadSet) throws SQLException {
		if( rs == null)
			return;
		ResultSetMetaData rsmd = rs.getMetaData();
		int numberOfColumns = rsmd.getColumnCount();
		int nPks = def.getPksPlain().length;
		if( ! addToReadSet)
			nPks = -1;
		while( rs.next()) {
			String[] row = new String[numberOfColumns-1-nPks];
			for( int i = 0; i < row.length; i++)
				row[i] = rs.getString( i + 1);
			result.add(row);
			if( ! addToReadSet)
				continue;
			if(nPks>0){
				String[] pk = new String[nPks];
				for( int i = 0; i < nPks; i++)
					pk[i] = rs.getString( numberOfColumns - nPks + i);
				LogicalClock lc = new LogicalClock( rs.getString( numberOfColumns));
				db.addToReadSet( DBReadSetEntry.createEntry( def.name, pk, blue, lc));
			}
		}
		rs.beforeFirst();
	}

	/**
	 * Process ResultSet and add it to the list of results.
	 * @throws SQLException 
	 */
	protected void addToResultList( ResultSet rs, List<String[]> result, IDBScratchpad db, ExecutionPolicy[] policies, boolean addToReadSet) throws SQLException {
		if( rs == null)
			return;
		int numMetadata = policies.length;
		for( int i = 0; i < policies.length; i++) 
			numMetadata = numMetadata + policies[i].getTableDefinition().getPksPlain().length;
		if( ! addToReadSet)
			numMetadata = 0;
		ResultSetMetaData rsmd = rs.getMetaData();
		int numberOfColumns = rsmd.getColumnCount();
		while( rs.next()) {
			String[] row = new String[numberOfColumns-numMetadata];
			for( int i = 0; i < numberOfColumns-numMetadata; i++)
				row[i] = rs.getString( i + 1);
			result.add(row);
			if( ! addToReadSet)
				continue;
			for( int i = numberOfColumns-numMetadata, j = 0; j < policies.length; j++) {
				int nPks = policies[j].getTableDefinition().pkPlain.length;
				if(nPks>0){
					String[] pk = new String[nPks];
					for( int n = 0; n < pk.length; n++, i++)
						pk[n] = rs.getString( i+1);
					LogicalClock lc = new LogicalClock( rs.getString( ++i));
					db.addToReadSet( DBReadSetEntry.createEntry( policies[j].getTableDefinition().getName(), pk, policies[j].isBlue(), lc));
				}
			}
		}
		rs.beforeFirst();
	}


	/**
	 * Add order by clause to the current buffer
	 */
	protected void addOrderBy( StringBuffer buffer, List l, ExecutionPolicy policies, String[] tables, boolean inTempTable) {
		if( l == null || l.size() == 0)
			return;
		buffer.append( " order by ");
		Iterator it = l.iterator();
		while( it.hasNext()) {
			buffer.append( replaceAliasInStr( it.next().toString(), policies, tables, inTempTable));
			if( it.hasNext())
				buffer.append(",");
		}
	}

	/**
	 * Add order by clause to the current buffer
	 */
	protected void addOrderBy( StringBuffer buffer, List l, ExecutionPolicy[] policies, String[][] tables, boolean inTempTable) {
		if( l == null || l.size() == 0)
			return;
		buffer.append( " order by ");
		Iterator it = l.iterator();
		while( it.hasNext()) {
			String s = it.next().toString();
			s = replaceAliasInStr(s, policies, tables, inTempTable);
			buffer.append( s);
			if( it.hasNext())
				buffer.append(",");
		}
	}

	/**
	 * Add group by clause to the current buffer
	 */
	protected void addGroupBy( StringBuffer buffer, List l, ExecutionPolicy policies, String[] tables, boolean inTempTable) {
		if( l == null || l.size() == 0)
			return;
		buffer.append( " group by ");
		Iterator it = l.iterator();
		while( it.hasNext()) {
			buffer.append( replaceAliasInStr( it.next().toString(), policies, tables, inTempTable));
			if( it.hasNext())
				buffer.append(",");
		}
	}

	/**
	 * Add group by clause to the current buffer
	 */
	protected void addGroupBy( StringBuffer buffer, List l, ExecutionPolicy[] policies, String[][] tables, boolean inTempTable) {
		if( l == null || l.size() == 0)
			return;
		buffer.append( " group by ");
		Iterator it = l.iterator();
		while( it.hasNext()) {
			String s = it.next().toString();
			s = replaceAliasInStr(s, policies, tables, inTempTable);
			buffer.append( s);
			if( it.hasNext())
				buffer.append(",");
		}
	}

	/**
	 * Add limit clause to the current buffer
	 */
	protected void addLimit( StringBuffer buffer, Limit l) {
		if( l == null)
			return;
		buffer.append( l.toString());
	}

	/**
	 * Add where clause to the current buffer, removing needed deleted Pks
	 */
	protected void addWhere( StringBuffer buffer, Expression e) {
		if( e == null)
			return;
		buffer.append( " where ");
		buffer.append( DBScratchpad.SCRATCHPAD_COL_DELETED);
		buffer.append( " = FALSE");
		if( e != null) {
			buffer.append( " and ( ");
			buffer.append( e.toString());
			buffer.append( " ) ");
		}
		
		this.addDeletedKeysWhere(buffer);
	}
	
	/**
	 * Add where clause to the current buffer, without delete flag
	 */
	protected void addWhereNoDelete( StringBuffer buffer, Expression e) {
		if( e == null)
			return;
		buffer.append( " where ");
		if( e != null) {
			buffer.append( e.toString());
		}
	}
	/**
	 * Add where clause to the current buffer, removing needed deleted Pks
	 */
	protected void addWhere( StringBuffer buffer, Expression e, ExecutionPolicy policies, String[] tables, boolean inTempTable) {
		if( e == null)
			return;
		buffer.append( " where ");
		buffer.append( DBScratchpad.SCRATCHPAD_COL_DELETED);
		buffer.append( " = FALSE");
		if( e != null) {
			buffer.append( " and ( ");
			buffer.append( replaceAliasInStr( e.toString(), policies, tables, inTempTable));
			buffer.append( " ) ");
		}
		this.addDeletedKeysWhere(buffer);
/*		Iterator<String[]> it = deletedPks.iterator();
		while( it.hasNext()) {
			String[] pk = it.next();
			String[] pkName = def.getPksPlain();		//TODO: must differ the plain and alias
			for( int i = 0; i < pk.length; i++) {
				buffer.append( " and ");
				buffer.append( pkName[i]);		//TODO: must differ the plain and alias
				buffer.append( " <> '");
				buffer.append( pk[i]);
				buffer.append( "'");
			}
		}
*/	}
	
	/**
	 * Replace alias in string
	 * @param where
	 * @param policies
	 * @param tables
	 * @return
	 */
	protected String replaceAliasInStr( String where, ExecutionPolicy policies, String[] tables, boolean inTempTable) {
		String t = "([ ,])" + tables[1] + "\\.";
		String tRep = null;
		if( inTempTable)
			tRep = "$1" + ((AbstractDBLockExecution)policies).getTempTableName() + "\\.";
		else
			tRep = "$1" + policies.getTableName() + "\\.";
		return where.replaceAll(t, tRep);
	}

	/**
	 * Replace alias in string
	 * @param where
	 * @param policies
	 * @param tables
	 * @return
	 */
	protected String replaceAliasInStr( String where, ExecutionPolicy[] policies, String[][] tables, boolean inTempTable) {
		for( int i = 0; i < tables.length; i++) {
			String t = "([ ,])" + tables[i][1] + "\\.";
			String tRep = null;
			if( inTempTable){
				tRep =  "$1" + ((AbstractDBLockExecution)policies[i]).getTempTableName() + ".";
			//else
				//tRep = policies[i].getTableName() + ".";
			where = where.replaceAll(t, tRep);}
		}
		return where;
/*		StringBuffer b = new StringBuffer();
		for( int i = 0; i < tables.length; i++) {
			String t = tables[i][1] + ".";
			String tRep = policies[i].getAliasTable() + ".";
			String whereUp = where.toUpperCase();
			int pos = 0;
			b.setLength(0);
			while( true) {
				int nextPos = whereUp.indexOf( t,pos);
				if( nextPos < 0) {
					b.append( where.substring( pos));
					break;
				}
				if( nextPos > 0)
					b.append( where.substring(pos, nextPos));
				if( nextPos == 0) {
					b.append( tRep);
					pos = nextPos + t.length();
				} else {
					char ch = where.charAt(nextPos - 1);
					if( Character.isJavaIdentifierPart(ch))	{	// not completely correct, but should work
						b.append( t);
						pos = nextPos + t.length();
					} else {
						b.append( tRep);
						pos = nextPos + t.length();
					}
				}
			}
			where = b.toString();
		}
		return where;
*/		
	}
	
	/**
	 * Add where clause to the current buffer, removing needed deleted Pks
	 */
	protected void addWhere( StringBuffer buffer, Expression e, ExecutionPolicy[] policies, String[][] tables, boolean inTempTable) {
		if( e == null)
			return;
		buffer.append( " where ");
		for( int i = 0; i < policies.length; i++) {
			if( i > 0)
				buffer.append( " and ");
			buffer.append( tables[i][1]);
			buffer.append(".");
			buffer.append( DBScratchpad.SCRATCHPAD_COL_DELETED);
			buffer.append( " = FALSE");
		}
		if( e != null) {
			buffer.append( " and ( ");
			String where = replaceAliasInStr( e.toString(), policies, tables, inTempTable);
			buffer.append( where);
			buffer.append( " ) ");
		}
		addDeletedKeysWhere(buffer);
	}
	
	@Override
	public void init(DatabaseMetaData dm, String tableName, int id, int tableId, IDBScratchpad db)
			throws ScratchpadException {
		try {
			this.tableId = tableId;
			tempTableName = tableName + "_" + id;
			tempTableNameAlias = DBScratchpad.SCRATCHPAD_TEMPTABLE_ALIAS_PREFIX + tableId;
			String tableNameAlias = DBScratchpad.SCRATCHPAD_TABLE_ALIAS_PREFIX + tableId;
			StringBuffer buffer2 = new StringBuffer();
			buffer2.append("DROP TABLE IF EXIST ");
			StringBuffer buffer = new StringBuffer();
			if( DBScratchpad.SQL_ENGINE == DBScratchpad.RDBMS_H2)
				buffer.append("CREATE LOCAL TEMPORARY TABLE ");	// for H2
			else
				buffer.append("CREATE TABLE IF NOT EXISTS ");		// for mysql
			buffer.append(tempTableName);
			buffer2.append(tempTableName);
			buffer2.append(";");
			buffer.append("(");
			ArrayList<Boolean> tempIsStr = new ArrayList<Boolean>() ;		// for columns
			ArrayList<String> temp = new ArrayList<String>() ;		// for columns
			ArrayList<String> tempAlias = new ArrayList<String>() ;	// for columns with aliases
			ArrayList<String> tempTempAlias = new ArrayList<String>() ;	// for temp columns with aliases
			ArrayList<String> uniqueIndices = new ArrayList<String>(); // unique index
			ResultSet colSet = dm.getColumns(null, null, tableName, "%");
			boolean first = true;
			Debug.println("INFO scratchpad: read table:"+tableName);
			while (colSet.next()) {
				if (!first)
					buffer.append(",");
				else
					first = false;
				buffer.append(colSet.getString(4));			// column name
				buffer.append(" ");
				String[] tmpStr = {""};
				if(colSet.getString(6).contains(" ") == true){		// column type
					tmpStr= colSet.getString(6).split(" ");
				}else{
					tmpStr[0] = colSet.getString(6);
				}
				//System.err.println("INFO scratchpad: read column:"+tmpStr[0]+" sql:"+buffer);
                buffer.append(tmpStr[0]);
				if(!(tmpStr[0].equals("INT") || 
					tmpStr[0].equals("DOUBLE") || 
					tmpStr[0].equals("BIT") || 
					tmpStr[0].equals("DATE") ||
					tmpStr[0].equals("TIME") ||
					tmpStr[0].equals("TIMESTAMP") || 
					tmpStr[0].equals("DATETIME")||
					tmpStr[0].equals("YEAR"))){
						buffer.append("(");
						buffer.append(colSet.getInt(7));		//size of type
						buffer.append(")");
				}
				buffer.append(" ");
				if(tmpStr.length > 1)
					buffer.append(tmpStr[1]);
				if( colSet.getString(4).equalsIgnoreCase( DBScratchpad.SCRATCHPAD_COL_DELETED)) {
					buffer.append(" DEFAULT FALSE ");
				}
				temp.add(  colSet.getString( 4 ));
				tempAlias.add(  tableNameAlias + "." + colSet.getString( 4 ));
				tempTempAlias.add(  tempTableNameAlias + "." + colSet.getString( 4 ));
				tempIsStr.add(colSet.getInt(5) == java.sql.Types.VARCHAR || colSet.getInt(5) == java.sql.Types.LONGNVARCHAR || colSet.getInt(5) == java.sql.Types.LONGVARCHAR 
						|| colSet.getInt(5) == java.sql.Types.CHAR || colSet.getInt(5) == java.sql.Types.DATE || colSet.getInt(5) == java.sql.Types.TIMESTAMP || colSet.getInt(5) == java.sql.Types.TIME);
			}
			colSet.close();
			String[] cols = new String[temp.size()];
			temp.toArray(cols);
			temp.clear();

			String[] aliasCols = new String[tempAlias.size()];
			tempAlias.toArray(aliasCols);
			tempAlias.clear();

			String[] tempAliasCols = new String[tempTempAlias.size()];
			tempTempAlias.toArray(tempAliasCols);
			tempTempAlias.clear();
			
			boolean[] colsIsStr = new boolean[tempIsStr.size()];
			for( int i = 0; i < colsIsStr.length; i++)
				colsIsStr[i] = tempIsStr.get(i);

			//get all unique index
			ResultSet uqIndices = dm.getIndexInfo(null, null, tableName, true, true);
	        while(uqIndices.next()) {
	            String indexName = uqIndices.getString("INDEX_NAME");
	            String columnName = uqIndices.getString("COLUMN_NAME");
	            if(indexName == null) {
	                continue;
	            }
	            Debug.println("UNIQUE INDEX" + columnName);
	            uniqueIndices.add(columnName);
	        }
	        uqIndices.close();
	        
			ResultSet pkSet = dm.getPrimaryKeys(null, null, tableName);
			while (pkSet.next()) {
				if( temp.size() == 0)
					buffer.append(", PRIMARY KEY (");
				else
					buffer.append(", ");
				buffer.append(pkSet.getString(4));
				temp.add(pkSet.getString( 4 ));
				tempAlias.add(  tableNameAlias + "." + pkSet.getString( 4 ));
				tempTempAlias.add(  tempTableNameAlias + "." + pkSet.getString( 4 ));
				uniqueIndices.remove(pkSet.getString( 4 ));
			}
			pkSet.close();
			if( temp.size() > 0)
				buffer.append(")");
			String[] pkPlain = new String[temp.size()];
			temp.toArray(pkPlain);
			temp.clear();

			String[] pkAlias = new String[tempAlias.size()];
			tempAlias.toArray(pkAlias);
			tempAlias.clear();

			String[] pkTempAlias = new String[tempTempAlias.size()];
			tempTempAlias.toArray(pkTempAlias);
			tempTempAlias.clear();
			
			String[] uqIndicesPlain = new String[uniqueIndices.size()];
			uniqueIndices.toArray(uqIndicesPlain);
			uniqueIndices.clear();
			
			Debug.println("Unique indices: " + uqIndicesPlain);

			//			if( temp.size() != 1)
//				throw new RuntimeException( "Does not support table with more than one primary key column : " + tableName + ":" + temp);
			
			def = new TableDefinition( tableName, tableNameAlias, tableId, colsIsStr, cols, aliasCols, tempAliasCols, 
											pkPlain, pkAlias, pkTempAlias, uqIndicesPlain);

			if( DBScratchpad.SQL_ENGINE == DBScratchpad.RDBMS_H2)
				buffer.append(") NOT PERSISTENT;");	// FOR H2
			else
				buffer.append(") ENGINE=MEMORY;");	// FOR MYSQL
			Debug.println("INFO scratchpad sql:"+buffer2+"\n"+buffer);
			Debug.println(buffer.toString());
			try {
				db.executeUpdate(buffer2.toString());
				System.err.println("Table already existed : " + tableName + "_" + id);
				return;
			} catch (Exception e) {
				// do nothing
			}
			db.executeUpdate(buffer.toString());
		} catch (SQLException e) {
			throw new ScratchpadException(e);
		}
	}
	
/**
 * duplicates of the previous function: not create temporary table but set database meta data
 */
	
	@Override
	public void init(DatabaseMetaData dm, String tableName, int id, int tableId)
			throws ScratchpadException {
		try {
			this.tableId = tableId;
			tempTableName = tableName + "_" + id;
			tempTableNameAlias = DBScratchpad.SCRATCHPAD_TEMPTABLE_ALIAS_PREFIX + tableId;
			String tableNameAlias = DBScratchpad.SCRATCHPAD_TABLE_ALIAS_PREFIX + tableId;
			ArrayList<Boolean> tempIsStr = new ArrayList<Boolean>() ;		// for columns
			ArrayList<String> temp = new ArrayList<String>() ;		// for columns
			ArrayList<String> tempAlias = new ArrayList<String>() ;	// for columns with aliases
			ArrayList<String> tempTempAlias = new ArrayList<String>() ;	// for temp columns with aliases
			ArrayList<String> uniqueIndices = new ArrayList<String>(); // unique index
			ResultSet colSet = dm.getColumns(null, null, tableName, "%");
			Debug.println("INFO scratchpad: read table:"+tableName);
			
			while (colSet.next()) {
				String[] tmpStr = {""};
				if(colSet.getString(6).contains(" ") == true){		// column type
					tmpStr= colSet.getString(6).split(" ");
				}else{
					tmpStr[0] = colSet.getString(6);
				}
				//Debug.println("INFO scratchpad: read column:"+tmpStr[0]);
				temp.add(  colSet.getString( 4 ));
				tempAlias.add(  tableNameAlias + "." + colSet.getString( 4 ));
				tempTempAlias.add(  tempTableNameAlias + "." + colSet.getString( 4 ));
				tempIsStr.add(colSet.getInt(5) == java.sql.Types.VARCHAR || colSet.getInt(5) == java.sql.Types.LONGNVARCHAR || colSet.getInt(5) == java.sql.Types.LONGVARCHAR 
						|| colSet.getInt(5) == java.sql.Types.CHAR || colSet.getInt(5) == java.sql.Types.DATE || colSet.getInt(5) == java.sql.Types.TIMESTAMP || colSet.getInt(5) == java.sql.Types.TIME);
			}
			colSet.close();
			
			
			String[] cols = new String[temp.size()];
			temp.toArray(cols);
			temp.clear();

			String[] aliasCols = new String[tempAlias.size()];
			tempAlias.toArray(aliasCols);
			tempAlias.clear();

			String[] tempAliasCols = new String[tempTempAlias.size()];
			tempTempAlias.toArray(tempAliasCols);
			tempTempAlias.clear();
			
			boolean[] colsIsStr = new boolean[tempIsStr.size()];
			for( int i = 0; i < colsIsStr.length; i++)
				colsIsStr[i] = tempIsStr.get(i);

			//get all unique index
			ResultSet uqIndices = dm.getIndexInfo(null, null, tableName, true, true);
	        while(uqIndices.next()) {
	            String indexName = uqIndices.getString("INDEX_NAME");
	            String columnName = uqIndices.getString("COLUMN_NAME");
	            if(indexName == null) {
	                continue;
	            }
	            Debug.println("UNIQUE INDEX" + columnName);
	            uniqueIndices.add(columnName);
	        }
	        uqIndices.close();
			
			ResultSet pkSet = dm.getPrimaryKeys(null, null, tableName);
			while (pkSet.next()) {
				temp.add(pkSet.getString( 4 ));
				tempAlias.add(  tableNameAlias + "." + pkSet.getString( 4 ));
				tempTempAlias.add(  tempTableNameAlias + "." + pkSet.getString( 4 ));
				uniqueIndices.remove(pkSet.getString( 4 ));
			}
			pkSet.close();
			String[] pkPlain = new String[temp.size()];
			temp.toArray(pkPlain);
			temp.clear();

			String[] pkAlias = new String[tempAlias.size()];
			tempAlias.toArray(pkAlias);
			tempAlias.clear();

			String[] pkTempAlias = new String[tempTempAlias.size()];
			tempTempAlias.toArray(pkTempAlias);
			tempTempAlias.clear();
			
			String[] uqIndicesPlain = new String[uniqueIndices.size()];
			uniqueIndices.toArray(uqIndicesPlain);
			uniqueIndices.clear();
			
			Debug.println("Unique indices: " + uqIndicesPlain);
			
			def = new TableDefinition( tableName, tableNameAlias, tableId, colsIsStr, cols, aliasCols, tempAliasCols, 
											pkPlain, pkAlias, pkTempAlias, uqIndicesPlain);

		} catch (SQLException e) {
			throw new ScratchpadException(e);
		}
	}

	/**
	 * Execute select operation in the temporary table
	 * @param op
	 * @param dbOp
	 * @param db
	 * @param tables 
	 * @return
	 * @throws SQLException
	 * @throws ScratchpadException 
	 */
	public Result executeTempOpSelect( DBOperation op, Select dbOp, IDBScratchpad db, ExecutionPolicy[] policies, String[][] tables) throws SQLException, ScratchpadException {
		Debug.println( "multi table select >>" + dbOp);
		HashMap<String,Integer> columnNamesToNumbersMap = new HashMap<String,Integer>();
		StringBuffer buffer = new StringBuffer();
		buffer.append("select ");						// select in base table
		PlainSelect select = (PlainSelect)dbOp.getSelectBody();
		List what = select.getSelectItems();
		int colIndex=1;
		TableDefinition tabdef;
		boolean needComma = true;
		boolean aggregateQuery = false;
		if( what.size() == 1 && what.get(0).toString().equalsIgnoreCase("*")) {
//			buffer.append( "*");
			for( int i = 0; i < policies.length; i++) {
				tabdef = policies[i].getTableDefinition();
				tabdef.addAliasColumnList(buffer, tables[i][1]);
				for(int j=0;j<tabdef.colsPlain.length-3;j++){ //columns doesnt include scratchpad tables
					columnNamesToNumbersMap.put(tabdef.colsPlain[j], colIndex);
					columnNamesToNumbersMap.put(tabdef.name+"."+tabdef.colsPlain[j], colIndex++);
				}
			}
			needComma = false;
		} else {
			Iterator it = what.iterator();
			String str;
			boolean f = true;
			while( it.hasNext()) {
				if( f) 
					f = false;
				else
					buffer.append( ",");
				str = it.next().toString();
				if( str.startsWith( "COUNT(") || str.startsWith( "count(") || str.startsWith( "MAX(") || str.startsWith( "max("))
					aggregateQuery = true;
				int starPos = str.indexOf(".*");
				if( starPos != -1) {
					String itTable = str.substring(0, starPos).trim();
					for( int i = 0; i < tables.length; ) {
						if( itTable.equalsIgnoreCase(tables[i][0]) || itTable.equalsIgnoreCase(tables[i][1])) {
							tabdef = policies[i].getTableDefinition();
							tabdef.addAliasColumnList(buffer, tables[i][1]);
							for(int j=0;j<tabdef.colsPlain.length-3;j++){ //columns doesnt include scratchpad tables
								columnNamesToNumbersMap.put(tabdef.colsPlain[j], colIndex);
								columnNamesToNumbersMap.put(tabdef.name+"."+tabdef.colsPlain[j], colIndex++);
							}
							break;
						}
						i++;
						if( i == tables.length) {
							Debug.println( "not expected " + str + " in select");
							buffer.append(str);
						}
					}
					f = true;
					needComma = false;
				} else {
					buffer.append(str);
					int aliasindex=str.toUpperCase().indexOf(" AS ");
					if(aliasindex!=-1){
						columnNamesToNumbersMap.put(str.substring(aliasindex+4), colIndex++);
						}
					else{
						int dotindex;
						dotindex=str.indexOf(".");
						if(dotindex!=-1){
							columnNamesToNumbersMap.put(str.substring(dotindex+1), colIndex++);
						}
						else{
							columnNamesToNumbersMap.put(str, colIndex++);

						}//else
					}
					needComma = true;
				}//else
				
			}
		}
		if( ! aggregateQuery) {
			for( int i = 0; i < policies.length; i++) {
				if( needComma)
					buffer.append( ",");
				else
					needComma = true;
				policies[i].addKeyVVBothTable( buffer, tables[i][1]);
			}
		}
		buffer.append( " from ");
		//get all joins:
		if(select.getJoins()!=null){
			String whereConditionStr = select.getWhere().toString();
			for(int i = 0; i < policies.length; i++) {
				if(i > 0)
					buffer.append(",");
				policies[i].addFromTablePlusPrimaryKeyValues( buffer, ! db.isReadOnly(), tables[i], whereConditionStr);
				//policies[i].addFromTable( buffer, ! db.isReadOnly(), tables[i]);
			}
			/*for(Iterator joinsIt = select.getJoins().iterator();joinsIt.hasNext();){
				Join join= (Join) joinsIt.next();
				String joinString = join.toString();
				if(joinString.contains("inner join")||joinString.contains("INNER JOIN")|| joinString.contains("left outer join") || joinString.contains("LEFT OUTER JOIN"))
					buffer.append(" ");
				else
					buffer.append(",");
				buffer.append(join.toString());
			}*/
		}
			
//		}else{
//			for( int i = 0; i < policies.length; i++) {
//				if( i > 0)
//					buffer.append( ",");
//				policies[i].addFromTable( buffer, ! db.isReadOnly(), tables[i]);
//			}
//		}
		addWhere( buffer, select.getWhere(), policies, tables, false);
		addGroupBy( buffer, select.getGroupByColumnReferences(), policies, tables, false);
		addOrderBy( buffer, select.getOrderByElements(), policies, tables, false);
		addLimit( buffer, select.getLimit());
		buffer.append( ";");
			
		//Debug.println( "---->" + buffer.toString());
		//Debug.println( "---->" + columnNamesToNumbersMap.toString().toString());
		//System.err.println( "---->" + buffer.toString());
		List<String[]> result = new ArrayList<String[]>();
		ResultSet rs = db.executeQuery( buffer.toString());
		addToResultList(rs, result, db, policies, ! aggregateQuery);
		rs.close();
		return DBSelectResult.createResult( result,columnNamesToNumbersMap);
			
	}
	
	
	/**
	 * Execute select operation in the temporary table without parsing it with multiple policies
	 * @param op
	 * @param dbOp
	 * @param db
	 * @param tables 
	 * @return
	 * @throws SQLException
	 * @throws ScratchpadException 
	 */
	public ResultSet executeTempOpSelectOrig( DBOperation op, Select dbOp, IDBScratchpad db, ExecutionPolicy[] policies, String[][] tables) throws SQLException {
		Debug.println( "multiple policies >>" + dbOp);
		StringBuffer buffer = new StringBuffer();
		buffer.append("select ");						// select in base table
		PlainSelect select = (PlainSelect)dbOp.getSelectBody();
		List what = select.getSelectItems();
		int colIndex=1;
		TableDefinition tabdef;
		boolean needComma = true;
		boolean aggregateQuery = false;
		if( what.size() == 1 && what.get(0).toString().equalsIgnoreCase("*")) {
//			buffer.append( "*");
			needComma = false;
		} else {
			Iterator it = what.iterator();
			String str;
			boolean f = true;
			while( it.hasNext()) {
				if( f) 
					f = false;
				else
					buffer.append( ",");
				str = it.next().toString();
				if( str.startsWith( "COUNT(") || str.startsWith( "count(") || str.startsWith( "MAX(") || str.startsWith( "max("))
					aggregateQuery = true;
				int starPos = str.indexOf(".*");
				if( starPos != -1) {
					String itTable = str.substring(0, starPos).trim();
					for( int i = 0; i < tables.length; ) {
						if( itTable.equalsIgnoreCase(tables[i][0]) || itTable.equalsIgnoreCase(tables[i][1])) {
							break;
						}
						i++;
						if( i == tables.length) {
							Debug.println( "not expected " + str + " in select");
							buffer.append(str);
						}
					}
					f = true;
					needComma = false;
				} else {
					buffer.append(str);
					needComma = true;
				}//else
				
			}
		}
		if( ! aggregateQuery) {
			for( int i = 0; i < policies.length; i++) {
				if( needComma)
					buffer.append( ",");
				else
					needComma = true;
				policies[i].addKeyVVBothTable( buffer, tables[i][1]);
			}
		}
		buffer.append( " from ");
		for( int i = 0; i < policies.length; i++) {
			if( i > 0)
				buffer.append( ",");
			policies[i].addFromTable( buffer, ! db.isReadOnly(), tables[i]);
		}
		addWhere( buffer, select.getWhere(), policies, tables, false);
		addGroupBy( buffer, select.getGroupByColumnReferences(), policies, tables, false);
		addOrderBy( buffer, select.getOrderByElements(), policies, tables, false);
		addLimit( buffer, select.getLimit());
		buffer.append( ";");
			
		Debug.println( "---->" + buffer.toString());
		//System.err.println( "---->" + buffer.toString());
		List<String[]> result = new ArrayList<String[]>();
		ResultSet rs = db.executeQuery( buffer.toString());
		addToResultList(rs, result, db, policies, ! aggregateQuery);
		return rs;
			
	}
	
	/**
	 * Execute select operation in the temporary table
	 * @param op
	 * @param dbOp
	 * @param db
	 * @return
	 * @throws SQLException
	 * @throws ScratchpadException 
	 */
	public Result executeTempOpSelect( DBOperation op, Select dbOp, IDBScratchpad db, String[] table) throws SQLException, ScratchpadException {
		Debug.println( "single table select>>" + dbOp);
		HashMap<String,Integer> columnNamesToNumbersMap = new HashMap<String,Integer>();

		boolean aggregateQuery = false;
		if( db.isReadOnly()) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("select ");
			PlainSelect select = (PlainSelect)dbOp.getSelectBody();
			List what = select.getSelectItems();
			if( what.size() == 1 && what.get(0).toString().equalsIgnoreCase("*")) {
				buffer.append( def.getPlainColumnList());
//				buffer.append( "*");
                        	for(int i = 0; i< def.colsPlain.length;i++){
					String columnName = def.colsPlain[i];
					columnNamesToNumbersMap.put(columnName, i+1);
				}
			} else {
				Iterator it = what.iterator();
				int colNumber=1;
				String str;
				boolean f = true;
				while( it.hasNext()) {
					if( f) 
						f = false;
					else
						buffer.append( ",");
					str = it.next().toString();
					if( str.startsWith( "COUNT(") || str.startsWith( "count(") || str.startsWith( "MAX(") || str.startsWith( "max("))
						aggregateQuery = true;
					int starPos = str.indexOf(".*");
					if( starPos != -1) {
						buffer.append( def.getPlainColumnList());
					} else
						buffer.append(str);
					int aliasindex=str.toUpperCase().indexOf(" AS ");
					if(aliasindex!=-1){
						columnNamesToNumbersMap.put(str.substring(aliasindex+4), colNumber++);
					}
					else{
						int dotindex=str.indexOf(".");
						if(dotindex!=-1){
							columnNamesToNumbersMap.put(str.substring(dotindex+1), colNumber++);
						}
					else
						columnNamesToNumbersMap.put(str, colNumber++);
					}
				}
			}
			if( ! aggregateQuery) {
				buffer.append( ",");
				if(def.getPkListPlain().length()>0){
					buffer.append( def.getPkListPlain());
					buffer.append( ",");
				}
				buffer.append( DBScratchpad.SCRATCHPAD_COL_VV);
			}
			buffer.append( " from ");
			buffer.append( table[0]);
			addWhere( buffer, select.getWhere(), this, table, false);
			addGroupBy( buffer, select.getGroupByColumnReferences(), this, table, false);
			addOrderBy( buffer, select.getOrderByElements(), this, table, false);
			addLimit( buffer, select.getLimit());
			
			Debug.println( "---->" + buffer.toString());
			List<String[]> result = new ArrayList<String[]>();
			ResultSet rs = db.executeQuery( buffer.toString());
			addToResultList(rs, result, db, ! aggregateQuery);
			rs.close();
			return DBSelectResult.createResult(result,columnNamesToNumbersMap);
		} else {
			StringBuffer buffer = new StringBuffer();
			buffer.append("(select ");
			PlainSelect select = (PlainSelect)dbOp.getSelectBody();
			List what = select.getSelectItems();
			if( what.size() == 1 && what.get(0).toString().equalsIgnoreCase("*")) {
				buffer.append( def.getPlainColumnList());
                         	for(int i = 0; i< def.colsPlain.length;i++){
					String columnName = def.colsPlain[i];
					columnNamesToNumbersMap.put(columnName, i+1);
				}
			} else {
				Iterator it = what.iterator();
				int colNumber=1;
				String str;
				boolean f = true;
				while( it.hasNext()) {
					if( f) 
						f = false;
					else
						buffer.append( ",");
					str = it.next().toString();
					if( str.startsWith( "COUNT(") || str.startsWith( "count(") || str.startsWith( "MAX(") || str.startsWith( "max(")){
						aggregateQuery = true;
						buffer.append(str + " as a ");
					}else
						buffer.append(str);
					int dotindex=str.indexOf(".");
					if(dotindex!=-1){
						columnNamesToNumbersMap.put(str.substring(dotindex+1), colNumber++);
					}
					else
						columnNamesToNumbersMap.put(str, colNumber++);
				}
			}
			if( ! aggregateQuery) {
				buffer.append( ",");
				if(def.getPkListPlain().length()>0){
					buffer.append( def.getPkListPlain());
					buffer.append( ",");
				}
				buffer.append( DBScratchpad.SCRATCHPAD_COL_VV);
			}
			buffer.append( " from ");
			buffer.append( table[0]);
			addWhere( buffer, select.getWhere(), this, table, false);
			addGroupBy( buffer, select.getGroupByColumnReferences(), this, table, false);
			addOrderBy( buffer, select.getOrderByElements(), this, table, false);
			buffer.append(") union (select ");
			if( what.size() == 1 && what.get(0).toString().equalsIgnoreCase("*")) {
				buffer.append( def.getPlainColumnList());
			} else {
				Iterator it = what.iterator();
				int colNumber=1;
				String str;
				boolean f = true;
				while( it.hasNext()) {
					if( f) 
						f = false;
					else
						buffer.append( ",");
					str = it.next().toString();
					if( str.startsWith( "COUNT(") || str.startsWith( "count(") || str.startsWith( "MAX(") || str.startsWith( "max(")){
						aggregateQuery = true;
						buffer.append(str + " as a ");
					}else
						buffer.append(str);
					columnNamesToNumbersMap.put(str, colNumber++);
				}
			}
			if( ! aggregateQuery) {
				buffer.append( ",");
				if(def.getPkListPlain().length()>0){
					buffer.append( def.getPkListPlain());
					buffer.append( ",");
				}
				buffer.append( DBScratchpad.SCRATCHPAD_COL_VV);
			}
			buffer.append( " from ");
			buffer.append( this.tempTableName);
			addWhere( buffer, select.getWhere(), this, table, true);
			buffer.append(") ");
			addGroupBy( buffer, select.getGroupByColumnReferences(), this, table, true);
			addOrderBy( buffer, select.getOrderByElements(), this, table, true);
			addLimit( buffer, select.getLimit());
			
			if(aggregateQuery){
				buffer.insert(0, "select sum(a) as 'COUNT(*)' from (");
				buffer.append(") c");
			}
			
			buffer.append( ";");
			
			Debug.println( "---->" + buffer.toString());
			List<String[]> result = new ArrayList<String[]>();
			ResultSet rs = db.executeQuery( buffer.toString());
			addToResultList(rs, result, db, ! aggregateQuery);
			rs.close();
			return DBSelectResult.createResult(result,columnNamesToNumbersMap);
			
		}
	}
	
	
	/**
	 * Execute select operation in the temporary table without parsing it
	 * @param op
	 * @param dbOp
	 * @param db
	 * @return
	 * @throws SQLException
	 * @throws ScratchpadException 
	 */
	public ResultSet executeTempOpSelectOrig( DBOperation op, Select dbOp, IDBScratchpad db, String[] table) throws SQLException {
		Debug.println( "single table >>" + dbOp);

		boolean aggregateQuery = false;
		if( db.isReadOnly()) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("select ");
			PlainSelect select = (PlainSelect)dbOp.getSelectBody();
			List what = select.getSelectItems();
			if( what.size() == 1 && what.get(0).toString().equalsIgnoreCase("*")) {
				buffer.append( def.getPlainColumnList());
//				buffer.append( "*");
			} else {
				Iterator it = what.iterator();
				int colNumber=1;
				String str;
				boolean f = true;
				while( it.hasNext()) {
					if( f) 
						f = false;
					else
						buffer.append( ",");
					str = it.next().toString();
					if( str.startsWith( "COUNT(") || str.startsWith( "count(") || str.startsWith( "MAX(") || str.startsWith( "max("))
						aggregateQuery = true;
					int starPos = str.indexOf(".*");
					if( starPos != -1) {
						buffer.append( def.getPlainColumnList());
					} else
						buffer.append(str);
				}
			}
			if( ! aggregateQuery) {
				buffer.append( ",");
				buffer.append( def.getPkListPlain());
				buffer.append( ",");
				buffer.append( DBScratchpad.SCRATCHPAD_COL_VV);
			}
			buffer.append( " from ");
			buffer.append( table[0]);
			addWhere( buffer, select.getWhere(), this, table, false);
			addGroupBy( buffer, select.getGroupByColumnReferences(), this, table, false);
			addOrderBy( buffer, select.getOrderByElements(), this, table, false);
			addLimit( buffer, select.getLimit());
			
			Debug.println( "---->" + buffer.toString());
			List<String[]> result = new ArrayList<String[]>();
			ResultSet rs = db.executeQuery( buffer.toString());
			addToResultList(rs, result, db, ! aggregateQuery);
			return rs;
		} else {
			StringBuffer buffer = new StringBuffer();
			buffer.append("(select ");
			PlainSelect select = (PlainSelect)dbOp.getSelectBody();
			List what = select.getSelectItems();
			if( what.size() == 1 && what.get(0).toString().equalsIgnoreCase("*")) {
				buffer.append( def.getPlainColumnList());
			} else {
				Iterator it = what.iterator();
				int colNumber=1;
				String str;
				boolean f = true;
				while( it.hasNext()) {
					if( f) 
						f = false;
					else
						buffer.append( ",");
					str = it.next().toString();
					if( str.startsWith( "COUNT(") || str.startsWith( "count(") || str.startsWith( "MAX(") || str.startsWith( "max(")){
						aggregateQuery = true;
						buffer.append(str + " as a ");
					}else
						buffer.append(str);
				}
			}
			if( ! aggregateQuery) {
				buffer.append( ",");
				buffer.append( def.getPkListPlain());
				buffer.append( ",");
				buffer.append( DBScratchpad.SCRATCHPAD_COL_VV);
			}
			buffer.append( " from ");
			buffer.append( table[0]);
			addWhere( buffer, select.getWhere(), this, table, false);
			addGroupBy( buffer, select.getGroupByColumnReferences(), this, table, false);
			addOrderBy( buffer, select.getOrderByElements(), this, table, false);
			buffer.append(") union (select ");
			if( what.size() == 1 && what.get(0).toString().equalsIgnoreCase("*")) {
				buffer.append( def.getPlainColumnList());
			} else {
				Iterator it = what.iterator();
				int colNumber=1;
				String str;
				boolean f = true;
				while( it.hasNext()) {
					if( f) 
						f = false;
					else
						buffer.append( ",");
					str = it.next().toString();
					if( str.startsWith( "COUNT(") || str.startsWith( "count(") || str.startsWith( "MAX(") || str.startsWith( "max(")){
						aggregateQuery = true;
						buffer.append(str + " as a ");
					}else
						buffer.append(str);
				}
			}
			if( ! aggregateQuery) {
				buffer.append( ",");
				buffer.append( def.getPkListPlain());
				buffer.append( ",");
				buffer.append( DBScratchpad.SCRATCHPAD_COL_VV);
			}
			buffer.append( " from ");
			buffer.append( this.tempTableName);
			addWhere( buffer, select.getWhere(), this, table, true);
			buffer.append(") ");
			addGroupBy( buffer, select.getGroupByColumnReferences(), this, table, true);
			addOrderBy( buffer, select.getOrderByElements(), this, table, true);
			addLimit( buffer, select.getLimit());
			
			if(aggregateQuery){
				buffer.insert(0, "select sum(a) as 'COUNT(*)' from (");
				buffer.append(") c");
			}
			
			buffer.append( ";");
			
			Debug.println( "---->" + buffer.toString());
			List<String[]> result = new ArrayList<String[]>();
			ResultSet rs = db.executeQuery( buffer.toString());
			addToResultList(rs, result, db, ! aggregateQuery);
			return rs;
			
		}
	}

	public Result executeTemporaryQuery(DBSingleOperation dbOp, IDBScratchpad db, String[] table) throws SQLException, ScratchpadException {
		if( dbOp.getStatementObj() instanceof Select)
			return executeTempOpSelect( dbOp, (Select)dbOp.getStatementObj(), db, table);
		throw new ScratchpadException( "Unknown update operation : " + dbOp.toString());
	}

	@Override
	public Result executeTemporaryQuery( DBSingleOperation dbOp, IDBScratchpad db, ExecutionPolicy[] policies, String[][] tables) throws SQLException, ScratchpadException {
		if( dbOp.getStatementObj() instanceof Select)
			return executeTempOpSelect( dbOp, (Select)dbOp.getStatementObj(), db, policies, tables);
		throw new ScratchpadException( "Unknown update operation : " + dbOp.toString());
	}
	
	/**
	 * Return the resultset from database, instead of re-assemble it (multi policies)
	 */
	@Override
	public ResultSet executeTemporaryQueryOrig( DBSingleOperation dbOp, IDBScratchpad db, ExecutionPolicy[] policies, String[][] tables) throws SQLException{
		if( dbOp.getStatementObj() instanceof Select)
			return executeTempOpSelectOrig( dbOp, (Select)dbOp.getStatementObj(), db, policies, tables);
		throw new SQLException( "Unknown update operation : " + dbOp.toString());
		
	}
	
	/**
	 * Return the resultset from database, instead of re-assemble it (single table)
	 */
	@Override
	public ResultSet executeTemporaryQueryOrig( DBSingleOperation dbOp, IDBScratchpad db, String[] table) throws SQLException{
		if( dbOp.getStatementObj() instanceof Select)
			return executeTempOpSelectOrig( dbOp, (Select)dbOp.getStatementObj(), db, table);
		throw new SQLException( "Unknown update operation : " + dbOp.toString());
		
	}
	
	/**
	 * Execute insert operation in the temporary table
	 * @param op
	 * @param dbOp
	 * @param db
	 * @return
	 * @throws SQLException
	 */
	protected DBUpdateResult executeTempOpInsert(DBSingleOperation op, Insert dbOp, IDBScratchpad db) throws SQLException {
		Debug.println( ">>" + dbOp);
		
		StringBuffer buffer = new StringBuffer();
		buffer.append( "insert into ");
		buffer.append( tempTableName);
		List s = dbOp.getColumns();
		if( s == null) {
			buffer.append( "(");
			buffer.append(def.getPlainColumnList());
			buffer.append( ")");
		} else {
			buffer.append( "(");
			Iterator it = s.iterator();
			boolean first = true;
			while( it.hasNext()) {
				if( ! first)
					buffer.append(",");
				first = false;
				buffer.append(it.next());
			}
			buffer.append( ")");
		}
		buffer.append( " values ");
		buffer.append( dbOp.getItemsList());
		buffer.append( ";");

		// TODO: blue transactions need to fail here when the value inserted already exists
		Debug.println( buffer.toString());
		int result = db.executeUpdate( buffer.toString());
		String[] pkVal = def.getPlainPKValue( dbOp.getColumns(), dbOp.getItemsList());
		if(pkVal.length > 0){
			db.addToWriteSet( DBWriteSetEntry.createEntry( dbOp.getTable().toString(), pkVal, this.blue,false));
		}
		
		//add unique index to write set as well
		//get unique indices
		String[] uniqueIndicesValue = def.getPlainUniqueIndexValue(dbOp.getColumns(), dbOp.getItemsList());
		for(int i = 0 ; i < uniqueIndicesValue.length; i++){
			String[] uiqStr = new String[1];
			uiqStr[0] = uniqueIndicesValue[i];
			db.addToWriteSet(DBWriteSetEntry.createEntry( dbOp.getTable().toString(), uiqStr, this.blue,false));
		}
		
		//db.addToOpLog( new DBSingleOpPair( op, pkVal));
		return DBUpdateResult.createResult(result);
	}

	/**
	 * Execute delete operation in the temporary table
	 * @param op
	 * @param dbOp
	 * @param db
	 * @return
	 * @throws SQLException
	 */
	protected DBUpdateResult executeTempOpDelete(DBSingleOperation op, Delete dbOp, IDBScratchpad db) throws SQLException {
		Debug.println( ">>" + dbOp);
		
        // GET PRIMERY KEY VALUE
		StringBuffer buffer = new StringBuffer();
		buffer.append( "(select ");
		if(def.getPkListPlain().length()>0)
			buffer.append( def.getPkListPlain());
		else
			buffer.append("*");
		buffer.append( " from ");
		buffer.append( dbOp.getTable().toString());
		addWhere( buffer, dbOp.getWhere());
		buffer.append( ") union (select ");
		if(def.getPkListPlain().length()>0)
			buffer.append( def.getPkListPlain());
		else
			buffer.append("*");
		buffer.append( " from ");
		buffer.append( this.tempTableName);
		addWhere( buffer, dbOp.getWhere());
		buffer.append( ")");
		buffer.append( ";");
		
		Debug.println( "Select keys before delete:" + buffer.toString());
		ResultSet res = db.executeQuery( buffer.toString());
		while( res.next()) {
			int nPks = def.getPksPlain().length;
			if(nPks > 0){
				String[] pkVal = new String[nPks];
				for( int i = 0; i < pkVal.length; i++)
					pkVal[i] = res.getObject(i+1).toString();
				db.addToWriteSet( DBWriteSetEntry.createEntry( dbOp.getTable().toString(), pkVal, this.blue, true));
				//db.addToOpLog( new DBSingleOpPair( op, pkVal));
				deletedPks.add(pkVal);
			}
			
			String[] uniqueIndexStrs = def.getUqIndicesPlain();
			for(int k = 0; k < uniqueIndexStrs.length; k++){
				String[] uqStr = new String[1];
				uqStr[0] = res.getString(uniqueIndexStrs[k]);
				db.addToWriteSet( DBWriteSetEntry.createEntry( def.name, uqStr, blue, true));
			}
		}
		res.close();
		/*buffer = new StringBuffer();
		buffer.append( "delete from ");
		buffer.append( this.tempTableName);
		buffer.append( " where ");
		buffer.append( dbOp.getWhere().toString());
		buffer.append( ";");
		
		Debug.println( ":" + buffer.toString());
		int result = db.executeUpdate( buffer.toString());
		return DBUpdateResult.createResult( result);*/
		return DBUpdateResult.createResult(1);
	}

	/**
	 * Execute update operation in the temporary table
	 * @param op
	 * @param dbOp
	 * @param db
	 * @return
	 * @throws SQLException
	 */
	protected DBUpdateResult executeTempOpUpdate(DBSingleOperation op, Update dbOp, IDBScratchpad db) throws SQLException {
		Debug.println( ">>" + dbOp.toString());
		
        // COPY ROWS TO MODIFY TO SCRATCHPAD
		Debug.println("check whether exist in the temporary table");
		StringBuffer buffer = new StringBuffer();
		buffer.append( "(select *, '"+dbOp.getTable().toString()+"' as tname from ");
		buffer.append( dbOp.getTable().toString());
		addWhere( buffer, dbOp.getWhere());
		buffer.append( ") union (select *, '"+this.tempTableName+"' as tname from ");
		buffer.append( this.tempTableName);
		addWhere( buffer, dbOp.getWhere());
		buffer.append( ")");
		buffer.append( ";");
		Debug.println( ":" + buffer.toString());
		ResultSet res = db.executeQuery( buffer.toString());
		while( res.next()) {
			if(res.getString("tname").equals(this.tempTableName) == false){
				if(res.next() == false){
					Debug.println("record exists in real table but not temp table");
					res.previous();
				}else{
					if(res.getString("tname").equals(this.tempTableName) == false){
						Debug.println("record exists in real table but not temp table");
						res.previous();
					}
					else{
						Debug.println("record exists in both real and temp table");
						continue;
					}
				}		
			}else{
				Debug.println("record exist in temporary table but not real table");
				continue;
			}
			buffer.setLength(0);
			buffer.append("insert into ");
			buffer.append( this.tempTableName);
			buffer.append(" values (");
			for( int i = 0 ; i < def.colsPlain.length; i++) {
				if( i > 0)
					buffer.append( ",");
				if( def.colsStr[i]) {
					buffer.append(res.getObject(i+1) == null ? "NULL" : "\"" + res.getObject(i+1).toString() + "\"");
				} else{
					if(def.colsPlain[i].equals(DBScratchpad.SCRATCHPAD_COL_DELETED))
						buffer.append(res.getObject(i+1) == null ? "NULL" : Integer.toString(res.getInt(i+1)));
					else
						buffer.append(res.getObject(i+1) == null ? "NULL" : res.getObject(i+1).toString());
				}
			}
			buffer.append( ");");
			Debug.println( ":" + buffer.toString());
			db.addToBatchUpdate(buffer.toString());
			int nPks = def.getPksPlain().length;
			if(nPks>0){
				String[] pkVal = new String[nPks];
				for( int i = 0; i < pkVal.length; i++)
					pkVal[i] = res.getObject(i+1).toString();
				db.addToWriteSet( DBWriteSetEntry.createEntry( dbOp.getTable().toString(), pkVal, this.blue,false));
			}
			
			String[] uniqueIndexStrs = def.getUqIndicesPlain();
			for(int k = 0; k < uniqueIndexStrs.length; k++){
				String[] uqStr = new String[1];
				uqStr[0] = res.getString(uniqueIndexStrs[k]);
				db.addToWriteSet( DBWriteSetEntry.createEntry( def.name, uqStr, blue, true));
			}
		}
		res.close();
		
		// DO THE UPDATE
		buffer.setLength(0);
		buffer.append( "update ");
		buffer.append( this.tempTableName);
		buffer.append( " set ");
		Iterator colIt = dbOp.getColumns().iterator();
		Iterator expIt = dbOp.getExpressions().iterator();
		while( colIt.hasNext()) {
			buffer.append( colIt.next());
			buffer.append( " = ");
			buffer.append( expIt.next());
			if( colIt.hasNext())
				buffer.append( " , ");
		}
		buffer.append( " where ");
		buffer.append( dbOp.getWhere().toString());
		buffer.append( ";");

		Debug.println( ":" + buffer.toString());
		db.addToBatchUpdate(buffer.toString());
		db.executeBatch();
		return DBUpdateResult.createResult( 1);
	}

	@Override
	public Result executeTemporaryUpdate(DBSingleOperation dbOp, IDBScratchpad db) throws SQLException, ScratchpadException {
		modified = true;
		if( dbOp.getStatementObj() instanceof Insert)
			return executeTempOpInsert( dbOp, (Insert)dbOp.getStatementObj(), db);
		if( dbOp.getStatementObj() instanceof Delete)
			return executeTempOpDelete( dbOp, (Delete)dbOp.getStatementObj(), db);
		if( dbOp.getStatementObj() instanceof Update)
			return executeTempOpUpdate( dbOp, (Update)dbOp.getStatementObj(), db);
		throw new ScratchpadException( "Unknown update operation : " + dbOp.toString());
	}
	/**
	 * Execute insert operation in the final table
	 * @param op
	 * @param dbOp
	 * @param db
	 * @param b 
	 * @return
	 * @throws SQLException
	 */
	protected abstract void executeDefOpInsert(DBSingleOpPair op, Insert dbOp, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b) throws SQLException;
	/**
	 * Execute insert operation in the final table
	 * @param op
	 * @param dbOp
	 * @param db
	 * @param b 
	 * @return
	 * @throws SQLException
	 */
	protected Result executeDefOpInsert(DBSingleOperation op, Insert dbOp, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b) throws SQLException {
		Debug.println( "DEF-ALL>>>>" + op.sql);

		/*if( modified && b && DBScratchpad.SQL_ENGINE == DBScratchpad.RDBMS_MYSQL) {
			db.executeUpdate( "delete from " + tempTableName + ";");
			modified = false;
		}*/
			
		StringBuffer buffer = new StringBuffer();
		buffer.append( "insert into ");
		buffer.append( def.name);
		List s = dbOp.getColumns();
		if( s == null) {
			buffer.append( "(");
			buffer.append(def.getPlainFullColumnList());
			buffer.append( ")");
		} else {
			buffer.append( "(");
			Iterator it = s.iterator();
			while( it.hasNext()) {
				buffer.append(it.next());
				buffer.append(",");
			}
			buffer.append( DBScratchpad.SCRATCHPAD_COL_DELETED);
			buffer.append(",");
			buffer.append( DBScratchpad.SCRATCHPAD_COL_TS);
			buffer.append(",");
			buffer.append( DBScratchpad.SCRATCHPAD_COL_VV);
			buffer.append( ")");
		}
		buffer.append( " values (");
		Iterator it = ((ExpressionList)dbOp.getItemsList()).getExpressions().iterator();
		while( it.hasNext()) {
			buffer.append( it.next());
			buffer.append(",");
		}
		buffer.append(" FALSE, ");
		buffer.append( ts.toIntString());
		buffer.append(",'");
		buffer.append( lc.toString());
		buffer.append( "');");

		Debug.println( "DEF GEN:" + buffer.toString());
		//int res = db.executeUpdate( buffer.toString());
		db.addToBatchUpdate( buffer.toString());
		return DBUpdateResult.createResult( 1);
	}
	/**
	 * Execute delete operation in the final table
	 * @param op
	 * @param dbOp
	 * @param db
	 * @param b 
	 * @return
	 * @throws SQLException
	 */
	protected abstract void executeDefOpDelete(DBSingleOpPair op, Delete dbOp, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b) throws SQLException;
	/**
	 * Execute delete operation in the final table
	 * @param op
	 * @param dbOp
	 * @param db
	 * @param b 
	 * @return
	 * @throws SQLException
	 */
	protected Result executeDefOpDelete(DBSingleOperation op, Delete dbOp, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b) throws SQLException {
		Debug.println( "DEF-ALL>>>>" + op.sql);
		
		/*if( modified && b && DBScratchpad.SQL_ENGINE == DBScratchpad.RDBMS_MYSQL) {
			db.executeUpdate( "delete from " + tempTableName + ";");
			modified = false;
		}*/
		StringBuffer buffer = new StringBuffer();
		buffer.append( "update ");
		buffer.append( def.name);
		buffer.append( " set ");
		buffer.append( DBScratchpad.SCRATCHPAD_COL_DELETED);
		buffer.append( "  = TRUE, ");
		buffer.append( DBScratchpad.SCRATCHPAD_COL_TS);
		buffer.append( " = ");
		buffer.append( ts.toIntString());
		buffer.append( " , ");
		buffer.append( DBScratchpad.SCRATCHPAD_COL_VV);
		buffer.append( " = '");
		buffer.append( lc.toString());
		buffer.append( "' ");
		addWhereNoDelete(buffer, dbOp.getWhere());
		buffer.append(" AND ");
		buffer.append( DBScratchpad.SCRATCHPAD_COL_TS);
		buffer.append( " <= ");
		buffer.append( ts.toIntString());
		buffer.append( ";");
		
		Debug.println( "DEF GEN:" + buffer.toString());
		//int res = db.executeUpdate( buffer.toString());
		db.addToBatchUpdate( buffer.toString());
		return DBUpdateResult.createResult( 1);
	}
	/**
	 * Execute update operation in the final table
	 * @param op
	 * @param dbOp
	 * @param db
	 * @param b 
	 * @return
	 * @throws SQLException
	 */
	protected abstract void executeDefOpUpdate(DBSingleOpPair op, Update dbOp, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b) throws SQLException;
	/**
	 * Execute update operation in the final table
	 * @param op
	 * @param dbOp
	 * @param db
	 * @param b 
	 * @return
	 * @throws SQLException
	 */
	protected Result executeDefOpUpdate(DBSingleOperation op, Update dbOp, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b) throws SQLException {
		Debug.println( "DEF-ALL>>>>" + op.sql);
		
		/*if( modified && b && DBScratchpad.SQL_ENGINE == DBScratchpad.RDBMS_MYSQL) {
			db.executeUpdate( "delete from " + tempTableName + ";");
			modified = false;
		}*/
		StringBuffer buffer = new StringBuffer();
		buffer.append( "update ");
		buffer.append( def.name);
		buffer.append( " set ");
		Debug.println(buffer);
		Iterator colIt = dbOp.getColumns().iterator();
		Iterator expIt = dbOp.getExpressions().iterator();
		while( colIt.hasNext()) {
			buffer.append( colIt.next());
			buffer.append( " = ");
			buffer.append( expIt.next());
			buffer.append( " , ");
		}
		buffer.append(DBScratchpad.SCRATCHPAD_COL_DELETED);
		buffer.append(" = ");
		buffer.append(" FALSE ");
		buffer.append( " , ");
		buffer.append( DBScratchpad.SCRATCHPAD_COL_TS);
		buffer.append( " = ");
		buffer.append( ts.toIntString());
		buffer.append( " , ");
		buffer.append( DBScratchpad.SCRATCHPAD_COL_VV);
		buffer.append( " = '");
		buffer.append( lc.toString());
		buffer.append( "' ");
		addWhereNoDelete(buffer, dbOp.getWhere());
		buffer.append(" AND ");
		buffer.append( DBScratchpad.SCRATCHPAD_COL_TS);
		buffer.append( " <= ");
		buffer.append( ts.toIntString());
		buffer.append( ";");
		
		Debug.println( "DEF GEN:" + buffer.toString());
		//int res = db.executeUpdate( buffer.toString());
		db.addToBatchUpdate( buffer.toString());
		return DBUpdateResult.createResult( 1);
	}
	
	//only update timestamp
	protected Result executeDefOpTS(DBSingleOperation op, Update dbOp, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b) throws SQLException {
		Debug.println( "DEF-ALL>>>>" + op.sql);
		
		/*if( modified && b && DBScratchpad.SQL_ENGINE == DBScratchpad.RDBMS_MYSQL) {
			db.executeUpdate( "delete from " + tempTableName + ";");
			modified = false;
		}*/
		StringBuffer buffer = new StringBuffer();
		buffer.append( "update ");
		buffer.append( def.name);
		buffer.append( " set ");
		Debug.println(buffer);
		buffer.append(DBScratchpad.SCRATCHPAD_COL_DELETED);
		buffer.append(" = ");
		buffer.append(" FALSE ");
		buffer.append( " , ");
		buffer.append( DBScratchpad.SCRATCHPAD_COL_TS);
		buffer.append( " = ");
		buffer.append( ts.toIntString());
		buffer.append( " , ");
		buffer.append( DBScratchpad.SCRATCHPAD_COL_VV);
		buffer.append( " = '");
		buffer.append( lc.toString());
		buffer.append( "' ");
		addWhereNoDelete(buffer, dbOp.getWhere());
		buffer.append(" AND ");
		buffer.append( DBScratchpad.SCRATCHPAD_COL_TS);
		buffer.append( " <= ");
		buffer.append( ts.toIntString());
		buffer.append( ";");
		
		Debug.println( "DEF GEN:" + buffer.toString());
		//int res = db.executeUpdate( buffer.toString());
		db.addToBatchUpdate( buffer.toString());
		return DBUpdateResult.createResult( 1);
	}

	@Override
	public void executeDefiniteUpdate(DBSingleOpPair op, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b) throws SQLException {
		if( op.op.getStatementObj() instanceof Insert)
			executeDefOpInsert( op, (Insert)op.op.getStatementObj(), db, lc, ts, b);
		else if( op.op.getStatementObj() instanceof Delete)
			executeDefOpDelete( op, (Delete)op.op.getStatementObj(), db, lc, ts, b);
		else if( op.op.getStatementObj() instanceof Update)
			executeDefOpUpdate( op, (Update)op.op.getStatementObj(), db, lc, ts, b);
		else
			throw new RuntimeException( "Not expected:" + op.op.getStatement());
	}

	@Override
	public Result executeDefiniteUpdate(DBSingleOperation op, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b) throws SQLException {
		if( op.getStatementObj() instanceof Insert)
			return executeDefOpInsert( op, (Insert)op.getStatementObj(), db, lc, ts, b);
		else if( op.getStatementObj() instanceof Delete)
			return executeDefOpDelete( op, (Delete)op.getStatementObj(), db, lc, ts, b);
		else if( op.getStatementObj() instanceof Update)
			return executeDefOpUpdate( op, (Update)op.getStatementObj(), db, lc, ts, b);
		else
			throw new RuntimeException( "Not expected:" + op.getStatement());
	}
	
	public Result executeOnlyOp(DBSingleOperation op, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b) throws SQLException {
		if( op.getStatementObj() instanceof Update)
			return executeDefOpTS( op, (Update)op.getStatementObj(), db, lc, ts, b);
		else
			throw new RuntimeException( "Not expected:" + op.getStatement());
	}
	
	//temporarily put there, need to file into other java files
	
	public boolean isInteger(String str)
	{
	    if (str == null) {
	            return false;
	    }
	    int length = str.length();
	    if (length == 0) {
	            return false;
	    }
	    int i = 0;
	    if (str.charAt(0) == '-') {
	            if (length == 1) {
	                    return false;
	            }
	            i = 1;
	    }
	    for (; i < length; i++) {
	            char c = str.charAt(i);
	            if (c <= '/' || c >= ':') {
	                    return false;
	            }
	    }
	    return true;
	}


}
