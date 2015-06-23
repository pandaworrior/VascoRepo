package org.mpi.vasco.txstore.scratchpad.rdbms.util;

import org.mpi.vasco.txstore.scratchpad.rdbms.PrimaryExecResults;

public class DBGenericOpPair
	extends DBOpPair
{
	public DBGenericOperation op;
	public PrimaryExecResults results;
	public DBGenericOpPair(DBGenericOperation op, PrimaryExecResults results) {
		this.op = op;
		this.results = results;
	}
	

}
