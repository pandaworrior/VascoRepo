package org.mpi.vasco.txstore.scratchpad.rdbms.util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.util.debug.Debug;



public class DBSifterEmptyShd extends DBShadowOperation{
	
	protected DBSifterEmptyShd(byte[] arr) {
		super(arr);
	}
	public static DBSifterEmptyShd createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		return new DBSifterEmptyShd( baos.toByteArray());
		
	}
	public static DBSifterEmptyShd createOperation() throws IOException {
		Debug.println("Create a sifter empty shadow operation");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_NONE);
		return new DBSifterEmptyShd( baos.toByteArray());
		
	}
	protected DBSifterEmptyShd(byte[] arr, String tN, String pK, String pKv, String fD, String vL) {
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

