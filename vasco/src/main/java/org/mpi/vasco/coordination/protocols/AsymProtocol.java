package org.mpi.vasco.coordination.protocols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mpi.vasco.coordination.MessageHandlerClientSide;
import org.mpi.vasco.coordination.VascoServiceAgentFactory;
import org.mpi.vasco.coordination.protocols.messages.LockReqMessage;
import org.mpi.vasco.coordination.protocols.util.LockReply;
import org.mpi.vasco.coordination.protocols.util.LockRequest;
import org.mpi.vasco.coordination.protocols.util.Protocol;
import org.mpi.vasco.coordination.protocols.util.asym.AsymCounterMap;
import org.mpi.vasco.txstore.util.ProxyTxnId;

public class AsymProtocol extends Protocol{
	
	//Store all replies, and trigger an event if all expected replies arrive
	private ConcurrentHashMap<ProxyTxnId, List<LockReply>> asymReplyMap;
	
	//the number of clients will join the barriers
	private int NUM_OF_CLIENTS;
	
	//barrier map and counters
    // keys: table name, second level key: keyname, values: counter set, third level key: conflict name, values: counter value
    private AsymCounterMap counterMap;

	public AsymProtocol(MessageHandlerClientSide c) {
		super(c);
		this.setNUM_OF_CLIENTS(c.getMembership().getLockService().getLockClients().length);
		this.asymReplyMap = new ConcurrentHashMap<ProxyTxnId, List<LockReply>>();
		this.setCounterMap(new AsymCounterMap());
	}

	@Override
	public
	LockReply getPermission(ProxyTxnId txnId, LockRequest lcRequest) {
		LockReqMessage msg = new LockReqMessage(txnId,
				client.getMyId(), lcRequest);
		
		//check whether if the operation name is the barrier, if not, then check whether barrier exists, 
		//if not exists, then increment its own counter, then return null
		
		//if exists, then need to wait until all barriers' counters become 0
		
		//if the operation name is the barrier, it needs to send the request to all
		
		//send the request to all client
		client.sentToAllLockClients(msg);
		
		//waitlcR
		List<LockReply> lcReplyList = new ArrayList<LockReply>();
		this.asymReplyMap.put(txnId, lcReplyList);
		synchronized(lcReplyList){
			while(lcReplyList.size() != this.getNUM_OF_CLIENTS()){
				try {
					lcReplyList.wait(VascoServiceAgentFactory.RESPONSE_WAITING_TIME_IN_MILL_SECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		LockReply lcReply = lcReplyList.remove(0);
		lcReply.aggreLockReplies(lcReplyList);
		lcReplyList.clear();
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

	//TODO: when receive a request from a peer to join a barrier, fetch
	// the counter of the counter parts and send it back
	@Override
	public LockReply getLocalPermission(ProxyTxnId txnId, LockRequest lcR) {
		// TODO Auto-generated method stub
		//if the operation is not barrier, then it will check whether barrier exist or not
		//otherwise, it will first set barrier and then start to send message out
		return null;
	}
	
	public String getGloballyUniqueBarrierId(){
		return null;
	}
	
	//actively ask peers to join a barrier when receive a barrier operation
	public void enterBarrierActive(){
		
	}
	
	//actively ask peers to leave a barrier when a barrier operation is done
	public void leaveBarrierActive(){
		
	}
	
	//passively join a barrier when a barrier request is received
	public void enterBarrierPassive(){
		
	}
	
	//passively leaves a barrier when a barrier request is one
	public void leaveBarrierPassive(){
		
	}

	public AsymCounterMap getCounterMap() {
		return counterMap;
	}

	public void setCounterMap(AsymCounterMap counterMap) {
		this.counterMap = counterMap;
	}

}
