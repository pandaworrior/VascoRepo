package org.mpi.vasco.coordination.protocols;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mpi.vasco.coordination.MessageHandlerClientSide;
import org.mpi.vasco.coordination.VascoServiceAgentFactory;
import org.mpi.vasco.coordination.protocols.messages.CleanUpBarrierMessage;
import org.mpi.vasco.coordination.protocols.messages.LockReqMessage;
import org.mpi.vasco.coordination.protocols.util.Conflict;
import org.mpi.vasco.coordination.protocols.util.LockReply;
import org.mpi.vasco.coordination.protocols.util.LockRequest;
import org.mpi.vasco.coordination.protocols.util.Protocol;
import org.mpi.vasco.coordination.protocols.util.asym.AsymCounterMap;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.util.debug.Debug;

public class AsymProtocol extends Protocol{
	
	//Store all replies, and trigger an event if all expected replies arrive
	private ConcurrentHashMap<ProxyTxnId, List<LockReply>> asymReplyMap;
	
	private Map<ProxyTxnId, LockRequest> asymRequestMap;
	
	//the number of clients will join the barriers
	private int NUM_OF_CLIENTS;
	
	//barrier map and counters
    // keys: table name, second level key: keyname, values: counter set, third level key: conflict name, values: counter value
    private AsymCounterMap counterMap;
    
    
    private Set<ProxyTxnId> activeBarriers;
    
    private static int BARRIER_SET_CAPACITY = 1000;

	public AsymProtocol(MessageHandlerClientSide c) {
		super(c);
		this.setNUM_OF_CLIENTS(c.getMembership().getLockService().getLockClients().length);
		this.asymReplyMap = new ConcurrentHashMap<ProxyTxnId, List<LockReply>>();
		this.setCounterMap(new AsymCounterMap());
		this.setActiveBarriers(new ObjectOpenHashSet<ProxyTxnId>(BARRIER_SET_CAPACITY));
		this.asymRequestMap = new ConcurrentHashMap<ProxyTxnId, LockRequest>();
	}

