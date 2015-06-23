package org.mpi.vasco.txstore.util;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBReadSetEntry;

import org.mpi.vasco.util.UnsignedTypes;

public class ReadSetEntry {

    int  color;
    String obj;
    LogicalClock lc;

    // objId is integer representation of object id
    // color is 1 if blue, 0 if red
    // logical clock is the last modification time for the read object
    public ReadSetEntry(String objId, int color, LogicalClock l){
    //Debug.printf("create read set entry id %s, color %d, lc %s\n", objId, color, l);
    this.obj = objId;
	this.color = color;
	lc = l;
    }

    public ReadSetEntry(String objId, boolean blue, LogicalClock l){
	this(objId, blue?1:0, l);
    }

    public ReadSetEntry(byte b[], int offset){
    	int objLength = UnsignedTypes.bytesToInt(b, offset);
    	offset += UnsignedTypes.uint16Size;
    	
    	byte[] tmp = new byte[objLength];
    	for(int i =0; i <objLength; i++ ){
    		tmp[i] = b[offset];
    		offset++;
    	}
    	obj = new String(tmp);
	color = UnsignedTypes.bytesToInt(b, offset);
	offset += UnsignedTypes.uint16Size;
	lc = new LogicalClock(b, offset);
    }

    public void getBytes(byte[] b, int offset){
	if (offset + getByteSize() > b.length)
	    throw new RuntimeException("not enough bytes");
    int objLength = obj.getBytes().length ;
	
	UnsignedTypes.intToBytes(objLength, b, offset);
	offset += UnsignedTypes.uint16Size;
	
	byte[] tmp = obj.getBytes();
	for(int i =0; i <objLength; i++ ){
		b[offset] = tmp[i];
		offset++;
	}
	
	UnsignedTypes.intToBytes(color, b, offset);
	offset += UnsignedTypes.uint16Size;
	lc.getBytes(b, offset);
    }

    public final int getByteSize(){
	return UnsignedTypes.uint16Size + obj.getBytes().length + UnsignedTypes.uint16Size + lc.getByteSize();
    }
    

    public String getObjectId(){
	return obj;
    }

    public int getColor(){
	return color;
    }
    
    public boolean isBlue(){
	return color == 1;
    }

    public boolean isRed(){
	return color == 0;
    }

    public LogicalClock getLogicalClock(){
	return lc;
    }

    public String toString(){
	return "/"+obj+" "+color+" "+lc+"/";
    }
    
	public int hashCode()  {
		return toString().hashCode();
	}
	
	public boolean equals( Object o) {
		if( !( o instanceof ReadSetEntry))
			return false;
		//return super.getObjectId() == ((ReadSetEntry)obj).getObjectId() &&
					//super.getColor() == ((ReadSetEntry)obj).getColor();
		return this.obj == ((ReadSetEntry) o).obj &&
		this.lc == ((ReadSetEntry) o).lc &&
		this.color == ((ReadSetEntry) o).color;
		
	}
}

