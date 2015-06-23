package org.mpi.vasco.txstore.scratchpad.rdbms;

import org.mpi.vasco.txstore.scratchpad.ScratchpadException;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSelectResult;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSingleOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBUpdateResult;

public interface IDatabase
{
	public DBSelectResult executeQuery( String sql) throws ScratchpadException;
	public DBUpdateResult executeUpdate( String sql) throws ScratchpadException;

}
