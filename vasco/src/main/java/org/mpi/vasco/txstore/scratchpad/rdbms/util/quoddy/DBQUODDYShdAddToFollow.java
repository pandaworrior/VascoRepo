package org.mpi.vasco.txstore.scratchpad.rdbms.util.quoddy;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.util.debug.Debug;

public class DBQUODDYShdAddToFollow extends DBShadowOperation{

	private String iFollowCollectionCreateDate;
	private String destinationUserUuid;
	private int iFollowCollectionId;
	private String targetUserUuid;
	

	public static DBQUODDYShdAddToFollow createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		String iFollowCollectionCreateDate = dis.readUTF();
		dos.writeUTF(iFollowCollectionCreateDate);
		String destinationUserUuid = dis.readUTF();
		dos.writeUTF(destinationUserUuid);
		int iFollowCollectionId = dis.readInt();
		dos.writeInt(iFollowCollectionId);
		String targetUserUuid = dis.readUTF();
		dos.writeUTF(targetUserUuid);
		return new DBQUODDYShdAddToFollow( baos.toByteArray(), iFollowCollectionCreateDate,
				destinationUserUuid, iFollowCollectionId,targetUserUuid);
		
	}
	public static DBQUODDYShdAddToFollow createOperation(String iFollowCollectionCreateDate,
	String destinationUserUuid,	int iFollowCollectionId,String targetUserUuid) throws IOException {
		Debug.println("add to follow operation\n");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_QUPDDY_FOLLOWSOMEBODY);
		dos.writeUTF(iFollowCollectionCreateDate);
		dos.writeUTF(destinationUserUuid);
		dos.writeInt(iFollowCollectionId);
		dos.writeUTF(targetUserUuid);
		Debug.println("add to follow operation done\n");
		return new DBQUODDYShdAddToFollow( baos.toByteArray(), iFollowCollectionCreateDate,
				destinationUserUuid, iFollowCollectionId,targetUserUuid);
		
	}
	
	protected DBQUODDYShdAddToFollow(byte[] arr) {
		super(arr);
	}
	
	protected DBQUODDYShdAddToFollow(byte[] arr, String iFollowCollectionCreateDate,
			String destinationUserUuid,	int iFollowCollectionId,String targetUserUuid) {
		super(arr);
		this.iFollowCollectionCreateDate = iFollowCollectionCreateDate;
		this.destinationUserUuid = destinationUserUuid;
		this.iFollowCollectionId = iFollowCollectionId;
		this.targetUserUuid = targetUserUuid;
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
		Debug.print("add to follow encode\n");
		dos.writeUTF(iFollowCollectionCreateDate);
		dos.writeUTF(destinationUserUuid);
		dos.writeInt(iFollowCollectionId);
		dos.writeUTF(targetUserUuid);
	}
	
	
	@Override
	public void executeShadow(IDefDatabase iDefDatabase) {
		// TODO Auto-generated method stub
		try {
			Debug.println("Shadow add to follow execution " );
			iDefDatabase.executeUpdate("update ifollow_collection set date_created='"+this.iFollowCollectionCreateDate+"',owner_uuid='"+this.destinationUserUuid+"' where id="+this.iFollowCollectionId);
			iDefDatabase.executeUpdate("insert into ifollow_collection_i_follow (ifollow_collection_id, i_follow_string) values ("+this.iFollowCollectionId+", '"+this.targetUserUuid+"')");
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow add to follow operation");
			e.printStackTrace();
		}
	}


}
