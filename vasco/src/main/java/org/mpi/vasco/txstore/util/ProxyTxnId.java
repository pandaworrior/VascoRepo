package org.mpi.vasco.txstore.util;

import org.mpi.vasco.util.UnsignedTypes;



public class ProxyTxnId {

    int dc; // identifier for the intiating dc
    int proxy; // identifier for the issuing proxy
    long count;  // proxy specified identifier
    
    byte[] _bytes;

    public ProxyTxnId(){
	this(0,0,0);
    }

    public ProxyTxnId(int dc, int proxy, long count){
	this.dc = dc;
	this.proxy = proxy;
	this.count = count;


    }

    public ProxyTxnId(byte[] b){
	this(b, 0);
	if (b.length != getByteSize())
	    throw new RuntimeException("invalid byte array size");
	_bytes = b;
    }


    /**
       This constructur is intended for use with in place byte arrays
       in order to avoid excessive creation/deletion of minor byte
       arrays.
     **/
    public ProxyTxnId(byte[] b, int offset){
	this.dc = UnsignedTypes.bytesToInt(b, offset);
	this.proxy = UnsignedTypes.bytesToInt(b, offset+UnsignedTypes.uint16Size);
	this.count = UnsignedTypes.bytesToLongLong(b, offset + UnsignedTypes.uint16Size*2);
    }

    public byte[] getBytes(){
	if (_bytes == null){
	    _bytes = new byte[getByteSize()];
	}
	return _bytes;
    }

    public int getDatacenterId(){
	return dc;
    }

    public int getProxyId(){
	return proxy;
    }

    public long getCount(){
	return count;
    }
    
    public int getDcId(){
    	return dc;
    }


    // this function is used for in place byte generation to avoid excessive byte array copies.
    public void getBytes(byte[] b, int offset){
	UnsignedTypes.intToBytes(dc, b, offset);
	UnsignedTypes.intToBytes(proxy, b, offset+UnsignedTypes.uint16Size);
	UnsignedTypes.longlongToBytes(count, b, offset+UnsignedTypes.uint16Size*2);
    }


    public boolean equals(ProxyTxnId pti){
	return pti.dc == dc && pti.proxy == pti.proxy && pti.count == pti.count;
    }

    public boolean equals(Object pti){
	if (pti instanceof ProxyTxnId)
	    return equals((ProxyTxnId) pti);
	else
	    return false;
    }

    public  int hashCode(){
	long tmp = count * 1000 + proxy * 10 + dc;
	return (int) tmp;
    }

    public String toString(){
	return ""+dc+"-"+proxy+"-"+count;
    }

    public final int getByteSize(){
	return UnsignedTypes.uint16Size +
	    UnsignedTypes.uint16Size +
	    UnsignedTypes.uint64Size;
    }
}