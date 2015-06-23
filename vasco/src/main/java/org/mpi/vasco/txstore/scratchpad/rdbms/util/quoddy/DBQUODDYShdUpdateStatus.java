package org.mpi.vasco.txstore.scratchpad.rdbms.util.quoddy;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.util.debug.Debug;

public class DBQUODDYShdUpdateStatus extends DBShadowOperation{
	private int newStatusId;
	private int userId;
	private String now;
	private String statusText;
	private int activityId;
	private String activityEffectiveDate;
	private String activityTitle;
	private int activityOwnerId;
	private String activityTargetUuid;
	private String activityContent;
	private String activityUrl;
	private String activityVerb;
	

	public static DBQUODDYShdUpdateStatus createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int newStatusId = dis.readInt();
		dos.writeInt(newStatusId);
		int userId = dis.readInt();
		dos.writeInt(userId);
		String now = dis.readUTF();
		dos.writeUTF(now);
		String statusText = dis.readUTF();
		dos.writeUTF(statusText);
		int activityId = dis.readInt();
		dos.writeInt(activityId);
		String activityEffectiveDate = dis.readUTF();
		dos.writeUTF(activityEffectiveDate);
		String activityTitle = dis.readUTF();
		dos.writeUTF(activityTitle);
		int activityOwnerId = dis.readInt();
		dos.writeInt(activityOwnerId);
		String activityTargetUuid = dis.readUTF();
		dos.writeUTF(activityTargetUuid);
		String activityContent = dis.readUTF();
		dos.writeUTF(activityContent);
		String activityUrl = dis.readUTF();
		dos.writeUTF(activityUrl);
		String activityVerb = dis.readUTF();
		dos.writeUTF(activityVerb);
		return new DBQUODDYShdUpdateStatus( baos.toByteArray(), newStatusId,userId,now,statusText,
				activityId,activityEffectiveDate,activityTitle,activityOwnerId,activityTargetUuid,
				activityContent,activityUrl,activityVerb);
		
	}
	public static DBQUODDYShdUpdateStatus createOperation(int newStatusId,int userId,String now,String statusText,
	int activityId,String activityEffectiveDate,String activityTitle,int activityOwnerId,String activityTargetUuid,
	String activityContent,String activityUrl,String activityVerb) throws IOException {
		Debug.println("update status operation\n");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_QUODDY_UPDATESTATUS);
		dos.writeInt(newStatusId);
		dos.writeInt(userId);
		dos.writeUTF(now);
		dos.writeUTF(statusText);
		dos.writeInt(activityId);
		dos.writeUTF(activityEffectiveDate);
		dos.writeUTF(activityTitle);
		dos.writeInt(activityOwnerId);
		dos.writeUTF(activityTargetUuid);
		dos.writeUTF(activityContent);
		dos.writeUTF(activityUrl);
		dos.writeUTF(activityVerb);
		Debug.println("update status operation done\n");
		return new DBQUODDYShdUpdateStatus( baos.toByteArray(),  newStatusId,userId,now,statusText,
				activityId,activityEffectiveDate,activityTitle,activityOwnerId,activityTargetUuid,
				activityContent,activityUrl,activityVerb);
		
	}
	
	protected DBQUODDYShdUpdateStatus(byte[] arr) {
		super(arr);
	}
	
	protected DBQUODDYShdUpdateStatus(byte[] arr, int newStatusId,int userId,String now,String statusText,
			int activityId,String activityEffectiveDate,String activityTitle,int activityOwnerId,String activityTargetUuid,
			String activityContent,String activityUrl,String activityVerb) {
		super(arr);
		this.newStatusId = newStatusId;
		this.userId = userId;
		this.now = now;
		this.statusText = statusText;
		this.activityId = activityId;
		this.activityEffectiveDate = activityEffectiveDate;
		this.activityTitle = activityTitle;
		this.activityOwnerId = activityOwnerId;
		this.activityTargetUuid = activityTargetUuid;
		this.activityContent = activityContent;
		this.activityUrl = activityUrl;
		this.activityVerb = activityVerb;
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
		Debug.print("update status encode\n");
		dos.writeInt(newStatusId);
		dos.writeInt(userId);
		dos.writeUTF(now);
		dos.writeUTF(statusText);
		dos.writeInt(activityId);
		dos.writeUTF(activityEffectiveDate);
		dos.writeUTF(activityTitle);
		dos.writeInt(activityOwnerId);
		dos.writeUTF(activityTargetUuid);
		dos.writeUTF(activityContent);
		dos.writeUTF(activityUrl);
		dos.writeUTF(activityVerb);
	}
	
	
	@Override
	public void executeShadow(IDefDatabase iDefDatabase) {
		// TODO Auto-generated method stub
		try {
			Debug.println("Shadow update status execution " );
			String sql = "insert into status_update (id,creator_id, date_created, text)	values	("
					+this.newStatusId+", "+this.userId+", '"+this.now+"', '"+this.statusText+"')";
			iDefDatabase.executeUpdate(sql);
			
			sql = "insert into	event_base (id, date_created, effective_date, name, owner_id, target_uuid) 	" +
					"values	("+this.activityId+", '"+this.now+"', '"+this.activityEffectiveDate+"', '"+this.activityTitle+"', "+this.activityOwnerId+", '"+this.activityTargetUuid+"')";
			iDefDatabase.executeUpdate(sql);
			
			sql = "insert into	activity " +
					"(actor_content, actor_display_name, actor_image_height, actor_image_url, actor_image_width, actor_object_type, actor_url, actor_uuid, content, generator_url, icon, object_content, object_display_name, object_image_height, object_image_url, object_image_width, object_object_type, object_url, object_uuid, provider_url, published, target_content, target_display_name, target_image_height, target_image_url, target_image_width, target_object_type, target_url, title, updated, url, uuid, verb, id) " +
				    " values (NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '"+this.activityContent+"', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '"+this.now+"', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '"+this.activityTitle+"', NULL, '"+this.activityUrl+"', '"+this.activityTargetUuid+"', '"+this.activityVerb+"', '"+this.activityId+"')";
			iDefDatabase.executeUpdate(sql);
			sql = "update uzer set current_status_id="+this.newStatusId+", date_created='"+this.now+"' where id="+this.userId;
			iDefDatabase.executeUpdate(sql);
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow update status operation");
			e.printStackTrace();
		}
	}

}
