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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The Class LockReply.
 */
public class LockReply {
	
	/** The op name. */
	String opName;
	
	/** The key counter map. 
	 * key -> table name
	 * value -> hashmap from key to counter
	 * */
	HashMap<String, HashMap<String, Long>> keyCounterMap;
	
	/** The arr. */
	byte[] arr;
	
	/**
	 * Instantiates a new lock reply.
	 *
	 * @param _opName the _op name
	 * @param _keyCounterMap the _key counter map
	 */
	public LockReply(String _opName, HashMap<String, HashMap<String, Long>> _keyCounterMap){
		this.setOpName(_opName);
		this.setKeyCounterMap(_keyCounterMap);
		this.setArr(null);
	}
	
	/**
	 * Instantiates a new lock reply.
	 *
	 * @param _opName the _op name
	 */
	public LockReply(String _opName){
		this.setOpName(_opName);
		this.setKeyCounterMap(new HashMap<String, HashMap<String, Long>>());
		this.setArr(null);
	}
	
	/**
	 * Instantiates a new lock reply.
	 *
	 * @param b the b
	 * @param offset the offset
	 */
	public LockReply(byte[] b, int offset){
		this.setArr(new byte[b.length - offset]);
		System.arraycopy(b, offset, this.getArr(), 0, b.length - offset);
		try {
			this.decode();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
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
	public HashMap<String, HashMap<String, Long>> getKeyCounterMap() {
		return keyCounterMap;
	}

	/**
	 * Sets the key counter map.
	 *
	 * @param keyCounterMap the key counter map
	 */
	public void setKeyCounterMap(HashMap<String, HashMap<String, Long>> keyCounterMap) {
		this.keyCounterMap = keyCounterMap;
	}
	
	/**
	 * Adds the key counter pair.
	 *
	 * @param key the key
	 * @param counter the counter
	 */
	public void addKeyCounterPair(String keyGroup, String key, long counter){
		HashMap<String, Long> keyCounter = this.getKeyCounterMap().get(keyGroup);
		if(keyCounter == null){
			keyCounter = new HashMap<String, Long>();
			this.getKeyCounterMap().put(keyGroup, keyCounter);
		}
		keyCounter.put(key, counter);
	}
	
	/**
	 * Decode.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void decode() throws IOException{
		ByteArrayInputStream bais = new ByteArrayInputStream(this.getArr());
		DataInputStream dis = new DataInputStream(bais);
		this.setOpName(dis.readUTF());
		int numOfKeyGroups = dis.readInt();
		this.setKeyCounterMap(new HashMap<String, HashMap<String, Long>>());
		while(numOfKeyGroups > 0){
			String keyGroupStr = dis.readUTF();
			int numOfKeys = dis.readInt();
			while(numOfKeys > 0){
				this.addKeyCounterPair(keyGroupStr, dis.readUTF(), dis.readLong());
				numOfKeys--;
			}
			numOfKeyGroups--;
		}
	}
	
	/**
	 * Encode.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void encode() throws IOException{
		if(this.getArr() == null){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeUTF(this.getOpName());
			dos.writeInt(this.getKeyCounterMap().size());
			Iterator it = this.getKeyCounterMap().entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String, HashMap<String, Long>> e = (Entry<String, HashMap<String, Long>>) it.next();
				dos.writeUTF(e.getKey());
				dos.writeInt(e.getValue().size());
				Set<String> keySet = e.getValue().keySet();
				for(String key : keySet){
					dos.writeUTF(key);
					dos.writeLong(e.getValue().get(key).longValue());
				}
			}
			this.setArr(baos.toByteArray());
		}
	}
	
	/**
	 * Gets the bytes.
	 *
	 * @return the bytes
	 */
	public byte[] getBytes(){
		try {
			this.encode();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuilder strBuild = new StringBuilder();
		strBuild.append("<(OpName, " + this.getOpName()+"), (keys, {");
		Iterator it = this.getKeyCounterMap().entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, HashMap<String, Long>> e = (Entry<String, HashMap<String, Long>>) it.next();
			strBuild.append("<keyGroups: ");
			strBuild.append(e.getKey());
			strBuild.append(", {");
			Iterator keyIt = e.getValue().entrySet().iterator();
			while(keyIt.hasNext()){
				Entry<String, Long> keyEntry = (Entry<String, Long>) keyIt.next();
				strBuild.append('(');
				strBuild.append(keyEntry.getKey());
				strBuild.append(',');
				strBuild.append(keyEntry.getValue().longValue());
				strBuild.append("),");
			}
			strBuild.deleteCharAt(strBuild.length() - 1);
			strBuild.append("}, ");
		}
		strBuild.deleteCharAt(strBuild.length() - 1);
		strBuild.append("})>");
		return strBuild.toString();
	}

}
