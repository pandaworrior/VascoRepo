package org.mpi.vasco.txstore.membership;

import org.mpi.vasco.network.Principal;

public class Datacenter{

    Coordinator coord[];
    RemoteCoordinator rcoord[];
    Proxy proxies[];
    Storage storage[];
    int dcId;

    public Datacenter(int dcId, Coordinator c[], RemoteCoordinator rc[], Proxy p[], Storage s[]){
	coord = c;
	rcoord = rc;
	proxies = p;
	storage = s;
	this.dcId = dcId;
    }

    public Datacenter(int dcId, Coordinator c, RemoteCoordinator rc, Proxy p[], Storage s[]){
	coord = new Coordinator[1];
	rcoord = new RemoteCoordinator[1];
	coord[0]= c;
	rcoord[0] = rc;
	proxies = p;
	storage = s;
	this.dcId = dcId;
    }

    public int getDatacenterId(){
	return dcId;
    }

    public Coordinator[] getCoordinators(){
	return coord;
    }
    
    public RemoteCoordinator[] getRemoteCoordinators(){
    	return rcoord;
    }

    public Coordinator getCoordinator(){
	return getCoordinator(0);
    }
    
    public RemoteCoordinator getRemoteCoordinator(){
    	return getRemoteCoordinator(0);
    }

    public Coordinator getCoordinator(int i){
	return coord[0];
    }
    
    public RemoteCoordinator getRemoteCoordinator(int i){
    	return rcoord[0];
    }

    public Proxy[] getProxies(){
	return proxies;
    }
    
    public int getProxyCount(){
    	return proxies.length;
    }

    public Proxy getProxy(int i){
	return proxies[i];
    }

    public Storage[] getStorage(){
	return storage;
    }

    public Storage getStorage(int i){
	return storage[i];
    }

    public int getStorageCount(){
	return storage.length;
    }

    public Principal getPrincipal(Role role, int roleid){
	switch(role){
	case COORDINATOR: return getCoordinator(roleid);
	case REMOTECOORDINATOR: return getRemoteCoordinator(roleid);
	case PROXY: return getProxy(roleid);
	case STORAGE: return getStorage(roleid);
	default: throw new RuntimeException("Unkown role: "+role);
	}
    }

    public String toString(){
	String tmp = "";
	for (int i = 0; i < getCoordinators().length; i++)
	    tmp += "\n\t"+getCoordinators()[i];
	for (int i = 0; i < getRemoteCoordinators().length; i++)
	    tmp += "\n\t"+getRemoteCoordinators()[i];
	for (int i = 0; i < getProxies().length; i++)
	    tmp += "\n\t"+getProxies()[i];
	for (int i = 0; i < getStorage().length; i++)
	    tmp += "\n\t"+getStorage()[i];
	return tmp;
    }

}