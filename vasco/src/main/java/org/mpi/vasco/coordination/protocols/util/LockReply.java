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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mpi.vasco.txstore.util.ProxyTxnId;

/**
 * The Class LockReply.
 */
public class LockReply {
	
	/** The op name. */
	String opName;
	
	int protocolType;
	
	/** The key counter map. 
	 * first level key -> table name
	 * second level key -> primary keys in that table
	 * third level key -> counter name (operation name requires stronger consistency semantics)
	 * value -> counter value
	 * */
	Map<String, Map<String, Map<String, Long>>> keyCounterMap;
	
	/*This field is used by the non-barrier op in asym protocol, do not need to encode */
	Set<ProxyTxnId> barrierInstancesForWait;
	
	/** The arr. */
	byte[] arr;
	
	/**
	 * Instantiates a new lock reply.
	 *
	 * @param _opName the _op name
	 * @param _keyCounterMap the _key counter map
	 */
	public LockReply(String _opName, int pType, Map<String, Map<String, Map<String, Long>>> _keyCounterMap){
		this.setOpName(_opName);
		this.setProtocolType(pType);
		this.setKeyCounterMap(_keyCounterMap);
		this.setArr(null);
	}
	
	/**
	 * Instantiates a new lock reply.
	 *
	 * @param _opName the _op name
	 */
	public LockReply(String _opName, int pType){
		this.setOpName(_opName);
		this.setProtocolType(pType);
		this.setKeyCounterMap(new Object2ObjectOpenHashMap<String, Map<String, Map<String, Long>>>());
		this.setArr(null);
	}
	
