package org.mpi.vasco.txstore.membership;

import java.net.InetAddress;
import java.net.InetSocketAddress;


public class Principal{
    InetAddress host;
    int port;
    static int count = 0;
    int uniqueid;
    InetSocketAddress isa;

    protected static void resetUniqueIdentifiers(){
	count = 0;
    }

    public Principal(String host, int port){
	try{
	    this.host = InetAddress.getByName(host);
	}catch(Exception e){
	    throw new RuntimeException(e);
	}
	this.port = port;
	uniqueid = count++;
	this.isa = new InetSocketAddress(host, port);
	System.out.println("ip address is:  "+isa);

    }

    public Principal(InetAddress host, int port){
	this.host = host;
	this.port = port;
	uniqueid = count++;
	this.isa = new InetSocketAddress(host, port);
	System.out.println("ip address is:  "+isa);
    }

    public InetSocketAddress getInetSocketAddress(){
	return isa;
    }

    public InetAddress getHost(){
	return host;
    }

    public int getPort(){
	return port;
    }
	
    public int getUniqueId(){
	return uniqueid;
    }
    

    public String toString(){
	return "<"+getHost()+":"+getPort()+" ** "+getUniqueId()+">";
    }
}