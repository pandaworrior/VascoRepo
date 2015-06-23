package org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Date;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWAdminUpdateRelated;
import org.mpi.vasco.util.debug.Debug;

public class DBTPCWShdAdminUpdate extends DBShadowOperation{
	int i_id ;
	double cost;
	String image;
	String thumbnail;
	Date pubdate;
	int related_item1;
	int related_item2;
	int related_item3;
	int related_item4;
	int related_item5;
	
	
	public static DBTPCWShdAdminUpdate createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int i_id = dis.readInt();
		dos.writeInt(i_id);
		double cost = dis.readDouble();
		dos.writeDouble(cost);
		String image = dis.readUTF();
		dos.writeUTF(image);
		String thumbnail = dis.readUTF();
		dos.writeUTF(thumbnail);
		long pubdate = dis.readLong();
		dos.writeLong(pubdate);
		int related_item1 = dis.readInt();
		dos.writeInt(related_item1);
		int related_item2 = dis.readInt();
		dos.writeInt(related_item2);
		int related_item3 = dis.readInt();
		dos.writeInt(related_item3);
		int related_item4 = dis.readInt();
		dos.writeInt(related_item4);
		int related_item5 = dis.readInt();
		dos.writeInt(related_item5);
		return new DBTPCWShdAdminUpdate( baos.toByteArray(), i_id , cost, image, thumbnail, pubdate,
				related_item1,
		 related_item2, 
		 related_item3,
		 related_item4,
		 related_item5);
		
	}
	public static DBTPCWShdAdminUpdate createOperation(	int i_id , double cost, String image,
			String thumbnail, long pubdate,
			int related_item1,
			int related_item2,
			int related_item3,
			int related_item4,
			int related_item5) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_TPCW_ADMINUPDATE);
		dos.writeInt(i_id);
		dos.writeDouble(cost);
		dos.writeUTF(image);
		dos.writeUTF(thumbnail);
		dos.writeLong(pubdate);
		dos.writeInt(related_item1);
		dos.writeInt(related_item2);
		dos.writeInt(related_item3);
		dos.writeInt(related_item4);
		dos.writeInt(related_item5);
		return new DBTPCWShdAdminUpdate( baos.toByteArray(), i_id ,cost, image, thumbnail, pubdate,related_item1,
		 related_item2, 
		 related_item3,
		 related_item4,
		 related_item5);
		
	}
	protected DBTPCWShdAdminUpdate(byte[] arr,int i_id ,double cost, String image,
			String thumbnail, long pubdate,
			int related_item1,
			int related_item2,
			int related_item3,
			int related_item4,
			int related_item5) {
		super(arr);
		this.i_id = i_id;
		this.cost = cost;
		this.image = image;
		this.thumbnail = thumbnail;
		this.pubdate = new Date(pubdate);
		this.related_item1 = related_item1;
	this.related_item2 = related_item2;
	this.related_item3 = related_item3;
	this.related_item4 = related_item4;
	this.related_item5 = related_item5;
	}

	public DBTPCWShdAdminUpdate(byte[] arr) {
		super(arr);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		// TODO Auto-generated method stub
		dos.writeInt(i_id);
		dos.writeDouble(cost);
		dos.writeUTF(image);
		dos.writeUTF(thumbnail);
		dos.writeLong(pubdate.getTime());
		dos.writeInt(related_item1);
		dos.writeInt(related_item2);
		dos.writeInt(related_item3);
		dos.writeInt(related_item4);
		dos.writeInt(related_item5);
	}

	@Override
	public int execute(IDatabase store) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void executeShadow(IDefDatabase iDefDatabase) {
		// TODO Auto-generated method stub
		try {
			Debug.print("Shadow adminupdate2 execution");
			String updateQuery = "UPDATE item SET i_cost = "+this.cost+", i_image = '"+this.image+"', i_thumbnail = '"+this.thumbnail+"', i_pub_date = '"+ this.pubdate +"', i_related1 = "+related_item1+", i_related2 = "+related_item2+", i_related3 = "+related_item3+", i_related4 = "+related_item4+", i_related5 = "+related_item5;
			updateQuery += " WHERE i_id = "+this.i_id;
			iDefDatabase.executeUpdate(updateQuery);
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow operation");
		}
	}

	@Override
	public boolean isQuery() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean registerIndividualOperations() {
		// TODO Auto-generated method stub
		return false;
	}

}
