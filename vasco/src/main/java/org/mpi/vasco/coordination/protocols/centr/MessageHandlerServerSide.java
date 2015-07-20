/*
 * This class is doing x;
 * Created by @Creator on @Date
 */
package org.mpi.vasco.coordination.protocols.centr;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.RandomUtils;
import org.mpi.vasco.coordination.BaseNode;
import org.mpi.vasco.coordination.membership.Role;
import org.mpi.vasco.coordination.protocols.centr.rsm.CounterService;
import org.mpi.vasco.coordination.protocols.messages.LockRepMessage;
import org.mpi.vasco.coordination.protocols.messages.LockReqMessage;
import org.mpi.vasco.coordination.protocols.messages.MessageFactory;
import org.mpi.vasco.coordination.protocols.messages.MessageTags;
import org.mpi.vasco.coordination.protocols.util.LockReply;
import org.mpi.vasco.network.ParallelPassThroughNetworkQueue;
import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.network.netty.NettyTCPReceiver;
import org.mpi.vasco.network.netty.NettyTCPSender;
import org.mpi.vasco.util.debug.Debug;


// TODO: Auto-generated Javadoc
/**
 * The Class MessageHandlerServerSide.
 */
public class MessageHandlerServerSide extends BaseNode{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7157915157725026251L;
	
	private String memFile;
	
	/** The mf. */
	private static MessageFactory mf = new MessageFactory();
	
	/** The rsm lock service. */
	private CounterService rsmLockService;

	/**
	 * Instantiates a new message handler server side.
	 *
	 * @param membershipFile the membership file
	 * @param myRole the my role
	 * @param myId the my id
	 */
	public MessageHandlerServerSide(String membershipFile, 
			Role myRole, int myId) {
		super(membershipFile, myRole, myId);
		this.setRsmLockService(null);
		this.memFile = membershipFile;
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.network.ByteHandler#handle(byte[])
	 */
	@Override
	public void handle(byte[] bytes) {
		// TODO Auto-generated method stub
		MessageBase msg = mf.fromBytes(bytes);
		if (msg == null) {
			throw new RuntimeException("Should never receive a null message");
		}
		
		switch (msg.getTag()) {
		case MessageTags.LOCKREQ:
			process((LockReqMessage) msg);
			break;
		default:
			throw new RuntimeException("invalid message tag: " + msg.getTag());
		}
	}
	
	public LockRepMessage generateRandomReplyMessage(LockReqMessage msg){
		Debug.println("Generate a random reply message");
		LockReply rp = new LockReply(msg.getLockReq().getOpName());
		Iterator it = msg.getLockReq().getKeyList().entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Set<String>> e = (Entry<String, Set<String>>) it.next();
			String keyGroup = e.getKey();
			for(String key : e.getValue()){
				int counter = RandomUtils.nextInt(0, 100);
				rp.addKeyCounterPair(keyGroup, key, counter);
			}
		}
		LockRepMessage pMsg = new LockRepMessage(msg.getProxyTxnId(), rp);
		return pMsg;
	}
	
	/**
	 * Process for test
	 *
	 * @param msg the msg
	 */
	/*private void process(LockReqMessage msg){
		Debug.printf("Receive from client %d a lock request message %s\n", msg.getGlobalProxyId(), msg.toString());
		//call
		//if(this.getRsmLockService() == null){
			//throw new RuntimeException("RSM is not set");
		//}else{
			//LockRepMessage repMsg = this.getRsmLockService().put(msg.getProxyTxnId().toString(), msg.getLockReq());
			LockRepMessage repMsg = this.generateRandomReplyMessage(msg);
			int clientId = msg.getGlobalProxyId();
			this.sendToLockClient(repMsg, clientId);
			Debug.printf("Send to lock client with id %d lock reply message %s", clientId, repMsg.toString());
			mf.returnLockReqMessage(msg);
			mf.returnLockRepMessage(repMsg);
		//}
	}*/
	
	/**
	 * Process.
	 *
	 * @param msg the msg
	 */
	private void process(LockReqMessage msg){
		Debug.printf("Receive from client %d a lock request message %s\n", msg.getGlobalProxyId(), msg.toString());
		if(this.getRsmLockService() == null){
			throw new RuntimeException("RSM is not set");
		}else{
			//LockRepMessage repMsg = this.getRsmLockService().put(msg.getProxyTxnId().toString(), msg.getLockReq());
			LockRepMessage repMsg = null;
			LockReply reply = null;
			try {
				reply = this.getRsmLockService().getAndAdd(msg.getLockReq());
			} catch (Exception e) {
				e.printStackTrace();
			}
			repMsg = new LockRepMessage(msg.getProxyTxnId(), reply);
			int clientId = msg.getGlobalProxyId();
			this.sendToLockClient(repMsg, clientId);
			Debug.printf("Send to lock client with id %d lock reply message %s", clientId, repMsg.toString());
			mf.returnLockReqMessage(msg);
			mf.returnLockRepMessage(repMsg);
		}
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.BaseNode#setUp()
	 */
	@Override
	public void setUp() {
		// set up for outgoing messages
		NettyTCPSender sendNet = new NettyTCPSender();
		this.setSender(sendNet);
		sendNet.setTCPNoDelay(false);
		sendNet.setKeepAlive(true);
		
		int threadCount = 2;
		ParallelPassThroughNetworkQueue ptnq = new ParallelPassThroughNetworkQueue(
				this, threadCount);
		NettyTCPReceiver rcv = new NettyTCPReceiver(this.getMembership().getMe()
				.getInetSocketAddress(), ptnq, threadCount);
		
		Debug.printf("Set up the server %d for lock client", myId);
	}

	/**
	 * Gets the rsm lock service.
	 *
	 * @return the rsm lock service
	 */
	public CounterService getRsmLockService() {
		return rsmLockService;
	}

	/**
	 * Sets the rsm lock service.
	 *
	 * @param rsmLockService the new rsm lock service
	 */
	public void setRsmLockService(CounterService rsmLockService) {
		this.rsmLockService = rsmLockService;
		Debug.println("Set rsm lock service");
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){
		if(args.length != 2){
			System.out.println("MessageHandlerServerSide [memshipFile] [id]");
			System.exit(-1);
		}
		
		String membershipFile = args[0];
		int myId = Integer.parseInt(args[1]);
		
		MessageHandlerServerSide mServer = new MessageHandlerServerSide(membershipFile, Role.LOCKSERVER, myId);
		// set up for outgoing messages
		NettyTCPSender sendNet = new NettyTCPSender();
		mServer.setSender(sendNet);
		sendNet.setTCPNoDelay(false);
		sendNet.setKeepAlive(true);
		
		int threadCount = 2;
		ParallelPassThroughNetworkQueue ptnq = new ParallelPassThroughNetworkQueue(
				mServer, threadCount);
		NettyTCPReceiver rcv = new NettyTCPReceiver(mServer.getMembership().getMe()
				.getInetSocketAddress(), ptnq, threadCount);
	
	}

}
