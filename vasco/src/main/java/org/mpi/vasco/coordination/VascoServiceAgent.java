package org.mpi.vasco.coordination;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mpi.vasco.coordination.membership.Role;
import org.mpi.vasco.coordination.protocols.AsymProtocol;
import org.mpi.vasco.coordination.protocols.SymProtocol;
import org.mpi.vasco.coordination.protocols.util.ConflictTable;
import org.mpi.vasco.coordination.protocols.util.LockReply;
import org.mpi.vasco.coordination.protocols.util.LockRequest;
import org.mpi.vasco.coordination.protocols.util.Protocol;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.WriteSet;
import org.mpi.vasco.txstore.util.WriteSetEntry;
import org.mpi.vasco.util.debug.Debug;

import bftsmart.tom.ServiceProxy;

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
		//SymProtocol symProtocol = new SymProtocol(new ServiceProxy(clientId));
                SymProtocol symProtocol = new SymProtocol(clientId);
		protocols.add(symProtocol);
		AsymProtocol asymProtocol = new AsymProtocol(this.client);
		protocols.add(asymProtocol);
		
		if(protocols.size() != Protocol.NUM_OF_PROTOCOLS){
			throw new RuntimeException("The numbers of protocols do not match");
		}
		this.client.setAgent(this);
		System.out.println("VascoServiceAgent is set up!");
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
	
	//TODO: currently we assume for any conflict operation, it can only be restricted by one type of protocol
	int getProtocolType(String opName){
		int[] pTypes = this.confTable.getProtocolType(opName);
		for(int i = 0; i < pTypes.length; i++){
			if(pTypes[i] == 1){
				return i;
			}
		}
		return -1;
	}
	
	int getProtocolType(LockRequest lcR){
		return this.getProtocolType(lcR.getOpName());
	}
	
	/*LockReply[] getAllPermissions(ProxyTxnId txnId, LockRequest lcR){
		int[] protocolTypes = this.getProtocolTypes(lcR);
		
		for(int i = 0; i < protocolTypes.length; i++){
			
		}
	}*/
	
	public LockRequest generateLockRequestFromWriteSet(String opName, ProxyTxnId txnId, 
			WriteSet wse){
		//System.out.println("Generate lock request");
		int pType = this.getProtocolType(opName);
		if(pType != -1){
			//generate a lockrequest
			LockRequest lr = new LockRequest(opName);
			for (int i = 0; i < wse.size(); i ++){
				WriteSetEntry wseEntry = wse.getWriteSetEntry(i);
				//if(wseEntry.isInvariantRelated()){
					//System.out.println("The item is invariant related " + wseEntry.getObjectId());
					lr.addKey(wseEntry.getObjectId());
				//}else {
					//System.out.println("The item is not invariant related " + wseEntry.getObjectId());
				//}
			}
			assert(!lr.getKeyList().isEmpty());
			if(lr.getKeyList().isEmpty()) {
				System.out.println("You want to coordination but no key identified");
			}
			//System.out.printf("Generate a lock request %s\n", lr.toString());
			return lr;
		}else {
			//System.out.println("Cannot find protocol type for opName " + opName);
		}
		//System.out.println("Generate a null lock request");
		return null;
	}
	
	public void getPemissions(ProxyTxnId txnId, LockRequest lcR){
		Debug.println("\t------>start getting the permissions");
		if(lcR == null){
			Debug.println("\t\t ---->No permission is needed");
		}else{
			int protocolType = this.getProtocolType(lcR);
			if(protocolType == -1){
				throw new RuntimeException("protocol type must be valid");
			}
			this.getProtocol(protocolType).getPermission(txnId, lcR);
		}
		Debug.println("\t<------end getting the permissions");
	}
	
	public void cleanUpLocalOperation(ProxyTxnId txnId, LockRequest lcR){
		Debug.println("\t----> start cleanning up a local operation");
		if(lcR == null){
			Debug.println("\t\t----> no need to clean up");
		}else{
			int protocolType = this.getProtocolType(lcR);
			if(protocolType == -1){
				throw new RuntimeException("protocol type must be valid");
			}
			this.getProtocol(protocolType).cleanUp(txnId, lcR.getKeyList(), lcR.getOpName());
		}
		Debug.println("\t<---- end cleanning up a local operation");
	}
	
	public void cleanUpRemoteOperation(ProxyTxnId txnId, Set<String> keys, String opName){
		Debug.println("\t----> start cleanning up a remote operation");
		int protocolType = this.getProtocolType(opName);
		if(protocolType == -1){
			Debug.println("\t\t----> no need to clean up");
		}else{
			this.getProtocol(protocolType).cleanUp(txnId, keys, opName);
		}
		Debug.println("\t<---- end cleanning up a remote operation");
	}
	
	/*Call before committing the transaction*/
	public void waitForBeExecuted(ProxyTxnId txnId, LockRequest lcR){
		Debug.println("\t-----> start waiting for being executed");
		if(lcR == null){
			// the operations do not need to be coordinated
			Debug.println("\t\t ----> no need for coordination");
		}else{
			int protocolType = this.getProtocolType(lcR);
			if(protocolType == -1){
				throw new RuntimeException("protocol type must be valid for the lock request " + lcR.toString());
			}
			this.getProtocol(protocolType).waitForBeExcuted(txnId, lcR);
		}
		Debug.println("\t<----- end waiting for being executed");
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

	public MessageHandlerClientSide getClient() {
		return client;
	}

	public void setClient(MessageHandlerClientSide client) {
		this.client = client;
	}
}
