package org.mpi.vasco.coordination.protocols.centr;

import org.mpi.vasco.coordination.BaseNode;
import org.mpi.vasco.coordination.membership.Role;
import org.mpi.vasco.coordination.protocols.messages.LockReqMessage;
import org.mpi.vasco.coordination.protocols.messages.MessageFactory;
import org.mpi.vasco.coordination.protocols.messages.MessageTags;
import org.mpi.vasco.network.messages.MessageBase;

public class MessageHandlerServerSide extends BaseNode{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7157915157725026251L;
	
	private static MessageFactory mf;

	public MessageHandlerServerSide(String membershipFile, 
			Role myRole, int myId) {
		super(membershipFile, myRole, myId);
		mf = new MessageFactory();
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
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		
	}

}
