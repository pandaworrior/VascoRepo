package org.mpi.vasco.txstore.scratchpad.rdbms.util.quoddy;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.util.debug.Debug;

public class DBQUODDYShdConfirmFriend extends DBShadowOperation{

	private String now;
	private String friendCollectionCUOwnerUuid;
	private int friendCollectionCUId;
	private String friendCollectionNFOwnerUuid;
	private int friendCollectionNFId;
	private String friendRequestsCUOwnerUuid;
	private int friendRequestsCUId;
	private String newFriendUuid;
	

	public static DBQUODDYShdConfirmFriend createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		String now = dis.readUTF();
		dos.writeUTF(now);
		String friendCollectionCUOwnerUuid = dis.readUTF();
		dos.writeUTF(friendCollectionCUOwnerUuid);
		int friendCollectionCUId = dis.readInt();
		dos.writeInt(friendCollectionCUId);
		String friendCollectionNFOwnerUuid = dis.readUTF();
		dos.writeUTF(friendCollectionNFOwnerUuid);
		int friendCollectionNFId = dis.readInt();
		dos.writeInt(friendCollectionNFId);
		String friendRequestsCUOwnerUuid = dis.readUTF();
		dos.writeUTF(friendRequestsCUOwnerUuid);
		int friendRequestsCUId = dis.readInt();
		dos.writeInt(friendRequestsCUId);
		String newFriendUuid = dis.readUTF();
		dos.writeUTF(newFriendUuid);
		return new DBQUODDYShdConfirmFriend( baos.toByteArray(),now, friendCollectionCUOwnerUuid,friendCollectionCUId, friendCollectionNFOwnerUuid,friendCollectionNFId,friendRequestsCUOwnerUuid,
		friendRequestsCUId,newFriendUuid);
		
	}
	public static DBQUODDYShdConfirmFriend createOperation(String now, String friendCollectionCUOwnerUuid,int friendCollectionCUId, String friendCollectionNFOwnerUuid,int friendCollectionNFId,String friendRequestsCUOwnerUuid,
	int friendRequestsCUId,String newFriendUuid) throws IOException {
		Debug.println("confirm friend operation\n");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_QUODDY_COMFIRMFRIEND);
		dos.writeUTF(now);
		dos.writeUTF(friendCollectionCUOwnerUuid);
		dos.writeInt(friendCollectionCUId);
		dos.writeUTF(friendCollectionNFOwnerUuid);
		dos.writeInt(friendCollectionNFId);
		dos.writeUTF(friendRequestsCUOwnerUuid);
		dos.writeInt(friendRequestsCUId);
		dos.writeUTF(newFriendUuid);
		Debug.println("confirm friend operation done\n");
		return new DBQUODDYShdConfirmFriend( baos.toByteArray(),now, friendCollectionCUOwnerUuid,friendCollectionCUId, friendCollectionNFOwnerUuid,friendCollectionNFId,friendRequestsCUOwnerUuid,
				friendRequestsCUId, newFriendUuid);
		
	}
	
	protected DBQUODDYShdConfirmFriend(byte[] arr) {
		super(arr);
	}
	
	protected DBQUODDYShdConfirmFriend(byte[] arr, String now, String friendCollectionCUOwnerUuid,int friendCollectionCUId, String friendCollectionNFOwnerUuid,int friendCollectionNFId,String friendRequestsCUOwnerUuid,
	int friendRequestsCUId,String newFriendUuid) {
		super(arr);
		this.now = now;
		this.friendCollectionCUOwnerUuid = friendCollectionCUOwnerUuid;
		this.friendCollectionCUId = friendCollectionCUId;
		this.friendCollectionNFOwnerUuid = friendCollectionNFOwnerUuid;
		this.friendCollectionNFId = friendCollectionNFId;
		this.friendRequestsCUOwnerUuid = friendRequestsCUOwnerUuid;
		this.friendRequestsCUId = friendRequestsCUId;
		this.newFriendUuid = newFriendUuid;
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
		Debug.print("confirm friend encode\n");
		dos.writeUTF(now);
		dos.writeUTF(friendCollectionCUOwnerUuid);
		dos.writeInt(friendCollectionCUId);
		dos.writeUTF(friendCollectionNFOwnerUuid);
		dos.writeInt(friendCollectionNFId);
		dos.writeUTF(friendRequestsCUOwnerUuid);
		dos.writeInt(friendRequestsCUId);
		dos.writeUTF(newFriendUuid);
	}
	
	
	@Override
	public void executeShadow(IDefDatabase iDefDatabase) {
		// TODO Auto-generated method stub
		try {
			Debug.println("Shadow confirm execution " );
			String sql = "update friend_collection " +
					"set date_created='"+this.now+"', 	owner_uuid='"+this.friendCollectionCUOwnerUuid+"' " +
					" where	id="+this.friendCollectionCUId;
			iDefDatabase.executeUpdate(sql);
			
			sql = "update friend_collection " +
					" set date_created='"+this.now+"', 	owner_uuid='"+this.friendCollectionNFOwnerUuid+"' " +
					" where	id="+this.friendCollectionNFId;
			iDefDatabase.executeUpdate(sql);
			
			sql = "update	friend_request_collection	" +
					" set	date_created='"+this.now+"',	owner_uuid='"+this.friendRequestsCUOwnerUuid+"' " +
					" where id="+this.friendRequestsCUId;
			iDefDatabase.executeUpdate(sql);
			
			sql = "insert	into friend_collection_friends (friend_collection_id, friends_string) values ( "
			   +this.friendCollectionCUId+",'"+this.friendCollectionNFOwnerUuid+"' )";
			iDefDatabase.executeUpdate(sql);
			
			sql = "insert	into friend_collection_friends (friend_collection_id, friends_string) values ("
			+this.friendCollectionNFId+",'"+this.friendCollectionCUOwnerUuid+"' )";
			iDefDatabase.executeUpdate(sql);
			
			sql ="delete from friend_request_collection_friend_requests " +
					 " where friend_request_collection_id="+this.friendRequestsCUId+"	and friend_requests_string='"+this.newFriendUuid+"'";
			iDefDatabase.executeUpdate(sql);
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow confirm friend operation");
			e.printStackTrace();
		}
	}

}