	/*Only used locally*/
	public LockReply(String _opName, int pType, Set<ProxyTxnId> barrierIds){
		this.setOpName(_opName);
		this.setProtocolType(pType);
		this.setBarrierInstancesForWait(barrierIds);
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
	public Map<String, Map<String, Map<String, Long>>> getKeyCounterMap() {
		return keyCounterMap;
	}

	/**
	 * Sets the key counter map.
	 *
	 * @param keyCounterMap the key counter map
	 */
	public void setKeyCounterMap(Map<String, Map<String, Map<String, Long>>> keyCounterMap) {
		this.keyCounterMap = keyCounterMap;
	}
	
	/**
	 * Adds the key counter pair.
	 *
	 * @param key the key
	 * @param counter the counter
	 */
	public void addKeyCounterPair(String keyGroup, String key, String conflictStr, long counter){
		//get table
		Map<String, Map<String, Long>> keyCounterMap = this.getKeyCounterMap().get(keyGroup);
		if(keyCounterMap == null){
			keyCounterMap = new Object2ObjectOpenHashMap<String, Map<String, Long>>();
			this.getKeyCounterMap().put(keyGroup, keyCounterMap);
		}
		
		//get key name
		Map<String, Long> counterSet = keyCounterMap.get(key);
		if(counterSet == null){
			counterSet = new HashMap<String, Long>();
			keyCounterMap.put(key, counterSet);
		}
		
		if(!counterSet.containsKey(conflictStr)){
			counterSet.put(conflictStr, counter);
		}else{
			throw new RuntimeException("Found duplicates\n");
		}
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
		this.setProtocolType(dis.readInt());
		int numOfKeyGroups = dis.readInt();
		this.setKeyCounterMap(new Object2ObjectOpenHashMap<String, Map<String, Map<String, Long>>>());
		while(numOfKeyGroups > 0){
			String keyGroupStr = dis.readUTF();
			int numOfKeys = dis.readInt();
			while(numOfKeys > 0){
				String key = dis.readUTF();
				int numOfConflicts = dis.readInt();
				while(numOfConflicts > 0){
					this.addKeyCounterPair(keyGroupStr, key, dis.readUTF(), dis.readLong());
					numOfConflicts--;
				}
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
			dos.writeInt(this.getProtocolType());
			dos.writeInt(this.getKeyCounterMap().size());
			Iterator it = this.getKeyCounterMap().entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String, HashMap<String, HashMap<String, Long>>> e = (Entry<String, HashMap<String, HashMap<String, Long>>>) it.next();
				dos.writeUTF(e.getKey());
				dos.writeInt(e.getValue().size());
				Set<String> keySet = e.getValue().keySet();
				for(String key : keySet){
					dos.writeUTF(key);
					HashMap<String, Long> counterSet = e.getValue().get(key);
					dos.writeInt(counterSet.size());
					for(Map.Entry<String, Long> counter : counterSet.entrySet()){
						dos.writeUTF(counter.getKey());
						dos.writeLong(counter.getValue().longValue());
					}
					
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
	
	private long getCounterByName(String tableName, String primaryKeyName, String operationName){
		Map<String, Map<String, Long>> secondLevelMap = this.getKeyCounterMap().get(tableName);
		if(secondLevelMap != null){
			Map<String, Long> thirdLevelMap = secondLevelMap.get(primaryKeyName);
			if(thirdLevelMap != null){
				if(thirdLevelMap.containsKey(operationName)){
					return thirdLevelMap.get(operationName).longValue();
				}
			}
		}
		return -1;
	}
	
	public void aggreLockReplies(List<LockReply> lcReplies){
		if(lcReplies == null || lcReplies.isEmpty()){
			return;
		}else{
			Iterator it = this.getKeyCounterMap().entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String, HashMap<String, HashMap<String, Long>>> e = (Entry<String, HashMap<String, HashMap<String, Long>>>) it.next();
				//table name
				String tableName = e.getKey();
				Set<String> keySet = e.getValue().keySet();
				for(String key : keySet){
					//enumerate all primary keys
					//check all counters
					HashMap<String, Long> counterSet = e.getValue().get(key);
					for(Map.Entry<String, Long> counter : counterSet.entrySet()){
						//conflict operation name
						String operationName = counter.getKey();
						long counterValue = counter.getValue().longValue();
						//aggregate here:
						
						for(LockReply lcReplyEntry : lcReplies){
							long cValue = lcReplyEntry.getCounterByName(tableName, key, operationName);
							if(cValue == -1){
								throw new RuntimeException("one lc reply doesn't observe the following counter " + tableName + ", " + key + ", " + operationName);
							}else{
								counterValue += cValue;
							}
						}
						counterSet.put(operationName, new Long(counterValue));
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuilder strBuild = new StringBuilder();
		strBuild.append("<(OpName, ");
		strBuild.append(this.getOpName());
		strBuild.append("(protocolType, ");
		strBuild.append(this.protocolType);
		strBuild.append("),");
		strBuild.append("), (keys, {");
		Iterator it = this.getKeyCounterMap().entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, HashMap<String, HashMap<String, Long>>> e = (Entry<String, HashMap<String, HashMap<String, Long>>>) it.next();
			strBuild.append("<keyGroups: ");
			strBuild.append(e.getKey());
			strBuild.append(", {");
			Set<String> keySet = e.getValue().keySet();
			for(String key : keySet){
				strBuild.append("key: ");
				strBuild.append(key);
				strBuild.append(", counters:  ");
				HashMap<String, Long> counterSet = e.getValue().get(key);
				for(Map.Entry<String, Long> counter : counterSet.entrySet()){
					strBuild.append('(');
					strBuild.append(counter.getKey());
					strBuild.append(',');
					strBuild.append(counter.getValue().longValue());
					strBuild.append("),");
				}
			}
			strBuild.deleteCharAt(strBuild.length() - 1);
			strBuild.append("}, ");
		}
		strBuild.deleteCharAt(strBuild.length() - 1);
		strBuild.append("})>");
		return strBuild.toString();
	}

	public int getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(int protocolType) {
		this.protocolType = protocolType;
	}

	public Set<ProxyTxnId> getBarrierInstancesForWait() {
		return barrierInstancesForWait;
	}

	public void setBarrierInstancesForWait(Set<ProxyTxnId> barrierInstancesForWait) {
		this.barrierInstancesForWait = barrierInstancesForWait;
	}

}
