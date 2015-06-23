package org.mpi.vasco.txstore.util;

import org.mpi.vasco.util.UnsignedTypes;

import java.util.Vector;
import java.util.Set;

public class ReadSet{

    ReadSetEntry readSet[];

    public ReadSet(ReadSetEntry[] rs){
	readSet = rs;
    }

    static ReadSetEntry[] _rse= new ReadSetEntry[0];
    public ReadSet(Vector<ReadSetEntry> rs){
	this(rs.toArray(_rse));
    }

    
    public ReadSet(Set<ReadSetEntry> rs){
	this(rs.toArray(_rse));
    }

    public ReadSet(byte b[], int offset){

	int sz = UnsignedTypes.bytesToInt(b, offset);
	offset += UnsignedTypes.uint16Size;


	readSet = new ReadSetEntry[sz];
	for (int i = 0; i < sz; i++){
	    readSet[i] = new ReadSetEntry(b, offset);
	    offset += readSet[i].getByteSize();
	}
    }

    public void getBytes(byte[] b, int offset){
	UnsignedTypes.intToBytes(readSet.length, b, offset);
	offset += UnsignedTypes.uint16Size;
	for (int i = 0; i < readSet.length; i++){
	    readSet[i].getBytes(b, offset);
	    offset += readSet[i].getByteSize();

	}
	

    }

    public final int getByteSize(){
	int sz = UnsignedTypes.uint16Size;
	for (int i  = 0; i < readSet.length; i++)
	    sz += readSet[i].getByteSize();
	return sz;
    }
    
    public ReadSetEntry[] getReadSet(){
	return readSet;
    }

    public ReadSetEntry getReadSetEntry(int i ){
	return readSet[i];
    }

    public String toString(){
	String s="<";
	for (int i = 0; i < readSet.length; i ++)
	    s+=readSet[i]+(i<readSet.length-1?", ":"");
	return s+">";
    }
    
}

