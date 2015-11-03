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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mpi.vasco.coordination.VascoServiceAgentFactory;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.util.debug.Debug;

/**
 * The Class LockReply.
 */
public class LockReply {
	
	/** The op name. */
	String opName;
	
	int protocolType;
	
	/** The key counter map. 
	 * first level key -> table name + primary key
	 * second level key -> counter name (operation name requires stronger consistency semantics)
	 * value -> counter value
	 * */
	Map<String, Map<String, Long>> keyCounterMap;
	
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
	public LockReply(String _opName, int pType, Map<String, Map<String, Long>> _keyCounterMap){
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
		this.setKeyCounterMap(new Object2ObjectOpenHashMap<String, Map<String, Long>>(VascoServiceAgentFactory.SMALL_MAP_INITIAL_SIZE));
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
	public Map<String, Map<String, Long>> getKeyCounterMap() {
		return keyCounterMap;
	}

	/**
	 * Sets the key counter map.
	 *
	 * @param keyCounterMap the key counter map
	 */
	public void setKeyCounterMap(Map<String, Map<String, Long>> keyCounterMap) {
		this.keyCounterMap = keyCounterMap;
	}
	
	/**
	 * Adds the key counter pair.
	 *
	 * @param key the key
	 * @param counter the counter
	 */
	public void addKeyCounterPair(String keyStr, String conflictStr, long counter){
		//get counter per key
		Map<String, Long> counterMapPerKey = this.getKeyCounterMap().get(keyStr);
		if(counterMapPerKey == null){
			counterMapPerKey = new Object2ObjectOpenHashMap<String, Long>(VascoServiceAgentFactory.SMALL_MAP_INITIAL_SIZE);
			this.getKeyCounterMap().put(keyStr, counterMapPerKey);
		}
		
		if(!counterMapPerKey.containsKey(conflictStr)){
			counterMapPerKey.put(conflictStr, counter);
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
		int numOfKeys = dis.readInt();
		this.setKeyCounterMap(new Object2ObjectOpenHashMap<String, Map<String, Long>>());
		while(numOfKeys > 0){
			String keyStr = dis.readUTF();
			int numOfConflicts = dis.readInt();
			while(numOfConflicts > 0){
				String conflictOpStr = dis.readUTF();
				long counterValue = dis.readLong();
				this.addKeyCounterPair(keyStr, conflictOpStr, counterValue);
				numOfConflicts--;
			}
			numOfKeys--;
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
			
			for(String keyStr : this.getKeyCounterMap().keySet()){
				dos.writeUTF(keyStr);
				Map<String, Long> countersPerKey = this.getKeyCounterMap().get(keyStr);
				dos.writeInt(countersPerKey.size());
				for(String opName : countersPerKey.keySet()){
					dos.writeUTF(opName);
					dos.writeLong(countersPerKey.get(opName).longValue());
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
	
	private long getCounterByName(String compoundKey, String operationName){
		Map<String, Long> countersPerKey = this.getKeyCounterMap().get(compoundKey);
		Long counterObj = countersPerKey.get(operationName);
		if(counterObj == null){
			throw new RuntimeException("no such counter exists for key " + compoundKey);
		}else{
			return counterObj.longValue();
		}
	}
	
	public void aggreLockReplies(List<LockReply> lcReplies){
		if(lcReplies == null || lcReplies.isEmpty() || lcReplies.size() <= 1){
			Debug.println("\t\t\t ----> No need to aggregate all lock replies");
			return;
		}else{
			
			for(String keyStr : this.getKeyCounterMap().keySet()){
				Map<String, Long> countersPerKey = this.getKeyCounterMap().get(keyStr);
				for(String opName : countersPerKey.keySet()){
					long counterValue = countersPerKey.get(opName).longValue();
					
					for(LockReply lcReplyEntry : lcReplies){
						long cValue = lcReplyEntry.getCounterByName(keyStr, opName);
						counterValue += cValue;
					}
					countersPerKey.put(opName, new Long(counterValue));
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuilder strBuild = new StringBuilder();
		strBuild.append("<(OpName: ");
		strBuild.append(this.getOpName());
		strBuild.append("), (protocolType, ");
		strBuild.append(this.protocolType);
		strBuild.append("),");
		
		if(this.getKeyCounterMap() == null){
			strBuild.append("(barrierOps it depends on: {");
			for(ProxyTxnId barrierId : this.getBarrierInstancesForWait()){
				strBuild.append(barrierId.toString());
				strBuild.append(',');
			}
			
		}else{
			strBuild.append("(keys-value pairs: {");
			for(String keyStr : this.getKeyCounterMap().keySet()){
				strBuild.append("(key: ");
				strBuild.append(keyStr);
				strBuild.append(", counters: {");
				Map<String, Long> countersPerKey = this.getKeyCounterMap().get(keyStr);
				for(String opName : countersPerKey.keySet()){
					strBuild.append('(');
					strBuild.append(opName);
					strBuild.append(',');
					strBuild.append(countersPerKey.get(opName).longValue());
					strBuild.append("),");
				}
				strBuild.deleteCharAt(strBuild.length() - 1);
				strBuild.append("}), ");
			}
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
