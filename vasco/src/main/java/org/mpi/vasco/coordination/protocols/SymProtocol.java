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

import bftsmart.tom.ServiceProxy;

/**
 * The Class SymProtocol.
 */
public class SymProtocol implements Protocol{
	
	private ConcurrentHashMap<ProxyTxnId, SymMetaData> symMetaDataMap;
	
	private int clientId;
	
	//locally maintain a copy of data on the centralized server
	// first level key : tablename + primary key
	// second level key : opName
	// second level value : counter for <first_level_key, second_level_key>
    protected Map<String, Map<String, Long>> countersLocalCopy;
    
	/** The proxy for communicating between server and client for sym protocol */
	protected ServiceProxy[] proxies;
	
	public static int NUM_OF_BFTSMART_PROXY = 20;
	
	public ServiceProxy getProxy(int thrdId){
		return this.proxies[thrdId % NUM_OF_BFTSMART_PROXY];
	}
	
	/**
	 * Instantiates a new sym protocol.
	 *
	 * @param xmlFile the xml file
	 * @param clientId the client id
	 */
	public SymProtocol(int cId) {
		this.proxies = new ServiceProxy[NUM_OF_BFTSMART_PROXY];
		for(int i = 0 ; i < NUM_OF_BFTSMART_PROXY; i++){
			this.proxies[i] = new ServiceProxy(cId * NUM_OF_BFTSMART_PROXY + i);
		}
		this.clientId = cId;
		this.symMetaDataMap = new ConcurrentHashMap<ProxyTxnId, SymMetaData>(VascoServiceAgentFactory.BIG_MAP_INITIAL_SIZE);
		this.setCountersLocalCopy(new Object2ObjectOpenHashMap<String, Map<String, Long>>(VascoServiceAgentFactory.BIG_MAP_INITIAL_SIZE));
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.Protocol#getPermission(org.mpi.vasco.txstore.util.ProxyTxnId, org.mpi.vasco.coordination.protocols.util.LockRequest)
	 */
	@Override
	public LockReply getPermission(ProxyTxnId txnId, LockRequest lcR) {
		//Debug.println("\t\t------> start getting symprotocol permission");
		LockReqMessage msg = new LockReqMessage(txnId,
				this.clientId, lcR);
		SymMetaData meta = this.addInitialMetaData(txnId, lcR);
		int threadId = (int)Thread.currentThread().getId();
		byte[] repliesInBytes = this.getProxy(threadId).invokeOrdered(msg.getBytes());
		LockReply lcReply = new LockReply(repliesInBytes, 0);
		//Debug.println("\t\t\t received a reply " + lcReply.toString());
		//Debug.println("\t\t<------ end getting symprotocol permission");
		meta.setLockReply(lcReply);
		return lcReply;
	}
	
	public SymMetaData addInitialMetaData(ProxyTxnId txnId, LockRequest lcR){
		//Debug.println("Initially added " + txnId.toString());
		SymMetaData meta = new SymMetaData(lcR, null);
		this.symMetaDataMap.put(txnId, meta);
		return meta;
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
	public void cleanUp(ProxyTxnId txnId, Set<String> keys, String opName) {
		//Debug.println("\t\t-----> start cleanning up [sym]");
		this.incrementLocalCounterByOne(keys, opName);
		if(this.symMetaDataMap.contains(txnId)){
			this.symMetaDataMap.remove(txnId);
		}
		//Debug.println("\t\t<----- end cleanning up [sym]");
		
		/*
        Debug.println("\t ----> printOut Local Counters");
        Debug.println(this.countersToString());
        Debug.println("\t<---- printOut Local Counters");
        */
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
		//Debug.println("\t\t----> start waiting for being executed [sym]");
		LockReply lcReply = this.symMetaDataMap.get(txnId).getLockReply();
		int timesForDebug = 1;
		synchronized(this.countersLocalCopy){
			while(!this.isCounterMatching(lcReply.getKeyCounterMap())){
				try {
					this.countersLocalCopy.wait(VascoServiceAgentFactory.RESPONSE_WAITING_TIME_IN_MILL_SECONDS);
					String missingCounters = convertCountersToString(lcReply.getKeyCounterMap());
					System.out.println("SymWait " + txnId.toString() + " times " + (timesForDebug++));
					System.out.println("missing counters ---------------->");
					System.out.println(missingCounters);
					System.out.println("<------------------ missing counters");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		//Debug.println("\t\t<---- end waiting for being executed [sym]");
	}

	public Map<String, Map<String, Long>> getCountersLocalCopy() {
		return countersLocalCopy;
	}

	public void setCountersLocalCopy(
			Map<String, Map<String, Long>> countersLocalCopy) {
		this.countersLocalCopy = countersLocalCopy;
	}

	@Override
	public void cleanUpLocal(ProxyTxnId txnId, Set<String> keys, String opName) {
		throw new RuntimeException("should not call this cleanUpLocal in the sym protocol class");
	}
	
	private String countersToString() {
        StringBuilder sb=new StringBuilder();
        for(Map.Entry<String, Map<String, Long>> fstLEntry: this.countersLocalCopy.entrySet()) {
        	sb.append("key: " + fstLEntry.getKey() + "\n");
        	sb.append("{");
        	for(Map.Entry<String, Long> sndLEntry : fstLEntry.getValue().entrySet()){
        		sb.append("op: " + sndLEntry.getKey() + ",");
        		sb.append(" value: ");
        		sb.append(sndLEntry.getValue().longValue());
        	}
        	sb.append("}\n");
        }
        return sb.toString();
    }
	
	public static String convertCountersToString(Map<String, Map<String, Long>> keyCounters) {
        StringBuilder sb=new StringBuilder();
        if(keyCounters != null && !keyCounters.isEmpty()){
	        for(Map.Entry<String, Map<String, Long>> fstLEntry: keyCounters.entrySet()) {
	        	sb.append("key: " + fstLEntry.getKey() + "\n");
	        	sb.append("{");
	        	for(Map.Entry<String, Long> sndLEntry : fstLEntry.getValue().entrySet()){
	        		sb.append("op: " + sndLEntry.getKey() + ",");
	        		sb.append(" value: ");
	        		sb.append(sndLEntry.getValue().longValue());
	        	}
	        	sb.append("}\n");
	        }
        }
        return sb.toString();
    }

	@Override
	public void addLockReply(ProxyTxnId txnId, LockReply lcReply) {
		throw new RuntimeException("Should not call addLockReply");
		
	}

}
