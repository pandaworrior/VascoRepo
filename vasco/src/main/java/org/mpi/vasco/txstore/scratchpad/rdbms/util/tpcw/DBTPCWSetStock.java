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



public class DBTPCWSetStock extends DBGenericOperation{
	protected int i_id;
	protected int new_stock;
	
	protected DBTPCWSetStock(byte[] arr) {
		super(arr);
	}
	public static DBTPCWSetStock createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int i_id = dis.readInt();
		dos.writeInt(i_id);
		int new_stock = dis.readInt();
		dos.writeInt(new_stock);
		return new DBTPCWSetStock( baos.toByteArray(), i_id, new_stock);
		
	}
	public static DBTPCWSetStock createOperation(int i_id, int new_stock) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeInt(i_id);
		dos.writeLong(new_stock);
		return new DBTPCWSetStock( baos.toByteArray(), i_id, new_stock);
		
	}
	protected DBTPCWSetStock(byte[] arr, int i_id, int new_stock) {
		super(arr);
		this.i_id = i_id;
		this.new_stock = new_stock;
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
			store.executeUpdate("UPDATE item SET i_stock = "+new_stock+" WHERE i_id = "+i_id);
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
			store.executeUpdate("UPDATE item SET i_stock = "+new_stock+" WHERE i_id = "+i_id);
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow operation");
			Debug.kill(e);
		}
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		dos.writeInt(i_id);
		dos.writeLong(new_stock);
	}

}
