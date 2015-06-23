package org.mpi.vasco.txstore.scratchpad.rdbms.util.micro;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IPrimaryExec;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSelectResult;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.util.debug.Debug;

import java.sql.Date;
import java.sql.ResultSet;



public class DBMICROTX2 extends DBShadowOperation{
    int a;
    int b;
    int c;
    int d;
	
	
	protected DBMICROTX2(byte[] arr) {
		super(arr);
	}
	public static DBMICROTX2 createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int a = dis.readInt();
		dos.writeInt(a);
		int b = dis.readInt();
		dos.writeInt(b);
		int c = dis.readInt();
		dos.writeInt(c);
		int d = dis.readInt();
		dos.writeInt(d);

		return new DBMICROTX2( baos.toByteArray(), a, b,c,d);
		
	}
	public static DBMICROTX2 createOperation(int a, int b, int c, int d) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_MICRO2);
		dos.writeInt(a);
		dos.writeInt(b);
		dos.writeInt(c);
		dos.writeInt(d);
		return new DBMICROTX2( baos.toByteArray(), a, b,c,d);
		
	}
	protected DBMICROTX2(byte[] arr, int a, int b, int c, int d) {
		super(arr);
		this.a = a;
		this.b = b;
		this.c=c;
		this.d = d;
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
		Debug.print("Shadow execution of micro2\n");
		try{
			//DBSelectResult res = (DBSelectResult) store.executeQuery("SELECT * FROM shopping_cart_line WHERE scl_sc_id = "+shoppingcart_id+" AND scl_i_id = "+item_id);
			store.executeUpdate("update t9 set b = "+b +", c = " +c+", d= "+d+" where a = " + a);
	    }catch(Exception e){
	    	System.err.println("Exception from shadow operation of micro2");
	    	e.printStackTrace();
	    	Debug.kill(e);
	    }
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		dos.writeInt(a);
		dos.writeInt(b);
		dos.writeInt(c);
		dos.writeInt(d);
		
	}
	@Override
	public int execute(IDatabase store) {
		// TODO Auto-generated method stub
		return 0;
	}
	

}

