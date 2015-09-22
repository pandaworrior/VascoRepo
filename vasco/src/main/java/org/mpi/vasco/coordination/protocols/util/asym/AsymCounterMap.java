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
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.FastEntrySet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class AsymCounterMap.
 */
public class AsymCounterMap {
	
	/** The counter map. */
	Map<String, Map<String, Set<AsymCounter>>> counterMap;
	
	/** The first level map capacity. */
	private static int FIRST_LEVEL_MAP_CAPACITY = 12;
	
	/** The second level map capacity. */
	private static int SECOND_LEVEL_MAP_CAPACITY = 1000;
	
	/**
	 * Instantiates a new asym counter map.
	 */
	public AsymCounterMap(){
		this.counterMap = new Object2ObjectOpenHashMap<String, Map<String, Set<AsymCounter>>>(FIRST_LEVEL_MAP_CAPACITY);
	}
	
	/**
	 * Initiate second level map.
	 *
	 * @param tableName the table name
	 * @return the map
	 */
	private Map<String, Set<AsymCounter>> initiateSecondLevelMap(String tableName){
		Map<String, Set<AsymCounter>> keyValueMap = new Object2ObjectOpenHashMap<String, Set<AsymCounter>>(SECOND_LEVEL_MAP_CAPACITY);
		this.counterMap.put(tableName, keyValueMap);
		return keyValueMap;
	}

	/**
	 * Gets the counter map.
	 *
	 * @return the counter map
	 */
	public Map<String, Map<String, Set<AsymCounter>>> getCounterMap() {
		return counterMap;
	}

	/**
	 * Sets the counter map.
	 *
	 * @param counterMap the counter map
	 */
	public void setCounterMap(Map<String, Map<String, Set<AsymCounter>>> counterMap) {
		this.counterMap = counterMap;
	}
	
	/**
	 * Gets the list of barrier instances and update local counter.
	 *
	 * @param tableKeyMap the table key map
	 * @param conflictBarrierOpNames the conflict barrier op names
	 * @param nonBarrierOpName the non barrier op name
	 * @return the list of barrier instances and update local counter
	 */
	public Set<ProxyTxnId> getListOfBarrierInstancesAndUpdateLocalCounter(Map<String, Set<String>> tableKeyMap, 
			Set<String> conflictBarrierOpNames,
			String nonBarrierOpName){
		
		//initiate the return value
		Set<ProxyTxnId> barrierInstances = new ObjectOpenHashSet<ProxyTxnId>();
		
		for(String tableName : tableKeyMap.keySet()){
			
			// get the meta data map
			Map<String, Set<AsymCounter>> secondLevelMap = this.getCounterMap()
					.get(tableName);
			if(secondLevelMap == null){
				//if the meta data map is empty then please populate it first
				secondLevelMap = this.initiateSecondLevelMap(tableName);
				this.getCounterMap().put(tableName, secondLevelMap);
			}
			
			//iterate all keys that are interested
			Set<String> keyList = tableKeyMap.get(tableName);
			for(String keyName : keyList){
				//read the meta data for that key
				ObjectOpenHashSet<AsymCounter> counterSetForKey = (ObjectOpenHashSet<AsymCounter>) secondLevelMap.get(keyName);
				if(counterSetForKey == null){
					counterSetForKey = new ObjectOpenHashSet<AsymCounter>();
					secondLevelMap.put(keyName, counterSetForKey);
				}
				
				//iterate the conflicting barrier operation you are interested
				for(String operationName : conflictBarrierOpNames){
					AsymCounter bCounter = counterSetForKey.get(operationName);
					if(bCounter == null){
						bCounter = new AsymBarrierCounter(operationName);
						counterSetForKey.add(bCounter);
					}else{
						if(!(bCounter instanceof AsymBarrierCounter)){
							throw new RuntimeException("The barrier is not AsymBarrierCounter " + bCounter.toString());
						}
					}
					
					//add all active barrier instances to the return set
					barrierInstances.addAll(((AsymBarrierCounter)bCounter).getActiveBarrierTxnIdSet());
				}
				
				//add local instance of non-barrier op
				AsymCounter nbCounter = counterSetForKey.get(nonBarrierOpName);
				if(nbCounter == null){
					//add local counter there if no exists
					nbCounter = new AsymNonBarrierCounter(tableName);
					counterSetForKey.add(nbCounter);
				}
				((AsymNonBarrierCounter) nbCounter).addLocalInstance();
			}
		}
		
		return barrierInstances;
	}
	
	/**
	 * Update global count for non barrier op by one.
	 *
	 * @param tableKeyMap the table key map
	 * @param nonBarrierOpName the non barrier op name
	 */
	private void updateGlobalCountForNonBarrierOpByOne(Map<String, Set<String>> tableKeyMap, 
			String nonBarrierOpName){
		for(String tableName : tableKeyMap.keySet()){
			Set<String> keyList = tableKeyMap.get(tableName);//read the key list touched by the operation
			Map<String, Set<AsymCounter>> secondLevelMap = this.getCounterMap().get(tableName);//get the meta data map
				
			if(secondLevelMap != null){
				for(String keyName : keyList){//iterate the keys you target
					ObjectOpenHashSet<AsymCounter> counterSetForKey = (ObjectOpenHashSet<AsymCounter>) secondLevelMap.get(keyName);
					if(counterSetForKey != null){
						AsymCounter nbCounter = counterSetForKey.get(nonBarrierOpName);
						if(nbCounter != null){
							((AsymNonBarrierCounter) nbCounter).completeLocalInstance();
						}else{
							throw new RuntimeException(nonBarrierOpName + " counter not exists!");
						}
					}else{
						throw new RuntimeException(keyName + " not exists!");
					}
				}
			}else{
				throw new RuntimeException(tableName + " not exists!");
			}
		}
	}
	
