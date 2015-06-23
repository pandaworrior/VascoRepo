package org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IPrimaryExec;
import org.mpi.vasco.txstore.scratchpad.rdbms.tests.DBTestCountOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBGenericOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSelectResult;
import org.mpi.vasco.util.debug.Debug;

import java.sql.Date;



public class DBTPCWAdminUpdate extends DBGenericOperation{
	int i_id ;
	double cost;
	String image;
	String thumbnail;
	long pubdate;
	
	protected DBTPCWAdminUpdate(byte[] arr) {
		super(arr);
	}
	public static DBTPCWAdminUpdate createOperation(DataInputStream dis) throws IOException {
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
		return new DBTPCWAdminUpdate( baos.toByteArray(), i_id ,cost,  image, thumbnail, pubdate);
		
	}
	public static DBTPCWAdminUpdate createOperation(	int i_id ,
	double cost, String image, String thumbnail, long pubdate) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeInt(i_id);
		dos.writeDouble(cost);
		dos.writeUTF(image);
		dos.writeUTF(thumbnail);
		dos.writeLong(pubdate);
		return new DBTPCWAdminUpdate( baos.toByteArray(), i_id ,cost,  image, thumbnail, pubdate);
		
	}
	protected DBTPCWAdminUpdate(byte[] arr,int i_id ,
			double cost, String image, String thumbnail, long pubdate) {
		super(arr);
		this.i_id = i_id;
		this.cost = cost;
		this.image = image;
		this.thumbnail = thumbnail;
		this.pubdate = pubdate;
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
		try {
			Debug.print("Primary execution");
			store.executeUpdate("UPDATE item SET i_cost = "+this.cost+", i_image = "+this.image+", i_thumbnail = "+this.thumbnail+", i_pub_date = "+ new java.sql.Date(this.pubdate) +" WHERE i_id = "+this.i_id);
			return 1;
		} catch( Exception e) {
			System.err.println("There was an exception in the primary datacenter!!!");
			e.printStackTrace();
			return 0;
		}
		
		
	}

	@Override
	public void executeShadow(IDefDatabase store, IPrimaryExec exec) {
		try {
			Debug.print("Shadow execution");
			store.executeUpdate("UPDATE item SET i_cost = "+this.cost+", i_image = "+this.image+", i_thumbnail = "+this.thumbnail+", i_pub_date = "+ new java.sql.Date(this.pubdate) +" WHERE i_id = "+this.i_id);
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow operation");
			Debug.kill(e);
		}
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		dos.writeInt(i_id);
		dos.writeDouble(cost);
		dos.writeUTF(image);
		dos.writeUTF(thumbnail);
		dos.writeLong(pubdate);
	}

}
