package org.mpi.vasco.coordination.protocols;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
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

public class AsymProtocol implements Protocol{
	
	//Store all replies, and trigger an event if all expected replies arrive
	private ConcurrentHashMap<ProxyTxnId, List<LockReply>> asymReplyMap;
	
	private Map<ProxyTxnId, LockRequest> asymRequestMap;
	
	public Map<ProxyTxnId, LockRequest> getAsymRequestMap() {
		return asymRequestMap;
	}

	public void setAsymRequestMap(Map<ProxyTxnId, LockRequest> asymRequestMap) {
		this.asymRequestMap = asymRequestMap;
	}

	//the number of clients will join the barriers
	private int NUM_OF_CLIENTS;
	
	//barrier map and counters
    // keys: table name, second level key: keyname, values: counter set, third level key: conflict name, values: counter value
    private AsymCounterMap counterMap;
    
    
    private Set<ProxyTxnId> activeBarriers;
    
    private static int BARRIER_SET_CAPACITY = 1000;
	
    /** The client for asym protocol. */
	protected MessageHandlerClientSide client;

	public AsymProtocol(MessageHandlerClientSide c) {
		this.setClient(c);
		this.setNUM_OF_CLIENTS(c.getMembership().getLockService().getLockClients().length);
		this.asymReplyMap = new ConcurrentHashMap<ProxyTxnId, List<LockReply>>();
		this.setCounterMap(new AsymCounterMap());
		this.setActiveBarriers(new ObjectOpenHashSet<ProxyTxnId>(BARRIER_SET_CAPACITY));
		this.asymRequestMap = new ConcurrentHashMap<ProxyTxnId, LockRequest>();
	}

	public
	LockReply getPermission(ProxyTxnId txnId, LockRequest lcRequest) {
		
		//Debug.println("\t\t----->start getting asymprotocol permission");
		if(lcRequest == null){
			throw new RuntimeException("no lc request for " + txnId.toString());
		}
		
		//put lcRequest in the record list
		this.asymRequestMap.put(txnId, lcRequest);
		//Debug.println("\t\t\t ---->add lc request data for " + txnId.toString());
		LockReply lcReply = null;
		
		String opName = lcRequest.getOpName();
		Conflict c = this.getMessageClient().getAgent().getConfTable().getConflictByOpName(opName, 
				Protocol.PROTOCOL_ASYM);
		
		//waitlcR
		List<LockReply> lcReplyList = new ArrayList<LockReply>();
		this.asymReplyMap.put(txnId, lcReplyList);
		
		//check whether if the operation name is the barrier
		//if the operation name is the barrier, it needs to send the request to all
		if(c.isBarrier()){
			
			//put the barrier id in the active barrier list
			synchronized(this.activeBarriers){
				this.activeBarriers.add(txnId);
			}
			
			LockReqMessage msg = new LockReqMessage(txnId,
					client.getMyId(), lcRequest);
			
			//send the request to all client
			//Debug.println("\t\t\t----->This is barrier, so send requests to all peers");
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
			lcReplyList.add(lcReply);
			//no barriers to be waiting, then return null
		}
		//Debug.println("\t\t\tThe final lock reply is " + lcReply.toString());
		//Debug.println("\t\t<-----end getting asymprotocol permission");
		return lcReply;
	}

