package org.mpi.vasco.coordination.protocols.centr;

import org.mpi.vasco.coordination.BaseNode;
import org.mpi.vasco.coordination.membership.Role;
import org.mpi.vasco.coordination.protocols.messages.MessageFactory;

public class MessageHandlerClientSide extends BaseNode{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4040510651915229397L;
	
	private MessageFactory mf;

	public MessageHandlerClientSide(String membershipFile, Role myRole, int myId) {
		super(membershipFile, myRole, myId);
		mf = new MessageFactory();
	}

	@Override
	public void handle(byte[] bytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		
	}

}
