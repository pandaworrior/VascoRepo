package org.mpi.vasco.txstore.scratchpad.rdbms.util.quoddy;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.util.debug.Debug;

public class DBQUODDYShdAddToFriend extends DBShadowOperation{

	private int newFriendId;
	private String newFriendUuid;
	private String timestamp;
	private String currentUserUuid;
	

	public static DBQUODDYShdAddToFriend createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int newFriendId = dis.readInt();
		dos.writeInt(newFriendId);
		String newFriendUuid = dis.readUTF();
		dos.writeUTF(newFriendUuid);
		String timestamp = dis.readUTF();
		dos.writeUTF(timestamp);
		String currentUserUuid = dis.readUTF();
		dos.writeUTF(currentUserUuid);
		return new DBQUODDYShdAddToFriend( baos.toByteArray(), newFriendId, newFriendUuid, timestamp,currentUserUuid);
		
	}
	public static DBQUODDYShdAddToFriend createOperation(int newFriendId, String newFriendUuid,
	String timestamp, String currentUserUuid) throws IOException {
		Debug.println("add to friend operation\n");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_QUODDY_ADDTOFRIENDS);
		dos.writeInt(newFriendId);
		dos.writeUTF(newFriendUuid);
		dos.writeUTF(timestamp);
		dos.writeUTF(currentUserUuid);
		Debug.println("add to friend operation done\n");
		return new DBQUODDYShdAddToFriend( baos.toByteArray(),  newFriendId, newFriendUuid, timestamp,currentUserUuid);
		
	}
	
	protected DBQUODDYShdAddToFriend(byte[] arr) {
		super(arr);
	}
	
	protected DBQUODDYShdAddToFriend(byte[] arr, int newFriendId, String newFriendUuid,
			String timestamp, String currentUserUuid) {
		super(arr);
		this.newFriendId = newFriendId;
		this.newFriendUuid = newFriendUuid;
		this.timestamp = timestamp;
		this.currentUserUuid = currentUserUuid;
	}
	
	
	@Override
	public boolean isQuery() {
		return false;
	}

	@Override
	public boolean registerIndividualOperations() {
		return false;
	}

	@Override
	public int execute(IDatabase store) {
		return 0;
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		Debug.print("add to friend encode\n");
		dos.writeInt(newFriendId);
		dos.writeUTF(newFriendUuid);
		dos.writeUTF(timestamp);
		dos.writeUTF(currentUserUuid);
	}
	
	
	@Override
	public void executeShadow(IDefDatabase iDefDatabase) {
		// TODO Auto-generated method stub
		try {
			Debug.println("Shadow add to friend execution " );
			iDefDatabase.executeUpdate("update friend_request_collection set date_created='"+this.timestamp+"',owner_uuid='"+this.newFriendUuid+"' where id="+this.newFriendId);
			iDefDatabase.executeUpdate("insert into friend_request_collection_friend_requests (friend_request_collection_id, friend_requests_string) values ("+this.newFriendId+", '"+this.currentUserUuid+"')");
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow add to friend operation");
			e.printStackTrace();
		}
	}

}
