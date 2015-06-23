package org.mpi.vasco.txstore.scratchpad.rdbms;

import java.util.ArrayList;
import java.util.List;

import org.mpi.vasco.txstore.util.Result;


public class PrimaryExecResults
	implements IPrimaryExec
{
	public List<Result> results;
	
	public PrimaryExecResults() {
		results = new ArrayList<Result>();
	}
	
	public void addResult( Result r) {
		results.add(r);
	}
	

	public Result getResult( int pos) {
		if( pos < 0 || pos >= results.size())
			return null;
		else
			return results.get(pos);
	}
	

}
