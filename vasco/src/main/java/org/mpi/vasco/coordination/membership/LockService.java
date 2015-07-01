package org.mpi.vasco.coordination.membership;

import org.mpi.vasco.network.Principal;

public class LockService {
	
	private LockClient[] lockClients;
	private LockServer[] lockServers;
	
	public LockService(LockClient[] _lClients, LockServer[] _lServers){
		this.setLockClients(_lClients);
		this.setLockServers(lockServers);
	}
	
	public LockClient[] getLockClients() {
		return lockClients;
	}
	public void setLockClients(LockClient[] lockClients) {
		this.lockClients = lockClients;
	}
	public LockServer[] getLockServers() {
		return lockServers;
	}
	public void setLockServers(LockServer[] lockServers) {
		this.lockServers = lockServers;
	}
	
	public LockClient getLockClient(int cId){
		return this.getLockClients()[cId];
	}
	
	public LockServer getLockServer(int sId){
		return this.getLockServers()[sId];
	}
	
	public Principal getPrincipal(Role role, int id){
		switch(role){
		case LOCKSERVER:
			return this.getLockServer(id);
		case LOCKCLIENT:
			return this.getLockClient(id);
			default:
				throw new RuntimeException("Cannot find the role " + role);
		}
	}
	
	public String toString(){
		StringBuilder strBuild = new StringBuilder();
		
		return strBuild.toString();
	}

}
