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
package org.mpi.vasco.coordination.protocols;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mpi.vasco.coordination.MessageHandlerClientSide;
import org.mpi.vasco.coordination.VascoServiceAgentFactory;
import org.mpi.vasco.coordination.protocols.messages.LockReqMessage;
import org.mpi.vasco.coordination.protocols.util.LockReply;
import org.mpi.vasco.coordination.protocols.util.LockRequest;
import org.mpi.vasco.coordination.protocols.util.Protocol;
import org.mpi.vasco.coordination.protocols.util.SymMetaData;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.util.debug.Debug;

/**
 * The Class SymProtocol.
 */
public class SymProtocol extends Protocol{
	
	private ConcurrentHashMap<ProxyTxnId, SymMetaData> symMetaDataMap;
	
	//locally maintain a copy of data on the centralized server
	// first level key : tablename + primary key
	// second level key : opName
	// second level value : counter for <first_level_key, second_level_key>
    protected Map<String, Map<String, Long>> countersLocalCopy;
	
	/**
	 * Instantiates a new sym protocol.
	 *
	 * @param xmlFile the xml file
	 * @param clientId the client id
	 */
	public SymProtocol(MessageHandlerClientSide c) {
		super(c);
		this.symMetaDataMap = new ConcurrentHashMap<ProxyTxnId, SymMetaData>(VascoServiceAgentFactory.BIG_MAP_INITIAL_SIZE);
		this.setCountersLocalCopy(new Object2ObjectOpenHashMap<String, Map<String, Long>>(VascoServiceAgentFactory.BIG_MAP_INITIAL_SIZE));
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.Protocol#getPermission(org.mpi.vasco.txstore.util.ProxyTxnId, org.mpi.vasco.coordination.protocols.util.LockRequest)
	 */
	@Override
	public LockReply getPermission(ProxyTxnId txnId, LockRequest lcR) {
		LockReqMessage msg = new LockReqMessage(txnId,
				client.getMyId(), lcR);
		SymMetaData meta = this.addInitialMetaData(txnId, lcR);
		client.sendToLockServer(msg);
		synchronized(meta){
			while(meta.getLockReply() == null){
				try {
					meta.wait(VascoServiceAgentFactory.RESPONSE_WAITING_TIME_IN_MILL_SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		//return the lcReply and remove it from the set
		this.cleanUpMetaData(txnId);
		return meta.getLockReply();
	}

	@Override
	public void addLockReply(ProxyTxnId txnId, LockReply lcReply) {
		SymMetaData meta = this.symMetaDataMap.get(txnId);
		if(meta == null){
			throw new RuntimeException("Meta for " + txnId.toString() + " doesn't exist");
		}
		synchronized(meta){
			meta.setLockReply(lcReply);
			meta.notify();
		}
	}
	
	public SymMetaData addInitialMetaData(ProxyTxnId txnId, LockRequest lcR){
		Debug.println("Initially added " + txnId.toString());
		SymMetaData meta = new SymMetaData(lcR, null);
		this.symMetaDataMap.put(txnId, meta);
		return meta;
	}
	
	private void cleanUpMetaData(ProxyTxnId txnId){
		this.symMetaDataMap.remove(txnId);
	}

	@Override
	public LockReply getLocalPermission(ProxyTxnId txnId, LockRequest lcR) {
		throw new RuntimeException("Not implemented yet!");
	}
	
	private void incrementLocalCounterByOne(Set<String> tableKeyMap,
			String operationName){
		synchronized(this.countersLocalCopy){
			for(String keyStr : tableKeyMap){
				Map<String, Long> countersPerKey = this.countersLocalCopy.get(keyStr);
				if(countersPerKey == null){
					countersPerKey = new Object2ObjectOpenHashMap<String, Long>(VascoServiceAgentFactory.SMALL_MAP_INITIAL_SIZE);
					this.countersLocalCopy.put(keyStr, countersPerKey);
				}
				Long counterValue = countersPerKey.get(operationName);
				if(counterValue == null){
					counterValue = new Long(1L);
				}else{
					counterValue = new Long(counterValue.longValue() + 1);
				}
				countersPerKey.put(operationName, counterValue);
			}
		}
	}

	@Override
	public void cleanUp(ProxyTxnId txnId) {
		//update the local counter, how to change the remote?
		LockRequest lcR = this.symMetaDataMap.get(txnId).getLockRequest();
		this.incrementLocalCounterByOne(lcR.getKeyList(), lcR.getOpName());
	}
	
	private boolean isCounterMatching(Map<String, Map<String, Long>> keyCounters){
		
		Iterator itKeyCounters = keyCounters.entrySet().iterator();
		while(itKeyCounters.hasNext()){
			Entry<String, Map<String, Long>> keyCountersEntry = (Entry<String, Map<String, Long>>) itKeyCounters.next();
			String keyStr = keyCountersEntry.getKey();
			Map<String, Long> coutersPerKeyByRequest = keyCountersEntry.getValue();
			
			//get meta data
			Map<String, Long> countersPerKeyMeta = this.getCountersLocalCopy().get(keyStr);
			if(countersPerKeyMeta == null){
				//not exists, please create one
				countersPerKeyMeta = new Object2ObjectOpenHashMap<String, Long>(VascoServiceAgentFactory.SMALL_MAP_INITIAL_SIZE);
				this.countersLocalCopy.put(keyStr, countersPerKeyMeta);
			}
			
			Iterator itCountersPerKey = coutersPerKeyByRequest.entrySet().iterator();
			while(itCountersPerKey.hasNext()){
				Entry<String, Long> countersPerKeyEntry = (Entry<String, Long>) itCountersPerKey.next();
				String opName = countersPerKeyEntry.getKey();
				long counterValueByRequest = countersPerKeyEntry.getValue().longValue();
				
				//get meta data
				//get meta data
				Long counterMeta = countersPerKeyMeta.get(opName);
				if(counterMeta == null){
					counterMeta = new Long(0L);
					countersPerKeyMeta.put(opName, counterMeta);
				}
				
				if(counterMeta.longValue() >= counterValueByRequest){
					itCountersPerKey.remove();
				}
			}
			
			if(coutersPerKeyByRequest.isEmpty()){
				itKeyCounters.remove();
			}
		}
		if(keyCounters.isEmpty()){
			return true;
		}
		return false;
	}

	@Override
	public void waitForBeExcuted(ProxyTxnId txnId, LockRequest lcR) {
		LockReply lcReply = this.getPermission(txnId, lcR);
		synchronized(this.countersLocalCopy){
			while(!this.isCounterMatching(lcReply.getKeyCounterMap())){
				try {
					this.countersLocalCopy.wait(VascoServiceAgentFactory.RESPONSE_WAITING_TIME_IN_MILL_SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Map<String, Map<String, Long>> getCountersLocalCopy() {
		return countersLocalCopy;
	}

	public void setCountersLocalCopy(
			Map<String, Map<String, Long>> countersLocalCopy) {
		this.countersLocalCopy = countersLocalCopy;
	}

}
