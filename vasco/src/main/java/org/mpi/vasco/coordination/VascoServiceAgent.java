package org.mpi.vasco.coordination;

import java.util.ArrayList;
import java.util.List;

import org.mpi.vasco.coordination.membership.Role;
import org.mpi.vasco.coordination.protocols.AsymProtocol;
import org.mpi.vasco.coordination.protocols.SymProtocol;
import org.mpi.vasco.coordination.protocols.util.ConflictTable;
import org.mpi.vasco.coordination.protocols.util.LockReply;
import org.mpi.vasco.coordination.protocols.util.LockRequest;
import org.mpi.vasco.coordination.protocols.util.Protocol;
import org.mpi.vasco.txstore.util.ProxyTxnId;

/**
 * This class is used as the entrance of the
 * vasco and it will contain instances for all protocols
 * it is a client not server
 * @author chengli
 *
 */
public class VascoServiceAgent {
	
	List<Protocol> protocols;
	MessageHandlerClientSide client;
	ConflictTable confTable;
	
	//it must contains a conflict table
	//it must return a null if the op is not restricted
	//it must call asym if the op is restricted by a asymm
	//it must call sym if the op is restricted by sym
	
	public VascoServiceAgent(String memFile, int clientId){
		this.setUpClient(memFile, clientId);
		
		confTable = new ConflictTable(memFile);
		
		protocols = new ArrayList<Protocol>();
		SymProtocol symProtocol = new SymProtocol(this.client);
		protocols.add(symProtocol);
		AsymProtocol asymProtocol = new AsymProtocol(this.client);
		protocols.add(asymProtocol);
		
		if(protocols.size() != Protocol.NUM_OF_PROTOCOLS){
			throw new RuntimeException("The numbers of protocols do not match");
		}
		this.client.setAgent(this);
	}
	
	Protocol getProtocol(int protocolType){
		if(protocolType < 0 || protocolType >= Protocol.NUM_OF_PROTOCOLS){
			throw new RuntimeException("No such protocol type " + protocolType);
		}
		return protocols.get(protocolType);
	}
	
	/*int[] getProtocolTypes(LockRequest lcR){
		throw new RuntimeException("Not implemented yet");
	}*/
	
	int getProtocolType(LockRequest lcR){
		int[] pTypes = this.confTable.getProtocolType(lcR.getOpName());
		for(int i = 0; i < pTypes.length; i++){
			if(pTypes[i] == 1){
				return i;
			}
		}
		return -1;
	}
	
	/*LockReply[] getAllPermissions(ProxyTxnId txnId, LockRequest lcR){
		int[] protocolTypes = this.getProtocolTypes(lcR);
		
		for(int i = 0; i < protocolTypes.length; i++){
			
		}
	}*/
	
	LockReply getPemissions(ProxyTxnId txnId, LockRequest lcR){
		int protocolType = this.getProtocolType(lcR);
		if(protocolType == -1){
			return null;//do not need any permission
		}
		return this.getProtocol(protocolType).getPermission(txnId, lcR);
	}

	/**
	 * Sets the up client.
	 *
	 * @param memFile the mem file
	 * @param clientId the client id
	 */
	public void setUpClient(String memFile, int clientId){
		this.client = new MessageHandlerClientSide(memFile, Role.LOCKCLIENT, clientId);
		this.client.setUp();
	}

	public ConflictTable getConfTable() {
		return confTable;
	}

	public void setConfTable(ConflictTable confTable) {
		this.confTable = confTable;
	}
}
