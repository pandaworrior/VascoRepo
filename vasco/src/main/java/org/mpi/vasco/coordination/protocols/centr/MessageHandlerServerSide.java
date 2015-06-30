package org.mpi.vasco.coordination.protocols.centr;

import org.mpi.vasco.txstore.BaseNode;
import org.mpi.vasco.txstore.membership.Role;

public class MessageHandlerServerSide extends BaseNode{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7157915157725026251L;

	public MessageHandlerServerSide(String membershipFile, int datacenter,
			Role myRole, int myId) {
		super(membershipFile, datacenter, myRole, myId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handle(byte[] bytes) {
		// TODO Auto-generated method stub
		
	}

}
