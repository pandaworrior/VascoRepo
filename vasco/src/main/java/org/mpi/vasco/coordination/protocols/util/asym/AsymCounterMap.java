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
package org.mpi.vasco.coordination.protocols.util.asym;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.FastEntrySet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mpi.vasco.coordination.VascoServiceAgentFactory;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class AsymCounterMap.
 */
public class AsymCounterMap {
	
	/** The counter map. 
	 * first level key: compoundKey
	 * second level key: opName
	 * */
	Map<String, Map<String, AsymCounter>> counterMap;
	
	/**
	 * Instantiates a new asym counter map.
	 */
	public AsymCounterMap(){
		this.counterMap = new Object2ObjectOpenHashMap<String, Map<String, AsymCounter>>(VascoServiceAgentFactory.BIG_MAP_INITIAL_SIZE);
	}
	
	private Map<String, AsymCounter> initiateCounterSetPerKey(String keyStr){
		Map<String, AsymCounter> counterSetPerKey = new Object2ObjectOpenHashMap<String, AsymCounter>(VascoServiceAgentFactory.SMALL_MAP_INITIAL_SIZE); 
		this.counterMap.put(keyStr, counterSetPerKey);
		return counterSetPerKey;
	}

	/**
	 * Gets the counter map.
	 *
	 * @return the counter map
	 */
	public Map<String, Map<String, AsymCounter>> getCounterMap() {
		return counterMap;
	}

	/**
	 * Sets the counter map.
	 *
	 * @param counterMap the counter map
	 */
	public void setCounterMap(Map<String, Map<String, AsymCounter>> counterMap) {
		this.counterMap = counterMap;
	}
	

	public Set<ProxyTxnId> getListOfBarrierInstancesAndUpdateLocalCounter(Set<String> keys, 
			Set<String> conflictBarrierOpNames,
			String nonBarrierOpName){
		Debug.println("Get a list of barrier for non-barrier op\n");
		//initiate the return value
		Set<ProxyTxnId> barrierInstances = new ObjectOpenHashSet<ProxyTxnId>();
		
		//iterate all keys that are interested
		for(String keyStr : keys){
			// get the meta data map
			Object2ObjectOpenHashMap<String, AsymCounter> counterSetPerKey = (Object2ObjectOpenHashMap<String, AsymCounter>)this.getCounterMap().get(keyStr);
			if(counterSetPerKey == null){
				counterSetPerKey = (Object2ObjectOpenHashMap<String, AsymCounter>) this.initiateCounterSetPerKey(keyStr);
			}
			
			//iterate the conflicting barrier operation you are interested
			for(String operationName : conflictBarrierOpNames){
				AsymCounter bCounter = counterSetPerKey.get(operationName);
				if(bCounter == null){
					bCounter = new AsymBarrierCounter();
					counterSetPerKey.put(operationName, bCounter);
				}else{
					if(!(bCounter instanceof AsymBarrierCounter)){
						throw new RuntimeException("The barrier is not AsymBarrierCounter " + bCounter.toString());
					}
				}
				
				//add all active barrier instances to the return set
				barrierInstances.addAll(((AsymBarrierCounter)bCounter).getActiveBarrierTxnIdSet());
			}
			
			//add local instance of non-barrier op
			AsymCounter nbCounter = counterSetPerKey.get(nonBarrierOpName);
			if(nbCounter == null){
				//add local counter there if no exists
				nbCounter = new AsymNonBarrierCounter();
				counterSetPerKey.put(nonBarrierOpName, nbCounter);
			}
			((AsymNonBarrierCounter) nbCounter).addLocalInstance();
		}
		//Debug.println("getListOfBarrierInstancesAndUpdateLocalCounter\n");
		//this.printOutCounterMap();
		return barrierInstances;
	}
	
	/**
	 * Update global count for non barrier op by one.
	 *
	 * @param tableKeyMap the table key map
	 * @param nonBarrierOpName the non barrier op name
	 */
	private void updateGlobalCountForNonBarrierOpByOne(Set<String> keys, 
			String nonBarrierOpName){
		
		Debug.println("Update the global count for non-barrier op when this op finishes\n");
		//read the key list touched by the operation
		for(String keyStr : keys){
			//get the meta data map
			Object2ObjectOpenHashMap<String, AsymCounter> counterSetPerKey = (Object2ObjectOpenHashMap<String, AsymCounter>)this.getCounterMap().get(keyStr);
			
			if(counterSetPerKey != null){
				AsymCounter nbCounter = counterSetPerKey.get(nonBarrierOpName);
				if(nbCounter != null){
					((AsymNonBarrierCounter) nbCounter).completeLocalInstance();
				}else{
					//this.printOutCounterMap();
					throw new RuntimeException(nonBarrierOpName + " counter for the key " +keyStr+ " not exists!");
				}
			}else{
				throw new RuntimeException("key " + keyStr + " not exists!");
			}
		}
	}
	
