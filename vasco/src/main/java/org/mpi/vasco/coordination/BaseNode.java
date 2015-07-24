// $Id: BaseNode.java 1195 2011-09-24 21:00:55Z chengli $

package org.mpi.vasco.coordination;
import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.coordination.membership.Membership;
import org.mpi.vasco.coordination.membership.Role;
import org.mpi.vasco.network.ByteHandler;
import org.mpi.vasco.network.NetworkSender;
import org.mpi.vasco.network.Principal;
import org.mpi.vasco.network.messages.Message;

import java.util.Calendar;



/**
   This class is a baseline for implementing the coordinator/storage
   shim/proxy shim.  It implements the ByteHandler interface
   (handle(byte[])) and provides a collection of wrapper functions for
   sending to specific components of the system.
 **/

abstract public class BaseNode extends Throwable implements ByteHandler {

    protected Membership members;
    
    protected NetworkSender sendNet = null;


    protected int myId;
    protected Role myRole;

    public BaseNode(String membershipFile, Role myRole, int myId){
		members = new Membership(membershipFile, myRole, myId);
		this.myRole = myRole;
		this.myId = myId;
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
    
    public void sendToLockServer(Message msg){
    	send(msg, getMembership().getPrincipal(Role.LOCKSERVER, 0));
    }
    
    public void sendToLockClient(Message msg, int roleId){
    	send(msg, getMembership().getPrincipal(Role.LOCKCLIENT, roleId));
    }
    
    public void sentToAllLockClients(Message msg){
    	this.sentToAll(msg, Role.LOCKCLIENT);
    }
    
    public void sentToAll(Message msg, Role r){
    	Principal[] ps = this.getMembership().getAllPrincipalByRole(r);
    	for(int i = 0; i < ps.length; i++){
    		send(msg, ps[i]);
    	}
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
		
	}
	
	public abstract void setUp();

	public void printTime(boolean isStart) {
		StackTraceElement[] elements = getStackTrace();
		Calendar now = Calendar.getInstance();
		String startEnd = isStart? "Starting ": "Ending   ";
		String toPrint = now.getTimeInMillis()+": "+
		startEnd+elements[0].getClassName()+"."+elements[0].getMethodName();
		System.err.println(toPrint);
	}


}
