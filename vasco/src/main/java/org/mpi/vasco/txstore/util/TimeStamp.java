package org.mpi.vasco.txstore.util;

import org.mpi.vasco.util.UnsignedTypes;

public class TimeStamp{
	public static final int MAX_DC = 20;
	
    int dcId;
    long count;

    public TimeStamp(int dc, long c){
	dcId = dc;
	count = c;
    }

    public TimeStamp( String intStr){
    	long n = Long.parseLong(intStr);
    	dcId = (int)(n % MAX_DC);
    	count =  n / MAX_DC;
    }

    public TimeStamp(byte b[], int offset){
	if (offset + getByteSize() > b.length)
	    throw new RuntimeException("byte array is not big enough");

	dcId = UnsignedTypes.bytesToInt(b, offset);
	offset += UnsignedTypes.uint16Size;
	count = UnsignedTypes.bytesToLong(b, offset);
    }

    public void getBytes(byte[] b, int offset){
	UnsignedTypes.intToBytes(dcId, b, offset);
	offset += UnsignedTypes.uint16Size;
	UnsignedTypes.longToBytes(count, b, offset);
    }

    public final int getByteSize(){
	return UnsignedTypes.uint16Size + UnsignedTypes.uint32Size;
    }

    public int getDataCenterId(){
	return dcId;
    }

    public long getCount(){
	return count;
    }

    public int hashCode(){
	return (int)(count *100+dcId);
    }
    
    public String toString(){
	return "("+count+","+dcId+")";
    }

    public String toIntString(){
    	return Long.toString( count * MAX_DC + dcId);
    }

    public long toLong(){
    	return count * (long)MAX_DC + dcId;
    }
    
    public boolean precedes(TimeStamp ts){
    	if(this.toLong() <= ts.toLong())
    		return true;
		return false;
    	
    }
}