	/**
	 * Complete local non barrier op clean up.
	 *
	 * @param tableKeyMap the table key map
	 * @param nonBarrierOpName the non barrier op name
	 */
	public void completeLocalNonBarrierOpCleanUp(Set<String> keys, String nonBarrierOpName){
		this.updateGlobalCountForNonBarrierOpByOne(keys, nonBarrierOpName);
		//Debug.println("completeLocalNonBarrierOpCleanUp\n");
		//this.printOutCounterMap();
	}
	
	/**
	 * Complete remote non barrier op clean up, needed and clean up when the op is propagated.
	 *
	 * @param tableKeyMap the table key map
	 * @param nonBarrierOpName the non barrier op name
	 */
	public void completeRemoteNonBarrierOpCleanUp(Set<String> keys, String nonBarrierOpName){
		this.updateGlobalCountForNonBarrierOpByOne(keys, nonBarrierOpName);
		//Debug.println("completeRemoteNonBarrierOpCleanUp\n");
		//this.printOutCounterMap();
	}

	/**
	 * Gets the map of non barrier op counters and place barrier.
	 * 
	 * @param tableKeyMap
	 *            the table key map
	 * @param conflictBarrierOpNames
	 *            the conflict barrier op names
	 * @param barrierId
	 *            the barrier id
	 * @param barrierOpName
	 *            the barrier op name
	 * @return the map of non barrier op counters and place barrier
	 */
	public Map<String, Map<String, Long>> getMapOfNonBarrierOpCountersAndPlaceBarrier(
			Set<String> keys,
			Set<String> conflictNonBarrierOpNames, ProxyTxnId barrierId,
			String barrierOpName) {

		Debug.println("place barrier and get non-barrier counter \n");
		Map<String, Map<String, Long>> counterAllMap = new Object2ObjectOpenHashMap<String, Map<String, Long>>();
		
		for(String keyStr : keys){
			// get the meta data map
			Object2ObjectOpenHashMap<String, AsymCounter> counterSetPerKey = (Object2ObjectOpenHashMap<String, AsymCounter>)this.getCounterMap().get(keyStr);
			if(counterSetPerKey == null){
				counterSetPerKey = (Object2ObjectOpenHashMap<String, AsymCounter>) this.initiateCounterSetPerKey(keyStr);
			}
			
			Map<String, Long> counterPerKeyMap = new Object2ObjectOpenHashMap<String, Long>();
			counterAllMap.put(keyStr, counterPerKeyMap);
			// iterate the conflicting non-barrier operation you are interested
			for (String operationName : conflictNonBarrierOpNames) {
							
				AsymCounter nbCounter = counterSetPerKey.get(operationName);
				if(nbCounter == null){
					nbCounter = new AsymNonBarrierCounter();
					counterSetPerKey.put(operationName, nbCounter);
				}else{
					if (!(nbCounter instanceof AsymNonBarrierCounter)) {
						throw new RuntimeException("The barrier is not AsymNonBarrierCounter "+ nbCounter.toString());
					}
				}
				counterPerKeyMap.put(operationName, ((AsymNonBarrierCounter) nbCounter).getLocalCount());
			}
			
			//add barrier op for that key and that table
			AsymCounter bCounter = counterSetPerKey.get(barrierOpName);
			if (bCounter == null) {
				bCounter = new AsymBarrierCounter();
				counterSetPerKey.put(barrierOpName, bCounter);
			}
			((AsymBarrierCounter) bCounter).addBarrierInstance(barrierId);
		}
		//Debug.println("getMapOfNonBarrierOpCountersAndPlaceBarrier\n");
		//this.printOutCounterMap();
		return counterAllMap;
	}
	
	
	/**
	 * Complete barrier op clean up.
	 *
	 * @param tableKeyMap the table key map
	 * @param operationName the operation name
	 * @param txnId the txn id
	 */
	private void completeBarrierOpCleanUp(Set<String> keys,
			String operationName, ProxyTxnId txnId){
		
		//read the key list touched by the operation
		for(String keyStr : keys){
			//get the meta data map
			Object2ObjectOpenHashMap<String, AsymCounter> counterSetPerKey = (Object2ObjectOpenHashMap<String, AsymCounter>)this.getCounterMap().get(keyStr);
			if(counterSetPerKey != null){
				AsymCounter bCounter = counterSetPerKey.get(operationName);
				if(bCounter != null){
					if(bCounter instanceof AsymBarrierCounter){
						((AsymBarrierCounter) bCounter).removeBarrierInstance(txnId);
					}else{
						throw new RuntimeException(bCounter.toString() + " is not a barrier counter!");
					}
				}else{
					throw new RuntimeException(operationName + " counter not exists!");
				}
			}else{
				throw new RuntimeException(keyStr + " not exists!");
			}
		}
		//Debug.println("completeBarrierOpCleanUp\n");
		//this.printOutCounterMap();
	}
	
