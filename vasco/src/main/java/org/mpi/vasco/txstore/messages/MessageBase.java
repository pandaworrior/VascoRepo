// $Id: MessageBase.java 510 2011-07-04 11:01:22Z chengli $


package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.util.UnsignedTypes;
import org.mpi.vasco.util.debug.Debug;

public abstract class MessageBase implements Message{

    /**
       Constructor takes the message tag and the size of the payload
     **/
    public MessageBase(int _tag, int _size){
	tag = _tag;
	size = _size;
	bytes = new byte[getTotalSize()];
	// add the tag to the byte array
	UnsignedTypes.intToBytes(tag, bytes, 0);
	// add the size to the byte array
	UnsignedTypes.longToBytes(size, bytes, UnsignedTypes.uint16Size);
    }
    
    public void config(int _tag, int _size){
    	tag = _tag;
    	size = _size;
    	bytes = new byte[getTotalSize()];
    	// add the tag to the byte array
    	UnsignedTypes.intToBytes(tag, bytes, 0);
    	// add the size to the byte array
    	UnsignedTypes.longToBytes(size, bytes, UnsignedTypes.uint16Size);
    }
    
    public MessageBase(byte[] bits){
	this(bits, 0);
    }

    public MessageBase(byte[] bits, int offset){
	tag = UnsignedTypes.bytesToInt(bits, offset);
	size = (int) UnsignedTypes.bytesToLong(bits, offset+UnsignedTypes.uint16Size);
	// creating a copy of the byte array for unsettling and
	// upsetting reasons current system invariant is that if the
	// constructor finishes, the byte array exists.  I'm not sure
	// that this is strictly needed, however.
	bytes = new byte[size + UnsignedTypes.uint16Size + UnsignedTypes.uint32Size];
	for (int i = 0; i < bytes.length; i++)
	    bytes[i] = bits[offset+i];
    }
    
    public void decodeMessage(byte[] bits, int offset){
    	tag = UnsignedTypes.bytesToInt(bits, offset);
    	size = (int) UnsignedTypes.bytesToLong(bits, offset+UnsignedTypes.uint16Size);
    	// creating a copy of the byte array for unsettling and
    	// upsetting reasons current system invariant is that if the
    	// constructor finishes, the byte array exists.  I'm not sure
    	// that this is strictly needed, however.
    	bytes = new byte[size + UnsignedTypes.uint16Size + UnsignedTypes.uint32Size];
    	for (int i = 0; i < bytes.length; i++)
    	    bytes[i] = bits[offset+i];
    }

    private int tag;
    private int size;
    private byte[] bytes;

    /** 
	returns the total size of the byte representation of the message 
    **/
    final public int getTotalSize(){
	return getPayloadSize() + getOffset();
    }
    
    /** 
     * returns the offset that subclasses should use in order to start
     * modifying the underlying byte array
     **/
    final static public  int getOffset(){
	return baseSize;
    }

    public boolean equals(MessageBase m){
	boolean res = tag == m.tag && size == m.size &&
	    bytes.length == m.bytes.length;
	for (int i = 0; i < bytes.length && res; i++)
	    res = res && bytes[i] == m.bytes[i];
	return res;
    }

    public boolean matches(MessageBase m){
    	Debug.kill(new RuntimeException("Not Yet Implemented"));
	return false;
    }

    public boolean isValid(){
	return true;
    }

    final public byte[] getBytes() {
	return bytes;
    }

    //    abstract public int getSender();


    public int getTag(){ return tag;}
    public int getPayloadSize(){ return size;}

    private final static int baseSize = 
	UnsignedTypes.uint16Size + UnsignedTypes.uint32Size;


    public String getTagString(){
	return MessageTags.getString(tag);
    }

    public String toString(){
	return "<MB, tag:"+getTagString()+", payloadSize:"+getPayloadSize()+", sender:"+
	    //getSender()+
	    ">";
    }

}



