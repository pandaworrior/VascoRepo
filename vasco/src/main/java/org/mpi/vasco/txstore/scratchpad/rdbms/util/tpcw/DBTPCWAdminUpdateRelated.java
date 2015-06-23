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



public class DBTPCWAdminUpdateRelated extends DBGenericOperation{
	int i_id ;
	int related_item1;
	int related_item2;
	int related_item3;
	int related_item4;
	int related_item5;
	
	protected DBTPCWAdminUpdateRelated(byte[] arr) {
		super(arr);
	}
	public static DBTPCWAdminUpdateRelated createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int i_id = dis.readInt();
		dos.writeInt(i_id);
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
		return new DBTPCWAdminUpdateRelated( baos.toByteArray(), i_id ,	 related_item1,
		 related_item2, 
		 related_item3,
		 related_item4,
		 related_item5);
		
	}
	public static DBTPCWAdminUpdateRelated createOperation(	int i_id ,
			int related_item1,
			int related_item2,
			int related_item3,
			int related_item4,
			int related_item5) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeInt(i_id);
		dos.writeInt(related_item1);
		dos.writeInt(related_item2);
		dos.writeInt(related_item3);
		dos.writeInt(related_item4);
		dos.writeInt(related_item5);
		return new DBTPCWAdminUpdateRelated( baos.toByteArray(), i_id ,related_item1,
		 related_item2, 
		 related_item3,
		 related_item4,
		 related_item5);
		
	}
	protected DBTPCWAdminUpdateRelated(byte[] arr,int i_id ,
			int related_item1,
			int related_item2,
			int related_item3,
			int related_item4,
			int related_item5) {
		super(arr);
		this.i_id = i_id;
		this.related_item1 = related_item1;
	this.related_item2 = related_item2;
	this.related_item3 = related_item3;
	this.related_item4 = related_item4;
	this.related_item5 = related_item5;
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
			store.executeUpdate("UPDATE item SET i_related1 = "+related_item1+", i_related2 = "+related_item2+", i_related3 = "+related_item3+", i_related4 = "+related_item4+", i_related5 = "+related_item5+" WHERE i_id = " + i_id);
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
			store.executeUpdate("UPDATE item SET i_related1 = "+related_item1+", i_related2 = "+related_item2+", i_related3 = "+related_item3+", i_related4 = "+related_item4+", i_related5 = "+related_item5+" WHERE i_id = " + i_id);
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow operation");
			Debug.kill(e);
		}
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		dos.writeInt(i_id);
		dos.writeInt(related_item1);
		dos.writeInt(related_item2);
		dos.writeInt(related_item3);
		dos.writeInt(related_item4);
		dos.writeInt(related_item5);
	}

}
