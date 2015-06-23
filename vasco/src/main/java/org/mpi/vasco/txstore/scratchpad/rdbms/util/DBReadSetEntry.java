package org.mpi.vasco.txstore.scratchpad.rdbms.util;

import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.ReadSetEntry;

public class DBReadSetEntry
	extends ReadSetEntry
{
	transient String table;
	transient String pk;
	transient boolean blue;
	public static DBReadSetEntry createEntry( String table, String[] pk, boolean blue, LogicalClock l) {
		StringBuffer buf = new StringBuffer();
		for( int i = 0; i < pk.length; i++) {
			buf.append(pk[i]);
			buf.append(".");
		}
		return new DBReadSetEntry( table, buf.toString(), blue, l);
	}

	public DBReadSetEntry(String table, String pk, boolean blue, LogicalClock l) {
    	super( table+'-'+pk, blue ? 1 : 0, l);
   		this.table = table;
		this.pk = pk;
		this.blue = blue;
	}
	
	public String toString() {
		return "(" + table + "," + pk + "," + blue + "," + super.getLogicalClock() + ")";
	}

	public int hashCode()  {
		return toString().hashCode();
	}
	
	public boolean equals( Object obj) {
		if( !( obj instanceof ReadSetEntry))
			return false;

		if((this.table.equals(((DBReadSetEntry) obj).table)) &&
				(this.pk.equals(((DBReadSetEntry) obj).pk)) &&
				(this.blue == ((DBReadSetEntry) obj).blue)){
			return true;
		}else{
			return false;
		}
		
	}
	
}
