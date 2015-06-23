package org.mpi.vasco.txstore.scratchpad.rdbms.util.micro;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.util.debug.Debug;



public class DBMICROTX4 extends DBShadowOperation{
	
	String tableName;
	String primaryKey;
	String primaryValue;
	String field;
	String value;
	
	
	protected DBMICROTX4(byte[] arr) {
		super(arr);
	}
	public static DBMICROTX4 createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		String tN = dis.readUTF();
		dos.writeUTF(tN);
		String pK = dis.readUTF();
		dos.writeUTF(pK);
		String pKv = dis.readUTF();
		dos.writeUTF(pKv);
		String fD = dis.readUTF();
		dos.writeUTF(fD);
		String vL = dis.readUTF();
		dos.writeUTF(vL);

		return new DBMICROTX4( baos.toByteArray(), tN, pK, pKv, fD, vL);
		
	}
	public static DBMICROTX4 createOperation(String tN, String pK, String pKv, String fD, String vL) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_MICRO4);
		dos.writeUTF(tN);
		dos.writeUTF(pK);
		dos.writeUTF(pKv);
		dos.writeUTF(fD);
		dos.writeUTF(vL);
		return new DBMICROTX4( baos.toByteArray(), tN, pK, pKv, fD, vL);
		
	}
	protected DBMICROTX4(byte[] arr, String tN, String pK, String pKv, String fD, String vL) {
		super(arr);
		this.tableName = tN;
		this.primaryKey = pK;
		this.primaryValue = pKv;
		this.field = fD;
		this.value = vL;
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
		Debug.print("Shadow execution of micro4\n");
		try{
			//DBSelectResult res = (DBSelectResult) store.executeQuery("SELECT * FROM shopping_cart_line WHERE scl_sc_id = "+shoppingcart_id+" AND scl_i_id = "+item_id);
			store.executeUpdate("update "+tableName+" set  "+ field +" = '"+value +"' where "+primaryKey+"  = " + primaryValue);
	    }catch(Exception e){
	    	System.err.println("Exception from shadow operation of micro4");
	    	e.printStackTrace();
	    	Debug.kill(e);
	    }
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		dos.writeUTF(tableName);
		dos.writeUTF(primaryKey);
		dos.writeUTF(primaryValue);
		dos.writeUTF(field);
		dos.writeUTF(value);
		
	}
	@Override
	public int execute(IDatabase store) {
		// TODO Auto-generated method stub
		return 0;
	}
	

}

