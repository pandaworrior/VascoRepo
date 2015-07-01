package org.mpi.vasco.txstore.membership;

import java.net.InetAddress;

import org.mpi.vasco.network.Principal;

public class Proxy extends Principal{

    private int proxyId;
    private int dcId;

    public Proxy(int proxyId, int dcId, String host, int port){
	super(host, port);
	this.proxyId = proxyId;
	this.dcId = dcId;
    }

    public Proxy(int proxyId, int dcId, InetAddress host, int port){
	super(host, port);
	this.proxyId = proxyId;
	this.dcId = dcId;
    }

    public int getProxyId(){
	return proxyId;
    }

    public int getDatacenterId(){
	return dcId;
    }

    public String toString(){
	return "++ PROXY " + super.toString();
    }

}