	public void addLockReply(ProxyTxnId txnId, LockReply lcReply) {
		//Debug.println("Received a lock reply " + lcReply.toString());
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
	public LockReply getLocalPermission(ProxyTxnId txnId, LockRequest lcR) {
		//Debug.println("\t\t----->start getting asymprotocol local permission for a request");
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
		
		//Debug.println("\t\t\t generated a lock reply " + lcReply.toString());
		//Debug.println("\t\t<-----end getting asymprotocol local permission for a request");
		return lcReply;
	}
	
	/**
	 * Once
	 */
	public void cleanUpBarrier(ProxyTxnId txnId, Set<String> keys, String opName){
		//Debug.println("\t\t----->clean up barrier operation " + opName);
		synchronized(this.counterMap){
			this.counterMap.completeLocalBarrierOpCleanUp(keys, 
				opName, txnId);
		}
		synchronized(this.activeBarriers){
			this.activeBarriers.remove(txnId);
			this.activeBarriers.notifyAll();
		}
		//this.counterMap.printOutCounterMap();
		//Debug.println("\t\t<-----clean up barrier operation " + opName);
	}
	
	public void cleanUpNonBarrier(Set<String> keys, String opName){
		//Debug.println("\t\t-----> clean up non-barrier operation " + opName);
		synchronized(this.counterMap){
			this.counterMap.completeLocalNonBarrierOpCleanUp(keys, 
				opName);
		}
		//this.counterMap.printOutCounterMap();
		//Debug.println("\t\t<----- clean up non-barrier operation " + opName);
	}
	
	/*
	 * Is not called right now, it is used when the replicated message doesn't carry
	 * the sufficient information for cleaning up barrier and non-barrier
	 */
	public void cleanUpBarrierGlobal(ProxyTxnId txnId){
		//need to send a message to all clients
		CleanUpBarrierMessage msg = new CleanUpBarrierMessage(txnId);
		client.sentToAllLockClients(msg);
	}
	
	/*
	 * actively cleanUp(non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.util.Protocol#cleanUp(org.mpi.vasco.txstore.util.ProxyTxnId)
	 */
	
	
	
	/*
	 * We do this locally since the replicated message can be used to clean up
	 * therefore, no need to send a separated message to clean
	 * @see org.mpi.vasco.coordination.protocols.util.Protocol#cleanUp(org.mpi.vasco.txstore.util.ProxyTxnId)
	 */
	public void cleanUp(ProxyTxnId txnId, Set<String> keys, String opName) {
		//Debug.println("\t\t ----> start cleaning up [asym]");
		this.cleanUpLocal(txnId, keys, opName);
		//Debug.println("\t\t <---- end cleaning up [asym]");
	}
	

	public void cleanUpLocal(ProxyTxnId txnId, Set<String> keys, String opName) {
		//Debug.println("cleanUpLocal \n");
		Conflict c = this.getMessageClient().getAgent().getConfTable().getConflictByOpName(
				opName, 
				Protocol.PROTOCOL_ASYM);
		
		if(!c.isBarrier()){
			//no barrier
			this.cleanUpNonBarrier(keys, opName);
		}else{
			this.cleanUpBarrier(txnId, keys, opName);
		}
		
		if(this.asymReplyMap.containsKey(txnId)){
			this.asymReplyMap.remove(txnId);
		}
		if(this.asymRequestMap.containsKey(txnId)){
			this.asymRequestMap.remove(txnId);
		}		
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

	public void waitForBeExcuted(ProxyTxnId txnId, LockRequest lcR) {
		List<LockReply> lcReplyList = this.asymReplyMap.get(txnId);
		if(lcReplyList == null || lcReplyList.isEmpty()){
			//Debug.println("Lock reply list is null or empty");
			return;
		}
		LockReply lcReply = lcReplyList.get(0);
		if(lcReply.getBarrierInstancesForWait() == null){
			//Debug.println("\t\t\t----> starting waiting for be executed for barrier");
			//that is a barrier op
			//check whether all non-barrier ops it depends on have been applied
			int timesForDebug = 1;
			synchronized(this.counterMap){
				while(!this.counterMap.isNonBarrierCountersMatching(lcReply.getKeyCounterMap())){
					try {
						this.counterMap.wait(VascoServiceAgentFactory.RESPONSE_WAITING_TIME_IN_MILL_SECONDS);
						System.out.println("BarrierWait " + txnId.toString() + " times " + (timesForDebug++));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			//this.counterMap.printOutCounterMap();
			//Debug.println("\t\t\t<---- end waiting for be executed for barrier");
		}else{
			//Debug.println("\t\t\t----> starting waiting for be executed for non-barrier");
			Set<ProxyTxnId> barriers = lcReply.getBarrierInstancesForWait();
			if(!barriers.isEmpty()){
				synchronized(this.activeBarriers){
					/*Iterator<ProxyTxnId> barrierIds = barriers.iterator();
					while(barrierIds.hasNext()){
						ProxyTxnId bId = barrierIds.next();
						if(!this.activeBarriers.contains(bId)){
							barrierIds.remove();
						}
					}
					int timesForDebug = 1;
					while(!barriers.isEmpty()){
						try {
							this.activeBarriers.wait(VascoServiceAgentFactory.RESPONSE_WAITING_TIME_IN_MILL_SECONDS);
							System.out.println("NonBarrierWait " + txnId.toString() + " times " + (timesForDebug++));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}*/
					int timesForDebug = 1;
					while(!this.isAllBarriersCleanedUp(barriers)){
						try {
							this.activeBarriers.wait(VascoServiceAgentFactory.RESPONSE_WAITING_TIME_IN_MILL_SECONDS);
							System.out.println("NonBarrierWait " + txnId.toString() + " times " + (timesForDebug++));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			//Debug.println("\t\t\t<---- end waiting for be executed for non-barrier");
		}
	}
	
	private boolean isAllBarriersCleanedUp(Set<ProxyTxnId> waitingForBarriers){
		boolean result = true;
		if(!waitingForBarriers.isEmpty()){
			Iterator<ProxyTxnId> barrierIds = waitingForBarriers.iterator();
			while(barrierIds.hasNext()){
				ProxyTxnId bId = barrierIds.next();
				if(!this.activeBarriers.contains(bId)){
					barrierIds.remove();
				}
			}
			if(!waitingForBarriers.isEmpty()){
				result = false;
			}
		}
		return result;
	}
	
	/**
	 * Sets the client.
	 *
	 * @param client the new clientlcR
	 */
	public void setClient(MessageHandlerClientSide client){
		this.client = client;
	}
	
	/**
	 * Gets the message client.
	 *
	 * @return the message client
	 */
	public MessageHandlerClientSide getMessageClient(){
		return client;
	}

}
