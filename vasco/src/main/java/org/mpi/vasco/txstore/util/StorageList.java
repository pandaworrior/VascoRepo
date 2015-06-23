package org.mpi.vasco.txstore.util;

import java.util.Vector;
import org.mpi.vasco.util.UnsignedTypes;

public class StorageList{

    int count;
        Vector<Integer> list;

    public StorageList(){
	count = 0;
	list = new Vector<Integer>();
    }
    
    public void reset(){
    	count = 0;
    	list.clear();
    }

    public StorageList(byte[] b, int offset){
	count = UnsignedTypes.bytesToInt(b, offset);
	if (offset + UnsignedTypes.uint16Size * (count+1) >b.length)
	    throw new RuntimeException("byte array not long enough");
	offset += UnsignedTypes.uint16Size;
	list = new Vector<Integer>();
	for (int i = 0; i < count; i++, offset += UnsignedTypes.uint16Size){
	    list.add(new Integer(UnsignedTypes.bytesToInt(b, offset)));
	}
    }

    public int getStorageCount(){
	return count;
    }
    
    public Vector<Integer> getStorageList(){
    	return list;
    }

    public int getStorage(int i){
	return list.elementAt(i).intValue();

    }

    public Integer getStorageInteger(int i){
	return list.elementAt(i);
    }

    public boolean isEmpty(){
        return list.isEmpty();
    }
    
    public void remove(int id){
        list.remove(new Integer(id));
    }
    public void addStorage(int id){
	Integer tmp = new Integer(id);
	if (list.contains(tmp))
	    return;
	list.add(tmp);
	count = list.size();
    }

    public void getBytes(byte[] b, int offset){
	if (offset + getByteSize() > b.length)
	    throw new RuntimeException("byte array not big enough");
	UnsignedTypes.intToBytes(count, b, offset);
	offset += UnsignedTypes.uint16Size;
	for (int i = 0; i < count; i++, offset += UnsignedTypes.uint16Size)
	    UnsignedTypes.intToBytes(list.elementAt(i).intValue() , b, offset);
    }
    

    public int getByteSize(){
	return UnsignedTypes.uint16Size * (count+1);
    }

    public String toString(){
	String tmp = "[";
	for (int i = 0; i < list.size(); i++)
	    tmp = tmp +", " +list.elementAt(i);
	return tmp +"]";
    }
    
}