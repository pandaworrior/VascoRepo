package org.mpi.vasco.txstore.scratchpad.rdbms.util.rubis;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;



public class DBRUBISShdEmpty extends DBShadowOperation{
	
	protected DBRUBISShdEmpty(byte[] arr) {
		super(arr);
	}
	public static DBRUBISShdEmpty createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		return new DBRUBISShdEmpty( baos.toByteArray());
		
	}
	public static DBRUBISShdEmpty createOperation() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_RUBIS_NONE);
		return new DBRUBISShdEmpty( baos.toByteArray());
		
	}
	protected DBRUBISShdEmpty(byte[] arr, String tN, String pK, String pKv, String fD, String vL) {
		super(arr);
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
	public void executeShadow(IDefDatabase store) {
		// TODO Auto-generated method stub
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {		
	}
	@Override
	public int execute(IDatabase store) {
		// TODO Auto-generated method stub
		return 0;
	}
	

}
