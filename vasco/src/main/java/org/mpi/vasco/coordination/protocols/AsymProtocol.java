package org.mpi.vasco.coordination.protocols;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.mpi.vasco.coordination.MessageHandlerClientSide;
import org.mpi.vasco.coordination.protocols.messages.LockReqMessage;
import org.mpi.vasco.coordination.protocols.util.LockReply;
import org.mpi.vasco.coordination.protocols.util.LockRequest;
import org.mpi.vasco.txstore.util.ProxyTxnId;

public class AsymProtocol extends Protocol{
	
	private ConcurrentHashMap<ProxyTxnId, List<LockReply>> asymReplyMap;
	
	private int NUM_OF_CLIENTS;
	//aggregate all replies from all participants and then return

	public AsymProtocol(MessageHandlerClientSide c) {
		super(c);
		this.setNUM_OF_CLIENTS(c.getMembership().getLockService().getLockClients().length);
		this.asymReplyMap = new ConcurrentHashMap<ProxyTxnId, List<LockReply>>();
	}

	@Override
	public
	LockReply getPermission(ProxyTxnId txnId, LockRequest lcRequest) {
		LockReqMessage msg = new LockReqMessage(txnId,
				client.getMyId(), lcRequest);
		//send the request to all client
		client.sentToAllLockClients(msg);
		
		//wait
		List<LockReply> lcReplyList = new ArrayList<LockReply>();
		this.asymReplyMap.put(txnId, lcReplyList);
		synchronized(lcReplyList){
			while(lcReplyList.size() != this.getNUM_OF_CLIENTS()){
				try {
					lcReplyList.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		LockReply lcReply = lcReplyList.remove(0);

		//Here
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

}
