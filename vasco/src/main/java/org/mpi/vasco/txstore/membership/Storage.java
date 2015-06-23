package org.mpi.vasco.txstore.membership;

import java.net.InetAddress;

public class Storage extends Principal{
    final int storageId;
    final int dcId;
    public Storage(int storageId, int dcId, String host, int port){
	super(host, port);
	this.storageId = storageId;
	this.dcId = dcId;
    }
    public Storage(int storageId, int dcId, InetAddress host, int port){
	super(host, port);
	this.storageId = storageId;
	this.dcId = dcId;
    }

    public int getStorageId(){
	return storageId;
    }

    public int getDatacenterId(){
	return dcId;
    }

    public String toString(){
	return "++ STORAGE " + super.toString();
    }
}