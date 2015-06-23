// $Id: BaseNode.java 1195 2011-09-24 21:00:55Z chengli $

package org.mpi.vasco.txstore;
import org.mpi.vasco.util.debug.Debug;


import org.mpi.vasco.txstore.membership.Role;
import org.mpi.vasco.txstore.membership.Membership;
import org.mpi.vasco.txstore.membership.Principal;
import org.mpi.vasco.txstore.membership.Datacenter;

import org.mpi.vasco.util.UnsignedTypes;
import org.mpi.vasco.txstore.messages.Message;
import org.mpi.vasco.txstore.util.StorageList;

import org.mpi.vasco.network.ByteHandler;
import org.mpi.vasco.network.NetworkSender;

import java.security.interfaces.*;
import javax.crypto.*;

import java.util.Calendar;
import java.util.Iterator;


/**
   This class is a baseline for implementing the coordinator/storage
   shim/proxy shim.  It implements the ByteHandler interface
   (handle(byte[])) and provides a collection of wrapper functions for
   sending to specific components of the system.
 **/

abstract public class BaseNode extends Throwable implements ByteHandler {

    protected Membership members;
    
    protected NetworkSender sendNet = null;


    protected int myDC, myId;
    protected Role myRole;

    public BaseNode(String membershipFile, int datacenter, Role myRole, int myId){
	members = new Membership(membershipFile, datacenter, myRole, myId);
	myDC = datacenter;
	this.myRole = myRole;
	this.myId = myId;
    }
    
    public int getMyDatacenterId(){
	return myDC;
    }

    public Role getMyRole(){
	return myRole;
    }

    public int getMyId(){
	return myId;
    }

    public Membership getMembership(){
	return members;
    }

    public int getDatacenterCount(){
	return members.getDatacenterCount();
    }

    public int getStorageCount(){
	return members.getStorageCount();
    }

    public void sendToCoordinator(Message msg){
	send(msg, getMembership().getDatacenter(getMyDatacenterId()).getCoordinator());
    }
    
    public void sendToOtherCoordinator(Message msg){
    	Datacenter[] dcs = getMembership().getDatacenters();
    	for (int i = 0; i < dcs.length; i++)
    	    send(msg, dcs[i].getCoordinator());
    }
    
    public void sendToOtherCoordinator(Message msg, int dc){
    	send(msg, getMembership().getDatacenter(dc).getCoordinator());
    }
    
    //send to remote coordinator acceptor
    public void sendToRemoteCoordinator(Message msg){
    	send(msg, getMembership().getDatacenter(getMyDatacenterId()).getRemoteCoordinator());
    }

    public void sendToRemoteCoordinator(Message msg, int dc){
	send(msg, getMembership().getDatacenter(dc).getRemoteCoordinator());
    }

    public void sendToAllRemoteCoordinator(Message msg){
	Datacenter[] dcs = getMembership().getDatacenters();
	for (int i = 0; i < dcs.length; i++)
	    send(msg, dcs[i].getRemoteCoordinator());
    }
    public void sendToOtherRemoteCoordinator(Message msg){
	Datacenter[] dcs = getMembership().getDatacenters();
	for (int i = 0; i < dcs.length; i++)
	    if (i != getMyDatacenterId())
		send(msg, dcs[i].getRemoteCoordinator());
    }


    public void sendToRemoteStorage(Message msg, int dc){
	send(msg, getMembership().getDatacenter(dc).getStorage(getMyId()));
    }
    
    public void sendToAllRemoteStorage(Message msg){
	sendToAllRemoteStorage(msg, -1);
    }

    public void sendToOtherRemoteStorage(Message msg){
	sendToAllRemoteStorage(msg, getMyDatacenterId());
    }

    public void sendToAllRemoteStorage(Message msg, int me){
	Datacenter[] dcs = getMembership().getDatacenters();
	for (int i = 0; i < dcs.length; i++)
	    if (i != me)
		send(msg, dcs[i].getStorage(getMyId()));
    }

    public void sendToStorage(Message msg, int id){
	send(msg, getMembership().getDatacenter(getMyDatacenterId()).getStorage(id));
    }

    public void sendToAllStorage(Message msg){
	for (int i = 0; i < getStorageCount(); i++)
	    sendToStorage(msg, i);
    }
    
    public void sendToAllStorage(Message msg, StorageList slist){
    	for(int i = 0; i < slist.getStorageCount(); i++){
    		sendToStorage(msg, slist.getStorage(i));
    	}
    }

    public void sendToProxy(Message msg, int id){
	send(msg, getMembership().getDatacenter(getMyDatacenterId()).getProxy(id));
    }

    private void send(Message msg, Principal rcpt){
	if (sendNet != null)
	    sendNet.send(msg.getBytes(), rcpt.getInetSocketAddress());
	else
	    throw new RuntimeException("no sending network available! "+sendNet);
    }

    

    /**
   Listen to appropriate sockets and call handle on all
   appropriate incoming messages
     **/
    public void start() {
	if ( sendNet == null) {
	    throw new RuntimeException("dont have a network");
	} else {
	    //Debug.println("wtf");
        }
    }


    public void setSender(NetworkSender sender){
	Debug.println("set up the sender network");
	sendNet = sender;
    }

	public void stop() {

		//throw new RuntimeException("Not yet implemented");
	}

	public void printTime(boolean isStart) {
		StackTraceElement[] elements = getStackTrace();
		Calendar now = Calendar.getInstance();
		String startEnd = isStart? "Starting ": "Ending   ";
		String toPrint = now.getTimeInMillis()+": "+
		startEnd+elements[0].getClassName()+"."+elements[0].getMethodName();
		System.err.println(toPrint);
	}


}
