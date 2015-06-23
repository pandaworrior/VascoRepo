package org.mpi.vasco.txstore.util;

import org.mpi.vasco.util.UnsignedTypes;

public class OperationLog{


    byte[] bits;

    public OperationLog(byte b[]){
	bits = b;
    }

    public OperationLog(byte b[], int offset){
	int l = (int) (UnsignedTypes.bytesToLong(b, offset));
	offset += UnsignedTypes.uint32Size;
	bits = new byte[l];
	for (int i = 0; i < bits.length; i++)
	    bits[i] = b[i+offset];
    }

    public byte[] getOperationLogBytes(){
	return bits;
    }

    public void getBytes(byte[] b, int offset){
	UnsignedTypes.longToBytes(bits.length, b, offset);
	offset += UnsignedTypes.uint32Size;
	for (int i = 0; i < bits.length; i++)
	    b[i+offset] = bits[i];
    }

    public final int getByteSize(){
	return bits.length + UnsignedTypes.uint32Size;

    }
    

}

