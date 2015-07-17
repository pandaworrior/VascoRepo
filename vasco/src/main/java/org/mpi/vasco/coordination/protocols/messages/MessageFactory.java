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
package org.mpi.vasco.coordination.protocols.messages;
import org.mpi.vasco.network.messages.MessageBase;


import org.mpi.vasco.util.ObjectPool;
import org.mpi.vasco.util.UnsignedTypes;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating Message objects.
 */
public class MessageFactory{

	/** The Lock req m pool. */
	public ObjectPool<LockReqMessage> LockReqMPool;
	
	/** The Lock rep m pool. */
	public ObjectPool<LockRepMessage> LockRepMPool;

    /**
     * Instantiates a new message factory.
     */
    public MessageFactory(){
    	LockReqMPool = new ObjectPool<LockReqMessage>();
    	LockRepMPool = new ObjectPool<LockRepMessage>();
    }

    /**
     * From bytes.
     *
     * @param bytes the bytes
     * @return the message base
     */
    public MessageBase fromBytes(byte[] bytes){
		int offset = 0;
		int tag = UnsignedTypes.bytesToInt(bytes, offset);
		offset += UnsignedTypes.uint16Size;
	
	
		switch(tag){
		case MessageTags.LOCKREQ:
			LockReqMessage qMsg = LockReqMPool.borrowObject();
			if( qMsg == null)
				return new LockReqMessage(bytes);
			else{
				qMsg.decodeMessage(bytes);
				return qMsg;
			}
		case MessageTags.LOCKREP:
			LockRepMessage pMsg = LockRepMPool.borrowObject();
			if(pMsg == null)
				return new LockRepMessage(bytes);
			else{
				pMsg.decodeMessage(bytes);
				return pMsg;
			}
		default:
	
		    throw new RuntimeException("Invalid message tag:  "+tag);
	            
		}
	
    }
    
    /**
     * Borrow lock req message.
     *
     * @return the lock req message
     */
    public LockReqMessage borrowLockReqMessage(){
    	return LockReqMPool.borrowObject();
    }
    
    /**
     * Return lock req message.
     *
     * @param msg the msg
     */
    public void returnLockReqMessage(LockReqMessage msg){
    	msg.reset();
    	LockReqMPool.returnObject(msg);	
    }
    
    /**
     * Borrow lock rep message.
     *
     * @return the lock rep message
     */
    public LockRepMessage borrowLockRepMessage(){
    	return LockRepMPool.borrowObject();
    }
    
    /**
     * Return lock rep message.
     *
     * @param msg the msg
     */
    public void returnLockRepMessage(LockRepMessage msg){
    	msg.reset();
    	LockRepMPool.returnObject(msg);
    }

}