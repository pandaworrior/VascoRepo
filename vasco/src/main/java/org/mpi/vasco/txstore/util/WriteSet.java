package org.mpi.vasco.txstore.util;

import org.mpi.vasco.util.UnsignedTypes;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Vector;

import java.util.Set;

public class WriteSet{

    WriteSetEntry writeSet[];

    public WriteSet(WriteSetEntry[] rs){
    	writeSet = rs;
    }

    public WriteSet(Set<WriteSetEntry> rs){
    	this(rs.toArray(_wse));
    }
    
    static WriteSetEntry[] _wse = new WriteSetEntry[0];
	    public WriteSet(Vector<WriteSetEntry> ws){
		this(ws.toArray(_wse));
    }

    public WriteSet(byte b[], int offset){

		int sz = UnsignedTypes.bytesToInt(b, offset);
		offset += UnsignedTypes.uint16Size;
	
		writeSet = new WriteSetEntry[sz];
		for (int i = 0; i < sz; i++){
		    writeSet[i] = new WriteSetEntry(b, offset);
		    offset += writeSet[i].getByteSize();
		}	
    }

    public void getBytes(byte[] b, int offset){
		UnsignedTypes.intToBytes(writeSet.length, b, offset);
		offset += UnsignedTypes.uint16Size;
		for (int i = 0; i < writeSet.length; i++){
		    writeSet[i].getBytes(b, offset);
		    offset += writeSet[i].getByteSize();
		}
    }

    public final int getByteSize(){
		int sz = UnsignedTypes.uint16Size;
		for (int i  = 0; i < writeSet.length; i++)
		    sz += writeSet[i].getByteSize();
		return sz;
    }
    
    public WriteSetEntry[] getWriteSet(){
    	return writeSet;
    }

    public WriteSetEntry getWriteSetEntry(int i ){
    	return writeSet[i];
    }

    public boolean isEmpty(){
        return writeSet.length == 0;
        
    }
    
    public int size(){
    	return this.writeSet.length;
    }
    
    public String toString(){
		String s="<";
		for (int i = 0; i < writeSet.length; i ++)
		    s+=writeSet[i]+(i<writeSet.length-1?", ":"");
		return s+">";
    }
    
    public Set<String> getInvariantRelatedKeys(){
    	Set<String> keys = new ObjectOpenHashSet();
		for (int i  = 0; i < writeSet.length; i++){
		    if(writeSet[i].isInvariantRelated()){
		    	keys.add(writeSet[i].obj);
		    }
		}
    	return keys;
    }

}

