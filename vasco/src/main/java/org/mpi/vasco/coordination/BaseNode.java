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



// TODO: Auto-generated Javadoc
/**
   This class is a baseline for implementing the node that needs socket connections
   It implements the ByteHandler interface
   (handle(byte[])) and provides a collection of wrapper functions for
   sending to specific components of the system.
 **/

abstract public class BaseNode extends Throwable implements ByteHandler {

    /** The members. */
    protected Membership members;
    
    /** The send net. */
    protected NetworkSender sendNet = null;


    /** The my id. */
    protected int myId;
    
    /** The my role. */
    protected Role myRole;

    /**
     * Instantiates a new base node.
     *
     * @param membershipFile the membership file
     * @param myRole the my role
     * @param myId the my id
     */
    public BaseNode(String membershipFile, Role myRole, int myId){
		members = new Membership(membershipFile, myRole, myId);
		this.myRole = myRole;
		this.myId = myId;
    }

    /**
     * Gets the my role.
     *
     * @return the my role
     */
    public Role getMyRole(){
	return myRole;
    }

    /**
     * Gets the my id.
     *
     * @return the my id
     */
    public int getMyId(){
	return myId;
    }

    /**
     * Gets the membership.
     *
     * @return the membership
     */
    public Membership getMembership(){
	return members;
    }
    
    /**
     * Send to lock server.
     *
     * @param msg the msg
     */
    public void sendToLockServer(Message msg){
    	send(msg, getMembership().getPrincipal(Role.LOCKSERVER, 0));
    }
    
    /**
     * Send to lock client.
     *
     * @param msg the msg
     * @param roleId the role id
     */
    public void sendToLockClient(Message msg, int roleId){
    	send(msg, getMembership().getPrincipal(Role.LOCKCLIENT, roleId));
    }
    
    /**
     * Sent to all lock clients.
     *
     * @param msg the msg
     */
    public void sentToAllLockClients(Message msg){
    	this.sentToAll(msg, Role.LOCKCLIENT);
    }
    
    /**
     * Sent to all.
     *
     * @param msg the msg
     * @param r the r
     */
    public void sentToAll(Message msg, Role r){
    	Principal[] ps = this.getMembership().getAllPrincipalByRole(r);
    	for(int i = 0; i < ps.length; i++){
    		send(msg, ps[i]);
    	}
    }
    
    /**
     * Send.
     *
     * @param msg the msg
     * @param rcpt the rcpt
     */
    private void send(Message msg, Principal rcpt){
	if (sendNet != null)
	    sendNet.send(msg.getBytes(), rcpt.getInetSocketAddress());
	else
	    throw new RuntimeException("no sending network available! "+sendNet);
    }

    /**
     *    Listen to appropriate sockets and call handle on all
     *    appropriate incoming messages.
     */
    public void start() {
	if ( sendNet == null) {
	    throw new RuntimeException("dont have a network");
	} else {
	    //Debug.println("wtf");
        }
    }


    /**
     * Sets the sender.
     *
     * @param sender the new sender
     */
    public void setSender(NetworkSender sender){
    	Debug.println("set up the sender network");
    	sendNet = sender;
    }

	/**
	 * Stop.
	 */
	public void stop() {
		
	}
	
	/**
	 * Sets the up.
	 */
	public abstract void setUp();

	/**
	 * Prints the time.
	 *
	 * @param isStart the is start
	 */
	public void printTime(boolean isStart) {
		StackTraceElement[] elements = getStackTrace();
		Calendar now = Calendar.getInstance();
		String startEnd = isStart? "Starting ": "Ending   ";
		String toPrint = now.getTimeInMillis()+": "+
		startEnd+elements[0].getClassName()+"."+elements[0].getMethodName();
		System.err.println(toPrint);
	}


}
