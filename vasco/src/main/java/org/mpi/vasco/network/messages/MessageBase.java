/*******************************************************************************
 * Copyright (c) 2015 Dependable Cloud Group and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dependable Cloud Group - initial API and implementation
 *
 * Creator:
 *     Cheng Li
 *
 * Contact:
 *     chengli@mpi-sws.org    
 *******************************************************************************/
// $Id: MessageBase.java 510 2011-07-04 11:01:22Z chengli $


package org.mpi.vasco.network.messages;

import org.mpi.vasco.txstore.messages.MessageTags;
import org.mpi.vasco.util.UnsignedTypes;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class MessageBase.
 */
public abstract class MessageBase implements Message{

    /**
     *        Constructor takes the message tag and the size of the payload.
     *
     * @param _tag the _tag
     * @param _size the _size
     */
    public MessageBase(int _tag, int _size){
	tag = _tag;
	size = _size;
	bytes = new byte[getTotalSize()];
	// add the tag to the byte array
	UnsignedTypes.intToBytes(tag, bytes, 0);
	// add the size to the byte array
	UnsignedTypes.longToBytes(size, bytes, UnsignedTypes.uint16Size);
    }
    
    /**
     * Config.
     *
     * @param _tag the _tag
     * @param _size the _size
     */
    public void config(int _tag, int _size){
    	tag = _tag;
    	size = _size;
    	bytes = new byte[getTotalSize()];
    	// add the tag to the byte array
    	UnsignedTypes.intToBytes(tag, bytes, 0);
    	// add the size to the byte array
    	UnsignedTypes.longToBytes(size, bytes, UnsignedTypes.uint16Size);
    }
    
    /**
     * Instantiates a new message base.
     *
     * @param bits the bits
     */
    public MessageBase(byte[] bits){
	this(bits, 0);
    }

    /**
     * Instantiates a new message base.
     *
     * @param bits the bits
     * @param offset the offset
     */
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
    
    /**
     * Decode message.
     *
     * @param bits the bits
     * @param offset the offset
     */
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

    /** The tag. */
    private int tag;
    
    /** The size. */
    private int size;
    
    /** The bytes. */
    private byte[] bytes;

    /**
     *  
     * 	returns the total size of the byte representation of the message .
     *
     * @return the total size
     */
    final public int getTotalSize(){
	return getPayloadSize() + getOffset();
    }
    
    /**
     *  
     * returns the offset that subclasses should use in order to start
     * modifying the underlying byte array.
     *
     * @return the offset
     */
    final static public  int getOffset(){
	return baseSize;
    }

    /**
     * Equals.
     *
     * @param m the m
     * @return true, if successful
     */
    public boolean equals(MessageBase m){
	boolean res = tag == m.tag && size == m.size &&
	    bytes.length == m.bytes.length;
	for (int i = 0; i < bytes.length && res; i++)
	    res = res && bytes[i] == m.bytes[i];
	return res;
    }

    /**
     * Matches.
     *
     * @param m the m
     * @return true, if successful
     */
    public boolean matches(MessageBase m){
    	Debug.kill(new RuntimeException("Not Yet Implemented"));
	return false;
    }

    /**
     * Checks if is valid.
     *
     * @return true, if is valid
     */
    public boolean isValid(){
	return true;
    }

    /* (non-Javadoc)
     * @see org.mpi.vasco.network.messages.Message#getBytes()
     */
    final public byte[] getBytes() {
	return bytes;
    }

    //    abstract public int getSender();


    /**
     * Gets the tag.
     *
     * @return the tag
     */
    public int getTag(){ return tag;}
    
    /**
     * Gets the payload size.
     *
     * @return the payload size
     */
    public int getPayloadSize(){ return size;}

    /** The Constant baseSize. */
    private final static int baseSize = 
	UnsignedTypes.uint16Size + UnsignedTypes.uint32Size;
    
    //reset a message for making the object reusable
    /**
     * Reset.
     */
    public abstract void reset();


    /**
     * Gets the tag string.
     *
     * @return the tag string
     */
    public String getTagString(){
	return MessageTags.getString(tag);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(){
	return "<MB, tag:"+getTagString()+", payloadSize:"+getPayloadSize()+", sender:"+
	    //getSender()+
	    ">";
    }

}