	/**
	 * Complete local barrier op clean up.
	 *
	 * @param tableKeyMap the table key map
	 * @param operationName the operation name
	 * @param txnId the txn id
	 */
	public void completeLocalBarrierOpCleanUp(Set<String> keys,
			String operationName, ProxyTxnId txnId){
		this.completeBarrierOpCleanUp(keys, operationName, txnId);
		//Debug.println("completeLocalBarrierOpCleanUp\n");
		//this.printOutCounterMap();
	}
	
	//check the non barrier operations a barrier operation depends on already applied
	public boolean isNonBarrierCountersMatching(Map<String, Map<String, Long>> nonBarrierKeyCounters){
		//please check whether all non-barrier operations have been applied
		//If so, please remove it from the map, otherwise, keep it
		
		Iterator itSecondLevel = nonBarrierKeyCounters.entrySet().iterator();
		while(itSecondLevel.hasNext()){
			Entry<String, Map<String, Long>> entrySecondLevel = (Entry<String, Map<String, Long>>) itSecondLevel.next();
			String keyStr = entrySecondLevel.getKey();
			
			//get key counter set
			Object2ObjectOpenHashMap<String, AsymCounter> counterSetForKey = (Object2ObjectOpenHashMap<String, AsymCounter>) this.getCounterMap().get(keyStr);
			
			Iterator itThirdLevel = entrySecondLevel.getValue().entrySet().iterator();
			while(itThirdLevel.hasNext()){
				Entry<String, Long> entryThirdLevel = (Entry<String, Long>) itThirdLevel.next();
				String opName = entryThirdLevel.getKey();
				
				AsymNonBarrierCounter nbCounter = (AsymNonBarrierCounter) counterSetForKey.get(opName);
				
				if(nbCounter.getGlobalCount() >= entryThirdLevel.getValue().longValue()){
					itThirdLevel.remove();
				}
			}
			
			if(entrySecondLevel.getValue().isEmpty()){
				itSecondLevel.remove();
			}	
		}
		if(nonBarrierKeyCounters.isEmpty()){
			return true;
		}
		return false;
	}
	
	public String toString(){
		StringBuilder strBuild = new StringBuilder("asym counter map content, {");
		//read the key list touched by the operation
		for(String keyStr : this.getCounterMap().keySet()){
			strBuild.append("( key : ");
			strBuild.append(keyStr);
			strBuild.append(", counters : {");
			//get the meta data map
			Object2ObjectOpenHashMap<String, AsymCounter> counterSetPerKey = (Object2ObjectOpenHashMap<String, AsymCounter>)this.getCounterMap().get(keyStr);
			if(counterSetPerKey != null){
				for(String opName : counterSetPerKey.keySet()){
					AsymCounter c = counterSetPerKey.get(opName);
					strBuild.append(c.toString());
					strBuild.append(',');
				}
				if(strBuild.charAt(strBuild.length() - 1) == ','){
					strBuild.deleteCharAt(strBuild.length() - 1);
				}
				strBuild.append('}');
			}
			strBuild.append(',');
		}
		if(strBuild.charAt(strBuild.length() - 1) == ','){
			strBuild.deleteCharAt(strBuild.length() - 1);
		}
		strBuild.append(")}");
		return strBuild.toString();
	}
	
	public void printOutCounterMap(){
		System.out.println("-----------> Beginning of the asym counter Map----------->");
		System.out.println(this.getCounterMap().toString());
		System.out.println("<----------- End of the asym counter Map<---------------");
	}
}
