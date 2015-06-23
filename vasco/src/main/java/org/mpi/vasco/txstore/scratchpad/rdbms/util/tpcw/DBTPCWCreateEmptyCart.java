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



public class DBTPCWCreateEmptyCart extends DBGenericOperation{
	private int cartID;
	private Date timestamp;
	

	public static DBTPCWCreateEmptyCart createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int shopping_id = dis.readInt();
		dos.writeInt(shopping_id);
		long timestamp = dis.readLong();
		dos.writeLong(timestamp);
		return new DBTPCWCreateEmptyCart( baos.toByteArray(), shopping_id, timestamp);
		
	}
	public static DBTPCWCreateEmptyCart createOperation(int shopping_id, long timestamp) throws IOException {
		Debug.println("create empty cart operation\n");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_GENERICOP);
		dos.writeByte(OP_GEN_TPCWCREATECART);
		dos.writeInt(shopping_id);
		dos.writeLong(timestamp);
		Debug.println("create empty cart operation done\n");
		return new DBTPCWCreateEmptyCart( baos.toByteArray(), shopping_id, timestamp);
		
	}
	
	protected DBTPCWCreateEmptyCart(byte[] arr) {
		super(arr);
	}
	
	protected DBTPCWCreateEmptyCart(byte[] arr, int shopping_id, long timestamp) {
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
		try {
			Debug.println("Primary EmptyCart execution" + this.cartID);
			store.executeUpdate("INSERT into shopping_cart (sc_id, sc_time) VALUES ("+this.cartID+",'"+this.timestamp+"')");
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
			Debug.println("Shadow EmptyCart execution " + this.cartID);
			store.executeUpdate("INSERT into shopping_cart (sc_id, sc_time) VALUES ("+this.cartID+",'"+this.timestamp+"')");
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow operation");
			e.printStackTrace();
			Debug.kill(e);
		}
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		Debug.print("empty cart encode\n");
		dos.writeInt(cartID);
		dos.writeLong(timestamp.getTime());
	}

}
