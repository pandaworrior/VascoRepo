package org.mpi.vasco.txstore.scratchpad.rdbms.util;

import org.mpi.vasco.txstore.util.WriteSetEntry;

public class DBWriteSetEntry
		extends WriteSetEntry
{
	transient String table;
	transient String pk;
	transient boolean blue;
	transient boolean deleted; 
	public static DBWriteSetEntry createEntry( String table, String[] pk, boolean blue, boolean deleted) {
		StringBuffer buf = new StringBuffer();
		for( int i = 0; i < pk.length; i++) {
			buf.append(pk[i]);
			buf.append(".");
		}
		return new DBWriteSetEntry( table, buf.toString(), blue, deleted);
		
	}
	public DBWriteSetEntry(String table, String pk, boolean blue, boolean deleted) {
    	super( table+'-'+pk, blue ? 1 : 0, deleted ? 1:0);
	    this.table = table;
		this.pk = pk;
		this.blue = blue;
		this.deleted = deleted;
	}
	
	public String toString() {
		return "(" + table + "," + pk + "," + blue + ","+deleted+")";
	}

	public int hashCode()  {
		return toString().hashCode() ;
	}
	
	public boolean equals( Object obj) {
		if( !( obj instanceof WriteSetEntry))
			return false;
		
		if((this.table.equals(((DBWriteSetEntry) obj).table)) &&
				(this.pk.equals(((DBWriteSetEntry) obj).pk)) &&
				(this.blue == ((DBWriteSetEntry) obj).blue)){
			return true;
		}else{
			return false;
		}
	}
	
}
