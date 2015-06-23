package org.mpi.vasco.txstore.scratchpad.rdbms;

import org.mpi.vasco.txstore.util.Result;

public interface IPrimaryExec
{
	public void addResult( Result r); 
	public Result getResult( int pos);
}
