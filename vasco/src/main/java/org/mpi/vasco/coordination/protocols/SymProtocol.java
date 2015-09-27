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
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;

import java.util.HashMap;
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
import org.mpi.vasco.coordination.protocols.util.asym.AsymCounter;
import org.mpi.vasco.coordination.protocols.util.asym.AsymNonBarrierCounter;
import org.mpi.vasco.txstore.util.ProxyTxnId;

/**
 * The Class SymProtocol.
 */
public class SymProtocol extends Protocol{
	
	private ConcurrentHashMap<ProxyTxnId, SymMetaData> symMetaDataMap;
	
	//locally maintain a copy of data on the centralized server
	// keys: table name, second level key: keyname, values: counter set, third level key: conflict name, values: counter value
    protected Map<String, Map<String, Map<String, Long>>> countersLocalCopy=new HashMap<>();
	
	/**
	 * Instantiates a new sym protocol.
	 *
	 * @param xmlFile the xml file
	 * @param clientId the client id
	 */
	public SymProtocol(MessageHandlerClientSide c) {
		super(c);
		this.symMetaDataMap = new ConcurrentHashMap<ProxyTxnId, SymMetaData>();
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.Protocol#getPermission(org.mpi.vasco.txstore.util.ProxyTxnId, org.mpi.vasco.coordination.protocols.util.LockRequest)
	 */
	@Override
	public LockReply getPermission(ProxyTxnId txnId, LockRequest lcR) {
		LockReqMessage msg = new LockReqMessage(txnId,
				client.getMyId(), lcR);
		
		client.sendToLockServer(msg);
		
		SymMetaData meta = this.addInitialMetaData(txnId, lcR);
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
	
	private void incrementLocalCounterByOne(Map<String, Set<String>> tableKeyMap,
			String operationName){
		synchronized(this.countersLocalCopy){
			for(String tableName : tableKeyMap.keySet()){
				//fetch metadata from the main counter map
				Map<String, Map<String, Long>> secondLevelMap = this.countersLocalCopy.get(tableName);
				if(secondLevelMap == null){
					secondLevelMap = new Object2ObjectOpenHashMap<String, Map<String, Long>>();
					this.countersLocalCopy.put(tableName, secondLevelMap);
				}
				
				Set<String> keySet = tableKeyMap.get(tableName);
				
				for(String keyName : keySet){
					//get key counter set
					Map<String, Long> counterSetForKey = secondLevelMap.get(keyName);
					if(counterSetForKey == null){
						counterSetForKey = new Object2ObjectOpenHashMap<String, Long>();
						secondLevelMap.put(keyName, counterSetForKey);
					}
					
					Long counterValue = counterSetForKey.get(operationName);
					if(counterValue == null){
						counterValue = new Long(0L);
					}else{
						counterValue = new Long(1L);
					}
					counterSetForKey.put(operationName, counterValue);
				}
			}
		}
	}

	@Override
	public void cleanUp(ProxyTxnId txnId) {
		//update the local counter, how to change the remote?
		LockRequest lcR = this.symMetaDataMap.get(txnId).getLockRequest();
		this.incrementLocalCounterByOne(lcR.getKeyList(), lcR.getOpName());
	}
	
	private boolean isCounterMatching(Map<String, Map<String, Map<String, Long>>> keyCounters){
		Iterator itSecondLevel = keyCounters.entrySet().iterator();
		while(itSecondLevel.hasNext()){
			Entry<String, Map<String, Map<String, Long>>> entrySecondLevel = (Entry<String, Map<String, Map<String, Long>>>) itSecondLevel.next();
			String tableName = entrySecondLevel.getKey();
			
			//fetch metadata from the main counter map
			Map<String, Map<String, Long>> secondLevelMap = this.countersLocalCopy.get(tableName);
			if(secondLevelMap == null){
				secondLevelMap = new Object2ObjectOpenHashMap<String, Map<String, Long>>();
				this.countersLocalCopy.put(tableName, secondLevelMap);
			}
			
			Iterator itThirdLevel = entrySecondLevel.getValue().entrySet().iterator();
			while(itThirdLevel.hasNext()){
				Entry<String, Map<String, Long>> entryThirdLevel = (Entry<String, Map<String, Long>>) itThirdLevel.next();
				String keyName = entryThirdLevel.getKey();
				
				//get key counter set
				Map<String, Long> counterSetForKey = secondLevelMap.get(keyName);
				if(counterSetForKey == null){
					counterSetForKey = new Object2ObjectOpenHashMap<String, Long>();
					secondLevelMap.put(keyName, counterSetForKey);
				}
				
				Iterator itFourthLevel = entryThirdLevel.getValue().entrySet().iterator();
				while(itFourthLevel.hasNext()){
					Entry<String, Long> entryFourthLevel = (Entry<String, Long>) itFourthLevel.next();
					String opName = entryFourthLevel.getKey();
					
					Long counterValue = counterSetForKey.get(opName);
					if(counterValue == null){
						counterValue = new Long(0L);
						counterSetForKey.put(opName, counterValue);
					}
					
					if(counterValue.longValue() >= entryFourthLevel.getValue().longValue()){
						itFourthLevel.remove();
					}
				}
				if(entryThirdLevel.getValue().isEmpty()){
					itThirdLevel.remove();
				}
			}
			
			if(entrySecondLevel.getValue().isEmpty()){
				itSecondLevel.remove();
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

}