	/**
	 * Complete local non barrier op clean up.
	 *
	 * @param tableKeyMap the table key map
	 * @param nonBarrierOpName the non barrier op name
	 */
	public void completeLocalNonBarrierOpCleanUp(Map<String, Set<String>> tableKeyMap, String nonBarrierOpName){
		this.updateGlobalCountForNonBarrierOpByOne(tableKeyMap, nonBarrierOpName);
	}
	
	/**
	 * Complete remote non barrier op clean up.
	 *
	 * @param tableKeyMap the table key map
	 * @param nonBarrierOpName the non barrier op name
	 */
	public void completeRemoteNonBarrierOpCleanUp(Map<String, Set<String>> tableKeyMap, String nonBarrierOpName){
		this.updateGlobalCountForNonBarrierOpByOne(tableKeyMap, nonBarrierOpName);
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
	public Map<String, Map<String, Map<String, Long>>> getMapOfNonBarrierOpCountersAndPlaceBarrier(
			Map<String, Set<String>> tableKeyMap,
			Set<String> conflictNonBarrierOpNames, ProxyTxnId barrierId,
			String barrierOpName) {

		Map<String, Map<String, Map<String, Long>>> counterMap = new Object2ObjectOpenHashMap<String, Map<String, Map<String, Long>>>();

		for (String tableName : tableKeyMap.keySet()) {
			
			Map<String, Set<AsymCounter>> secondLevelMap = this.getCounterMap()
					.get(tableName);// get the meta data map
			
			if(secondLevelMap == null){
				secondLevelMap = this.initiateSecondLevelMap(tableName);
				this.getCounterMap().put(tableName, secondLevelMap);
			}
			
			//iterate all keys that are interested
			Set<String> keyList = tableKeyMap.get(tableName);
	
			Map<String, Map<String, Long>> keyCounterMap = new Object2ObjectOpenHashMap<String, Map<String, Long>>();
			counterMap.put(tableName, keyCounterMap);
			
			// iterate the keys you target
			for (String keyName : keyList) {
				//get meta data for that key
				ObjectOpenHashSet<AsymCounter> counterSetForKey = (ObjectOpenHashSet<AsymCounter>) secondLevelMap
								.get(keyName);
				
				if(counterSetForKey == null){
					counterSetForKey = new ObjectOpenHashSet<AsymCounter>();
					secondLevelMap.put(keyName, counterSetForKey);
				}
				
				Map<String, Long> opCounterMap = new Object2ObjectOpenHashMap<String, Long>();
				keyCounterMap.put(keyName, opCounterMap);
	
				// iterate the conflicting non-barrier operation you are interested
				for (String operationName : conflictNonBarrierOpNames) {
								
					AsymCounter nbCounter = counterSetForKey.get(operationName);
					if(nbCounter == null){
						nbCounter = new AsymNonBarrierCounter(operationName);
						counterSetForKey.add(nbCounter);
					}else{
						if (!(nbCounter instanceof AsymNonBarrierCounter)) {
							throw new RuntimeException("The barrier is not AsymNonBarrierCounter "+ nbCounter.toString());
						}
					}
					opCounterMap.put(nbCounter.getCounterName(), ((AsymNonBarrierCounter) nbCounter).getLocalCount());
				}
				
				//add barrier op for that key and that table
				AsymCounter bCounter = counterSetForKey.get(barrierOpName);
				if (bCounter == null) {
					bCounter = new AsymBarrierCounter(barrierOpName);
					counterSetForKey.add(bCounter);
				}
				((AsymBarrierCounter) bCounter).addBarrierInstance(barrierId);
			}
		}

		return counterMap;
	}
	
	
	/**
	 * Complete barrier op clean up.
	 *
	 * @param tableKeyMap the table key map
	 * @param operationName the operation name
	 * @param txnId the txn id
	 */
	private void completeBarrierOpCleanUp(Map<String, Set<String>> tableKeyMap,
			String operationName, ProxyTxnId txnId){
		
		for(String tableName : tableKeyMap.keySet()){
			Set<String> keyList = tableKeyMap.get(tableName);//read the key list touched by the operation
			Map<String, Set<AsymCounter>> secondLevelMap = this.getCounterMap().get(tableName);//get the meta data map
				
			if(secondLevelMap != null){
				for(String keyName : keyList){//iterate the keys you target
					ObjectOpenHashSet<AsymCounter> counterSetForKey = (ObjectOpenHashSet<AsymCounter>) secondLevelMap.get(keyName);
					if(counterSetForKey != null){
						AsymCounter bCounter = counterSetForKey.get(operationName);
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
						throw new RuntimeException(keyName + " not exists!");
					}
				}
			}else{
				throw new RuntimeException(tableName + " not exists!");
			}
		}
	}
	
	/**
	 * Complete local barrier op clean up.
	 *
	 * @param tableKeyMap the table key map
	 * @param operationName the operation name
	 * @param txnId the txn id
	 */
	public void completeLocalBarrierOpCleanUp(Map<String, Set<String>> tableKeyMap,
			String operationName, ProxyTxnId txnId){
		this.completeBarrierOpCleanUp(tableKeyMap, operationName, txnId);
	}

	/**
	 * Complete remote barrier op clean up.
	 *
	 * @param tableKeyMap the table key map
	 * @param operationName the operation name
	 * @param txnId the txn id
	 */
	public void completeRemoteBarrierOpCleanUp(Map<String, Set<String>> tableKeyMap,
			String operationName, ProxyTxnId txnId){
		this.completeBarrierOpCleanUp(tableKeyMap, operationName, txnId);
	}
}
