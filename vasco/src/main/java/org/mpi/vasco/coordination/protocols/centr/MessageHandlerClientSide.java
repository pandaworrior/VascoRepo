package org.mpi.vasco.coordination.protocols.centr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.lang3.RandomStringUtils;
import org.jgroups.util.Util;
import org.mpi.vasco.coordination.BaseNode;
import org.mpi.vasco.coordination.membership.Role;
import org.mpi.vasco.coordination.protocols.messages.LockRepMessage;
import org.mpi.vasco.coordination.protocols.messages.LockReqMessage;
import org.mpi.vasco.coordination.protocols.messages.MessageFactory;
import org.mpi.vasco.coordination.protocols.messages.MessageTags;
import org.mpi.vasco.coordination.protocols.util.LockRequest;
import org.mpi.vasco.network.ParallelPassThroughNetworkQueue;
import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.network.netty.NettyTCPReceiver;
import org.mpi.vasco.network.netty.NettyTCPSender;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.util.debug.Debug;

public class MessageHandlerClientSide extends BaseNode{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4040510651915229397L;
	
	private MessageFactory mf;

	public MessageHandlerClientSide(String membershipFile, Role myRole, int myId) {
		super(membershipFile, myRole, myId);
		mf = new MessageFactory();
		Debug.printf("Set up lock client %d for lock server", myId);
	}

	@Override
	public void handle(byte[] bytes) {
		// TODO Auto-generated method stub
		MessageBase msg = mf.fromBytes(bytes);
		if (msg == null) {
			throw new RuntimeException("Should never receive a null message");
		}
		
		switch (msg.getTag()) {
		case MessageTags.LOCKREP:
			process((LockRepMessage) msg);
			break;
		default:
			throw new RuntimeException("invalid message tag: " + msg.getTag());
		}
		
	}

	private void process(LockRepMessage msg) {
		// TODO Auto-generated method stub
		Debug.printf("Receive a lock reply message from server content %s", msg.toString());
		mf.returnLockRepMessage(msg);
		
	}

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
		
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
		Debug.println("Randomly generate a request message\n");
		ProxyTxnId txnId = new ProxyTxnId(0, 0, counterPerClient++);
		LockRequest lr = new LockRequest(RandomStringUtils.randomAlphabetic(5).toLowerCase());
		Random random = new Random();
		int numOfKeys = random.nextInt(10);
		Debug.printf("Generate %d keys\n", numOfKeys);
		List<String> keys = new ArrayList<String>();
		for(int i = 0; i < numOfKeys; i++){
			String key = RandomStringUtils.randomAlphabetic(i+1).toLowerCase();
			keys.add(key);
		}
		lr.setKeyList(keys);
		LockReqMessage msg = new LockReqMessage(txnId, myId, lr);
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

}