	@Override
	public
	LockReply getPermission(ProxyTxnId txnId, LockRequest lcRequest) {
		
		if(lcRequest == null){
			throw new RuntimeException("no lc request for " + txnId.toString());
		}
		
		//put lcRequest in the record list
		this.asymRequestMap.put(txnId, lcRequest);
		Debug.println("add lc request data for " + txnId.toString());
		LockReply lcReply = null;
		
		String opName = lcRequest.getOpName();
		Conflict c = this.getMessageClient().getAgent().getConfTable().getConflictByOpName(opName, 
				Protocol.PROTOCOL_ASYM);
		
		//check whether if the operation name is the barrier
		//if the operation name is the barrier, it needs to send the request to all
		if(c.isBarrier()){
			
			//put the barrier id in the active barrier list
			synchronized(this.activeBarriers){
				this.activeBarriers.add(txnId);
			}
			
			LockReqMessage msg = new LockReqMessage(txnId,
					client.getMyId(), lcRequest);
			
			//waitlcR
			List<LockReply> lcReplyList = new ArrayList<LockReply>();
			this.asymReplyMap.put(txnId, lcReplyList);
			//send the request to all client
			client.sentToAllLockClients(msg);
			synchronized(lcReplyList){
				while(lcReplyList.size() != this.getNUM_OF_CLIENTS()){
					try {
						lcReplyList.wait(VascoServiceAgentFactory.RESPONSE_WAITING_TIME_IN_MILL_SECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			lcReply = lcReplyList.get(0);
			lcReply.aggreLockReplies(lcReplyList);
		}else{
			//if not, then get a list of barriers it should wait
			//increment the corresponding local counters
			//TODO: check if getConfList contains some other non barrier operations
			Set<ProxyTxnId> barrierInstances = null;
			
			synchronized(this.counterMap){
				barrierInstances = this.counterMap.getListOfBarrierInstancesAndUpdateLocalCounter(
					lcRequest.getKeyList(), c.getConfList(), opName);
			}
			
			lcReply = new LockReply(opName, Protocol.PROTOCOL_ASYM, barrierInstances);
			
			//no barriers to be waiting, then return null
		}
		
		Debug.println("Received all replies \n");
		return lcReply;
	}

	@Override
	public void addLockReply(ProxyTxnId txnId, LockReply lcReply) {
		List<LockReply> lcReplyList = this.asymReplyMap.get(txnId);
		if(lcReplyList == null){
			throw new RuntimeException("Meta is not initialized " + txnId.toString());
		}
		synchronized(lcReplyList){
			lcReplyList.add(lcReply);
			if(lcReplyList.size() == this.getNUM_OF_CLIENTS()){
				lcReplyList.notify();
			}
		}
		
	}

	public int getNUM_OF_CLIENTS() {
		return NUM_OF_CLIENTS;
	}

	public void setNUM_OF_CLIENTS(int nUM_OF_CLIENTS) {
		NUM_OF_CLIENTS = nUM_OF_CLIENTS;
	}

	//when receive a request from a peer to join a barrier, fetch
	// the counter of the counter parts and send it back
	@Override
	public LockReply getLocalPermission(ProxyTxnId txnId, LockRequest lcR) {
		//if the operation is not barrier, then it will check whether barrier exist or not
		//otherwise, it will first set barrier and then start to send message out
		String opName = lcR.getOpName();
		Conflict c = this.getMessageClient().getAgent().getConfTable().getConflictByOpName(opName, 
				Protocol.PROTOCOL_ASYM);
		
		if(!this.asymRequestMap.containsKey(txnId)){
			this.asymRequestMap.put(txnId, lcR);
		}
		
		LockReply lcReply = null;
		synchronized(this.counterMap){
			
			Map<String, Map<String, Long>> nonBarrierOpCounterMap = 
					this.counterMap.getMapOfNonBarrierOpCountersAndPlaceBarrier(
					lcR.getKeyList(),
					c.getConfList(), txnId,
					opName);
			//barrierInstances = this.counterMap.getListOfBarrierInstancesAndUpdateLocalCounter(
				//lcRequest.getKeyList(), c.getConfList(), opName);
			lcReply = new LockReply(opName, Protocol.PROTOCOL_ASYM, nonBarrierOpCounterMap);
		}
		return lcReply;
	}
	
	/**
	 * Once
	 */
	public void cleanUpBarrier(ProxyTxnId txnId, LockRequest lcRequest){
		Debug.println("cleanUpBarrier\n");
		synchronized(this.counterMap){
			this.counterMap.completeLocalBarrierOpCleanUp(lcRequest.getKeyList(), 
				lcRequest.getOpName(), txnId);
		}
		synchronized(this.activeBarriers){
			this.activeBarriers.remove(txnId);
			this.activeBarriers.notifyAll();
		}
	}
	
	public void cleanUpNonBarrier(LockRequest lcRequest){
		synchronized(this.counterMap){
			this.counterMap.completeLocalNonBarrierOpCleanUp(lcRequest.getKeyList(), 
				lcRequest.getOpName());
		}
	}
	
	public void cleanUpBarrierGlobal(ProxyTxnId txnId){
		//need to send a message to all clients
		CleanUpBarrierMessage msg = new CleanUpBarrierMessage(txnId);
		client.sentToAllLockClients(msg);
	}
	
	/*
	 * actively cleanUp(non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.util.Protocol#cleanUp(org.mpi.vasco.txstore.util.ProxyTxnId)
	 */
	@Override
	public void cleanUp(ProxyTxnId txnId) {
		Debug.println("cleanUp \n");
		LockRequest lcRequest = this.asymRequestMap.get(txnId);
		if(lcRequest == null){
			throw new RuntimeException("no lc request for " + txnId.toString());
		}
		Conflict c = this.getMessageClient().getAgent().getConfTable().getConflictByOpName(
				lcRequest.getOpName(), 
				Protocol.PROTOCOL_ASYM);
		if(!c.isBarrier()){
			//no barrier
			this.cleanUpNonBarrier(lcRequest);
		}else{
			this.cleanUpBarrierGlobal(txnId);
		}
	}
	

	@Override
	public void cleanUpLocal(ProxyTxnId txnId) {
		// TODO Auto-generated method stub
		Debug.println("cleanUpLocal \n");
		LockRequest lcRequest = this.asymRequestMap.get(txnId);
		if(lcRequest == null){
			throw new RuntimeException("no lc request for " + txnId.toString());
		}
		Conflict c = this.getMessageClient().getAgent().getConfTable().getConflictByOpName(
				lcRequest.getOpName(), 
				Protocol.PROTOCOL_ASYM);
		if(!c.isBarrier()){
			//no barrier
			this.cleanUpNonBarrier(lcRequest);
		}else{
			this.cleanUpBarrier(txnId, lcRequest);
		}
		
		if(this.asymReplyMap.containsKey(txnId)){
			this.asymReplyMap.remove(txnId);
		}
		this.asymRequestMap.remove(txnId);
	}

	public AsymCounterMap getCounterMap() {
		return counterMap;
	}

	public void setCounterMap(AsymCounterMap counterMap) {
		this.counterMap = counterMap;
	}

	public Set<ProxyTxnId> getActiveBarriers() {
		return activeBarriers;
	}

	public void setActiveBarriers(Set<ProxyTxnId> activeBarriers) {
		this.activeBarriers = activeBarriers;
	}

	@Override
	public void waitForBeExcuted(ProxyTxnId txnId, LockRequest lcR) {
		LockReply lcReply = this.getPermission(txnId, lcR);
		if(lcReply.getBarrierInstancesForWait() == null){
			//that is a barrier op
			//check whether all non-barrier ops it depends on have been applied
			synchronized(this.counterMap){
				while(!this.counterMap.isNonBarrierCountersMatching(lcReply.getKeyCounterMap())){
					try {
						this.counterMap.wait(VascoServiceAgentFactory.RESPONSE_WAITING_TIME_IN_MILL_SECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			Set<ProxyTxnId> barriers = lcReply.getBarrierInstancesForWait();
			if(!barriers.isEmpty()){
				synchronized(this.activeBarriers){
					Iterator<ProxyTxnId> barrierIds = barriers.iterator();
					while(barrierIds.hasNext()){
						ProxyTxnId bId = barrierIds.next();
						if(!this.activeBarriers.contains(bId)){
							barrierIds.remove();
						}
					}
					while(!barriers.isEmpty()){
						try {
							this.activeBarriers.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			//if no barriers to wait, then please immediately execute
		}
	}

}
