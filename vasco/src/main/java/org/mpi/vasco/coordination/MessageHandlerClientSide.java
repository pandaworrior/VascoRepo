package org.mpi.vasco.coordination;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.mpi.vasco.coordination.membership.Role;
import org.mpi.vasco.coordination.protocols.messages.LockRepMessage;
import org.mpi.vasco.coordination.protocols.messages.LockReqMessage;
import org.mpi.vasco.coordination.protocols.messages.MessageFactory;
import org.mpi.vasco.coordination.protocols.messages.MessageTags;
import org.mpi.vasco.coordination.protocols.util.LockReply;
import org.mpi.vasco.coordination.protocols.util.LockRequest;
import org.mpi.vasco.coordination.protocols.util.Protocol;
import org.mpi.vasco.network.ParallelPassThroughNetworkQueue;
import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.network.netty.NettyTCPReceiver;
import org.mpi.vasco.network.netty.NettyTCPSender;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.util.debug.Debug;

public class MessageHandlerClientSide extends BaseNode{

	private static final long serialVersionUID = -4040510651915229397L;
	
	private MessageFactory mf;
	
	private VascoServiceAgent agent;

	public MessageHandlerClientSide(String membershipFile, Role myRole, int myId) {
		super(membershipFile, myRole, myId);
		mf = new MessageFactory();
		Debug.printf("Set up lock client %d for lock server", myId);
	}

	@Override
	public void handle(byte[] bytes) {
		MessageBase msg = mf.fromBytes(bytes);
		if (msg == null) {
			throw new RuntimeException("Should never receive a null message");
		}
		
		switch (msg.getTag()) {
		case MessageTags.LOCKREP:
			//Receive the reply from the centralized server
			process((LockRepMessage) msg);
			break;
		case MessageTags.LOCKREQ:
			//Barrier, receive request from the client
			process((LockReqMessage) msg);
			break;
		default:
			throw new RuntimeException("invalid message tag: " + msg.getTag());
		}
		
	}

	private void process(LockRepMessage msg) {
		Debug.printf("Receive a lock reply message from server content %s", msg.toString());
		LockReply lcReply = msg.getLockRly();
		int pType = lcReply.getProtocolType();
		Protocol p = this.getAgent().getProtocol(pType);
		if(p == null){
			throw new RuntimeException("No such a protocol " + lcReply.getProtocolType());
		}
		p.addLockReply(msg.getProxyTxnId(), lcReply);
		mf.returnLockRepMessage(msg);
		
	}
	
	private void process(LockReqMessage msg){
		Debug.printf("Receive a lock request message from client content %s", msg.toString());
		Protocol p = this.getAgent().getProtocol(Protocol.PROTOCOL_ASYM);
		if(p == null){
			throw new RuntimeException("No such a protocol " + Protocol.PROTOCOL_ASYM);
		}
		
		LockReply lcReply = p.getLocalPermission(msg.getProxyTxnId(), msg.getLockReq());
		LockRepMessage repMsg = new LockRepMessage(msg.getProxyTxnId(), lcReply);
		int clientId = msg.getGlobalProxyId();
		this.sendToLockClient(repMsg, clientId);
		mf.returnLockReqMessage(msg);
		mf.returnLockRepMessage(repMsg);
	}

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
	}
	
	//for testing the functionalities
	public void sendMessages(int n){
		for(int i = 0; i < n; i++){
			LockRequest lR = new LockRequest("a");//bit flip to a or b
			LockReqMessage msg = new LockReqMessage(new ProxyTxnId(0,this.getMyId(),i),
					this.getMyId(), lR);
			this.sendToLockServer(msg);
			mf.returnLockReqMessage(msg);
		}
	}
	
	static int counterPerClient = 0;
	public LockReqMessage generateRandomRequestMessage(){
		ProxyTxnId txnId = new ProxyTxnId(0, 0, counterPerClient++);
		LockRequest lr = new LockRequest(RandomStringUtils.randomAlphabetic(5).toLowerCase());
		Random random = new Random();
		int numOfKeyGroups = random.nextInt(5);
		Debug.printf("Generate %d key groups\n", numOfKeyGroups);
		for(int i = 0; i < numOfKeyGroups; i++){
			String keyGroupStr = RandomStringUtils.randomAlphabetic(10).toLowerCase();
			int numOfKeys = random.nextInt(10) + 1;
			while(numOfKeys > 0){
				String keyStr = RandomStringUtils.randomAlphabetic(5).toLowerCase();
				if(keyStr.equalsIgnoreCase("")){
					throw new RuntimeException("You generated an empty string\n");
				}
				numOfKeys--;
				lr.addKey(keyGroupStr, keyStr);
			}
		}
		LockReqMessage msg = new LockReqMessage(txnId, myId, lr);
		Debug.println("Randomly generate a request message\n");
		Debug.println(msg.toString());
		return msg;
	}
	
	public void sendRandomRequestMessage(){
		Debug.println("Send a random request message to server");
		LockReqMessage msg = this.generateRandomRequestMessage();
		
		this.sendToLockServer(msg);
	}
	
	public void test(){
		Debug.println("Test the client and server");
		Scanner keyboard = new Scanner(System.in);
		while(true){
			Debug.println("[1] send a request msg and [2] quit"+ "\n");
			int input=keyboard.nextInt();
			switch(input){
			case 1:
				sendRandomRequestMessage();
				break;
			case 2:
				keyboard.close();
				return;
			default:
				keyboard.close();
				throw new RuntimeException("Not specified yet");
			}
		}
	}
	
	public static void main(String[] args){
		if(args.length != 2){
			System.out.println("MessageHandlerServerSide [memshipFile] [id]");
			System.exit(-1);
		}
		
		String membershipFile = args[0];
		int myId = Integer.parseInt(args[1]);
		
		MessageHandlerClientSide mClient = new MessageHandlerClientSide(membershipFile, Role.LOCKCLIENT, myId);
		// set up for outgoing messages
		NettyTCPSender sendNet = new NettyTCPSender();
		mClient.setSender(sendNet);
		sendNet.setTCPNoDelay(false);
		sendNet.setKeepAlive(true);
		
		int threadCount = 2;
		ParallelPassThroughNetworkQueue ptnq = new ParallelPassThroughNetworkQueue(
				mClient, threadCount);
		NettyTCPReceiver rcv = new NettyTCPReceiver(mClient.getMembership().getMe()
				.getInetSocketAddress(), ptnq, threadCount);
	
		mClient.test();
	}

	public VascoServiceAgent getAgent() {
		return agent;
	}

	public void setAgent(VascoServiceAgent agent) {
		this.agent = agent;
	}

}
