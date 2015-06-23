package org.mpi.vasco.txstore.scratchpad.rdbms.util;
import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.txstore.util.Result;

public class DBUpdateResult
	extends Result
{
	transient boolean hasDecoded;
	transient int result;
	
	public static DBUpdateResult createResult( int result) {
		return new DBUpdateResult( result);
	}
	
	public static DBUpdateResult createResult( byte[] arr) {
		return new DBUpdateResult( arr);
	}
	
//	protected DBUpdateResult( int result) {
//		super( new byte[] {
//                (byte)(result >>> 24),
//                (byte)(result >>> 16),
//                (byte)(result >>> 8),
//                (byte)result});
//		this.result = result;
//		this.hasDecoded = true;
//	}
	protected DBUpdateResult( int result) {
		super( new byte[] {
                (byte)(result >>> 24),
                (byte)(result >>> 16),
                (byte)(result >>> 8),
                (byte)result},
                new byte[] {0,0}); //fill in due the new header (column map)
		this.result = result;
		this.hasDecoded = true;
	}
	
	protected DBUpdateResult( byte[] arr) {
		super( arr);
		this.hasDecoded = false;
	}
	protected DBUpdateResult( Result r) {
		super(r.getResult(),r.getColumnIndexesResult());
		this.hasDecoded = false;
	}
	
	public int getUpdateResult() {
		if( hasDecoded)
			return result;
		byte[] b = super.getResult();
        result = (b[0] << 24)
        		+ ((b[1] & 0xFF) << 16)
        		+ ((b[2] & 0xFF) << 8)
        		+ (b[3] & 0xFF);
        hasDecoded = true;
        return result;
	}
}
