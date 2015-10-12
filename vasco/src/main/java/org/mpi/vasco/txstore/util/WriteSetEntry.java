package org.mpi.vasco.txstore.util;

import org.mpi.vasco.util.UnsignedTypes;

public class WriteSetEntry{

    String obj; 
    int  invariantRelated;//1 => yes, 0 => no
    int deleted;
    

    public WriteSetEntry(String objId, int iR, int deleted){
    //Debug.printf("create write set entry id %s, invariant related %d deleted %d\n", objId, iR, deleted);
	this.obj = objId;
	this.invariantRelated = iR;
	this.deleted = deleted;
    }

    public WriteSetEntry(byte b[], int offset){
    
    	int objLength = UnsignedTypes.bytesToInt(b, offset);
    	offset += UnsignedTypes.uint16Size;
    	
    	byte[] tmp = new byte[objLength];
    	for(int i =0; i <objLength; i++ ){
    		tmp[i] = b[offset];
    		offset++;
    	}
    	obj = new String(tmp);
    	invariantRelated = UnsignedTypes.bytesToInt(b, offset);
    	offset += UnsignedTypes.uint16Size;
    	deleted = UnsignedTypes.bytesToInt(b, offset);
    	offset += UnsignedTypes.uint16Size;
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
		
		UnsignedTypes.intToBytes(invariantRelated, b, offset);
		offset += UnsignedTypes.uint16Size;
		UnsignedTypes.intToBytes(deleted, b, offset);
		offset += UnsignedTypes.uint16Size;
    }

    public final int getByteSize(){
	return UnsignedTypes.uint16Size + obj.getBytes().length + UnsignedTypes.uint16Size + UnsignedTypes.uint16Size ;
    }
    

    public String getObjectId(){
    	return obj;
    }
    
    public boolean isDeleted(){
    	if(deleted == 1){
    		return true;
    	}
    	return false;
    }

    
    public String toString(){
	return "/"+obj+" "+invariantRelated+ " " + deleted+"/";
    }

	public int getInvariantRelated() {
		return invariantRelated;
	}

	public void setInvariantRelated(int invariantRelated) {
		this.invariantRelated = invariantRelated;
	}
	
	public boolean isInvariantRelated(){
		return invariantRelated == 1;
	}

}

