package org.mpi.vasco.txstore.scratchpad.resolution;
import org.mpi.vasco.util.debug.Debug;

import java.sql.*;

import org.mpi.vasco.txstore.scratchpad.ScratchpadException;
import org.mpi.vasco.txstore.scratchpad.rdbms.*;
import org.mpi.vasco.txstore.scratchpad.rdbms.resolution.LWWLockExecution;
import org.mpi.vasco.txstore.scratchpad.rdbms.resolution.TableDefinition;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSingleOpPair;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSingleOperation;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.Result;
import org.mpi.vasco.txstore.util.TimeStamp;

/**
 * Interface for defining the execution and resolution policy for a given table
 * @author nmp
 */
public interface ExecutionPolicy
{
	/**
	 * Called on begin transaction
	 */
	public void beginTx(IDBScratchpad db);
	/**
	 * Returns an unitialized fresh copy of this execution policy
	 */
	public ExecutionPolicy duplicate();
	/**
	 * Returns true if it is a blue table
	 */
	public boolean isBlue();

	/**
	 * Returns the table definition for this execution policy
	 */
	TableDefinition getTableDefinition() ;
	/**
	 * Returns the alias table name
	 */
	String getAliasTable() ;
	/**
	 * Returns the table name
	 */
	String getTableName() ;
	/**
	 * Add deleted to where statement
	 */
	void addDeletedKeysWhere( StringBuffer buffer) ;
	/**
	 * Returns what should be in the from clause in select statements
	 */
	void addFromTable( StringBuffer buffer, boolean both, String[] tableNames) ;
	/**
	 * Returns what should be in the from clause in select statements plus the primary key value for performance
	 */
	void addFromTablePlusPrimaryKeyValues( StringBuffer buffer, boolean both, String[] tableNames, String whereClauseStr) ;
	/**
	 * Returns the text for retrieving key and version vector in select statements
	 */
	public void addKeyVVBothTable( StringBuffer buffer, String tableAlias);
	/**
	 * Called on scratchpad initialization for a given table.
	 * Allows to setup any internal state needed
	 */
	void init( DatabaseMetaData dm, String tableName, int id, int tableId, IDBScratchpad db) throws ScratchpadException;

	/**
	 * Executes a query in the scratchpad temporary state.
	 * @throws ScratchpadException 
	 */
	Result executeTemporaryQuery( DBSingleOperation dbOp, IDBScratchpad db, String[] table) throws SQLException, ScratchpadException;

	/**
	 * Executes a query in the scratchpad temporary state for a query that combines multiple ExecutionPolicies.
	 * @throws ScratchpadException 
	 */
	Result executeTemporaryQuery( DBSingleOperation dbOp, IDBScratchpad db, ExecutionPolicy[] policies, String[][] table) throws SQLException, ScratchpadException;

	/**
	 * Executes a query against database with temporary state for a single table
	 * 
	 */
	ResultSet executeTemporaryQueryOrig( DBSingleOperation dbOp, IDBScratchpad db, String[] table) throws SQLException;
	/**
	 * Executes a query against database with temporary state for multiple table policies 
	 * @throws SQLException
	 */
	
	ResultSet executeTemporaryQueryOrig( DBSingleOperation dbOp, IDBScratchpad db, ExecutionPolicy[] policies, String[][] table) throws SQLException;
	
	/**
	 * Executes an update in the scratchpad temporary state.
	 * @throws ScratchpadException 
	 */
	Result executeTemporaryUpdate( DBSingleOperation dbOp, IDBScratchpad db) throws SQLException, ScratchpadException;

	/**
	 * Executes an update in the scratchpad final state.
	 * @param b 
	 */
	void executeDefiniteUpdate(DBSingleOpPair dbOp, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b) throws SQLException;
	/**
	 * Executes an update in the scratchpad final state for generic operation.
	 * @param b 
	 */
	Result executeDefiniteUpdate(DBSingleOperation dbOp, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b) throws SQLException;
	
	//update timestamp only
	Result executeOnlyOp(DBSingleOperation op, IDBScratchpad db, LogicalClock lc, TimeStamp ts, boolean b) throws SQLException;
	/**
	 * not create temporary table, but set table meta data
	 * @param dm
	 * @param tableName
	 * @param id
	 * @param tableId
	 * @throws ScratchpadException
	 */
	void init(DatabaseMetaData dm, String tableName, int id, int tableId)
			throws ScratchpadException;


}
