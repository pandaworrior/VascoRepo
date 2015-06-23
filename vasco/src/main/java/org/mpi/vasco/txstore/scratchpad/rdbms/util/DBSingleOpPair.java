package org.mpi.vasco.txstore.scratchpad.rdbms.util;

public class DBSingleOpPair
	extends DBOpPair
{
	public DBSingleOperation op;
	public String[] pk;
	public DBSingleOpPair(DBSingleOperation op, String[] pk) {
		this.op = op;
		this.pk = pk;
	}
	
	public String toString() {
		return "(" + op.toString()+ "," + pk + ")";
	}
	

}
