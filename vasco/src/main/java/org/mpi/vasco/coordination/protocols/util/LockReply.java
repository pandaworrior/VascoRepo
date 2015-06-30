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
package org.mpi.vasco.coordination.protocols.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

// TODO: Auto-generated Javadoc
/**
 * The Class LockReply.
 */
public class LockReply {
	
	/** The op name. */
	String opName;
	
	/** The key counter map. */
	HashMap<String, Integer> keyCounterMap;
	
	/** The arr. */
	byte[] arr;
	
	/** The has decoded. */
	boolean hasDecoded;
	
	/**
	 * Instantiates a new lock reply.
	 *
	 * @param _opName the _op name
	 * @param _keyCounterMap the _key counter map
	 */
	public LockReply(String _opName, HashMap<String, Integer> _keyCounterMap){
		this.setOpName(_opName);
		this.setKeyCounterMap(_keyCounterMap);
		this.setHasDecoded(false);
	}
	
	/**
	 * Instantiates a new lock reply.
	 *
	 * @param _opName the _op name
	 */
	public LockReply(String _opName){
		this.setOpName(_opName);
		this.setKeyCounterMap(new HashMap<String, Integer>());
		this.setHasDecoded(false);
	}
	
	/**
	 * Instantiates a new lock reply.
	 *
	 * @param b the b
	 * @param offset the offset
	 */
	public LockReply(byte[] b, int offset){
		
	}

	/**
	 * Gets the op name.
	 *
	 * @return the op name
	 */
	public String getOpName() {
		return opName;
	}

	/**
	 * Sets the op name.
	 *
	 * @param opName the new op name
	 */
	public void setOpName(String opName) {
		this.opName = opName;
	}

	/**
	 * Gets the key counter map.
	 *
	 * @return the key counter map
	 */
	public HashMap<String, Integer> getKeyCounterMap() {
		return keyCounterMap;
	}

	/**
	 * Sets the key counter map.
	 *
	 * @param keyCounterMap the key counter map
	 */
	public void setKeyCounterMap(HashMap<String, Integer> keyCounterMap) {
		this.keyCounterMap = keyCounterMap;
	}
	
	/**
	 * Adds the key counter pair.
	 *
	 * @param key the key
	 * @param counter the counter
	 */
	public void addKeyCounterPair(String key, int counter){
		this.getKeyCounterMap().put(key, counter);
	}
	
	/**
	 * Decode.
	 */
	public void decode(){
		this.setArr(null);
	}
	
	/**
	 * Gets the bytes.
	 *
	 * @return the bytes
	 */
	public byte[] getBytes(){
		if(!this.isHasDecoded()){
			this.decode();
		}
		return this.getArr();
	}
	
	/**
	 * Gets the byte size.
	 *
	 * @return the byte size
	 */
	public int getByteSize(){
		return this.getBytes().length;
	}

	/**
	 * Gets the arr.
	 *
	 * @return the arr
	 */
	private byte[] getArr() {
		return arr;
	}

	/**
	 * Sets the arr.
	 *
	 * @param arr the new arr
	 */
	public void setArr(byte[] arr) {
		this.arr = arr;
	}

	/**
	 * Checks if is checks for decoded.
	 *
	 * @return true, if is checks for decoded
	 */
	public boolean isHasDecoded() {
		return hasDecoded;
	}

	/**
	 * Sets the checks for decoded.
	 *
	 * @param hasDecoded the new checks for decoded
	 */
	public void setHasDecoded(boolean hasDecoded) {
		this.hasDecoded = hasDecoded;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuilder strBuild = new StringBuilder();
		strBuild.append("OpName: " + this.getOpName()+"\t");
		Iterator it = this.getKeyCounterMap().entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Integer> e = (Entry<String, Integer>) it.next();
			strBuild.append(e.getKey() + " -> " + e.getValue().intValue() + "\t");
		}
		return strBuild.toString();
	}

}
