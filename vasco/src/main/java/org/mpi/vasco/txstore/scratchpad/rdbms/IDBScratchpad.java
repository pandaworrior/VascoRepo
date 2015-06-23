package org.mpi.vasco.txstore.scratchpad.rdbms;
import org.mpi.vasco.util.debug.Debug;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBOpPair;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBReadSetEntry;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBWriteSetEntry;
import org.mpi.vasco.txstore.util.ReadSetEntry;

public interface IDBScratchpad
{
	/**
	 * Returns true if current transaction is read-only.
	 */
	boolean isReadOnly();

	/**
	 * Executes a query in the scratchpad state.
	 */
	ResultSet executeQuery( String op) throws SQLException;

	/**
	 * Executes an update in the scratchpad state.
	 */
	int executeUpdate( String op) throws SQLException;
	
	/**
	 * Executes operation and update timestamp
	 */
	int executeOp(String op)throws SQLException;

	/**
	 * Add an update to the batch in the scratchpad state.
	 */
	void addToBatchUpdate( String op) throws SQLException;
	/**
	 * Execute operations in the batch so far
	 */
	void executeBatch() throws SQLException;
	/**
	 * Add the given entry to the write set
	 */
	boolean addToWriteSet( DBWriteSetEntry entry);
	/**
	 * Add the given entry to the read set
	 */
	boolean addToReadSet( DBReadSetEntry readSetEntry);
	/**
	 * Add the given entry to the operation log
	 * 
	 */
	void addToOpLog( DBOpPair op);
}
