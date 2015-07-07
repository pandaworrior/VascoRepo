package org.mpi.vasco.coordination.protocols.centr;

import org.mpi.vasco.coordination.BaseNode;
import org.mpi.vasco.coordination.membership.Role;
import org.mpi.vasco.coordination.protocols.messages.LockRepMessage;
import org.mpi.vasco.coordination.protocols.messages.LockReqMessage;
import org.mpi.vasco.coordination.protocols.messages.MessageFactory;
import org.mpi.vasco.coordination.protocols.messages.MessageTags;
import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.util.debug.Debug;

public class MessageHandlerServerSide extends BaseNode{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7157915157725026251L;
	
	private static MessageFactory mf = new MessageFactory();
	
	private ReplicatedLockService rsmLockService;

	public MessageHandlerServerSide(String membershipFile, 
			Role myRole, int myId) {
		super(membershipFile, myRole, myId);
		this.setRsmLockService(null);
		Debug.printf("Set up the server %d for lock client", myId);
	}

	@Override
	public void handle(byte[] bytes) {
		// TODO Auto-generated method stub
		MessageBase msg = mf.fromBytes(bytes);
		if (msg == null) {
			throw new RuntimeException("Should never receive a null message");
		}
		
		switch (msg.getTag()) {
		case MessageTags.LOCKREQ:
			process((LockReqMessage) msg);
			break;
		default:
			throw new RuntimeException("invalid message tag: " + msg.getTag());
		}
	}
	
	private void process(LockReqMessage msg){
		//call
		if(this.getRsmLockService() == null){
			throw new RuntimeException("RSM is not set");
		}else{
			LockRepMessage repMsg = this.getRsmLockService().put(msg.getProxyTxnId().toString(), msg.getLockReq());
			int clientId = msg.getGlobalProxyId();
			this.sendToLockClient(repMsg, clientId);
			Debug.printf("Send to lock client with id %d lock reply message %s", clientId, repMsg.toString());
			mf.returnLockReqMessage(msg);
		}
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		
	}

	public ReplicatedLockService getRsmLockService() {
		return rsmLockService;
	}

	public void setRsmLockService(ReplicatedLockService rsmLockService) {
		this.rsmLockService = rsmLockService;
	}

}
