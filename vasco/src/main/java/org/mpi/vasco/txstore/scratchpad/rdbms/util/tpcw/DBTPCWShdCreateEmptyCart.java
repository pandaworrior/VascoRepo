package org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.util.debug.Debug;

import java.sql.Date;

public class DBTPCWShdCreateEmptyCart extends DBShadowOperation{
	private int cartID;
	private Date timestamp;
	

	public static DBTPCWShdCreateEmptyCart createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int shopping_id = dis.readInt();
		dos.writeInt(shopping_id);
		long timestamp = dis.readLong();
		dos.writeLong(timestamp);
		return new DBTPCWShdCreateEmptyCart( baos.toByteArray(), shopping_id, timestamp);
		
	}
	public static DBTPCWShdCreateEmptyCart createOperation(int shopping_id, long timestamp) throws IOException {
		Debug.println("create empty cart operation\n");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_TPCW_CREATEEMPTYCART);
		dos.writeInt(shopping_id);
		dos.writeLong(timestamp);
		Debug.println("create empty cart operation done\n");
		return new DBTPCWShdCreateEmptyCart( baos.toByteArray(), shopping_id, timestamp);
		
	}
	
	protected DBTPCWShdCreateEmptyCart(byte[] arr) {
		super(arr);
	}
	
	protected DBTPCWShdCreateEmptyCart(byte[] arr, int shopping_id, long timestamp) {
		super(arr);
		this.cartID = shopping_id;
		this.timestamp = new Date(timestamp);
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
		Debug.print("empty cart encode\n");
		dos.writeInt(cartID);
		dos.writeLong(timestamp.getTime());
	}
	
	
	@Override
	public void executeShadow(IDefDatabase iDefDatabase) {
		// TODO Auto-generated method stub
		try {
			Debug.println("Shadow EmptyCart execution " + this.cartID);
			String insertQuery = "INSERT into shopping_cart (sc_id, sc_time) VALUES ("+this.cartID+",'"+this.timestamp+"')";
			iDefDatabase.executeUpdate(insertQuery);
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow operation");
			e.printStackTrace();
		}
	}

}
