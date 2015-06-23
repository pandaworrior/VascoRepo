package org.mpi.vasco.txstore.scratchpad.rdbms.tests;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mpi.vasco.util.debug.Debug;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IPrimaryExec;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.*;
import org.mpi.vasco.txstore.util.Result;

public class DBTestCountOperation
	extends DBGenericOperation
{

	private int a;
	private int c;
	private int d;
	private String e;
	
	public static DBTestCountOperation createOperation(	DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int a = dis.readInt();
		dos.writeInt(a);
		int c = dis.readInt();
		dos.writeInt(c);
		int d = dis.readInt();
		dos.writeInt(d);
		String e = dis.readUTF();
		dos.writeUTF(e);
		return new DBTestCountOperation( baos.toByteArray(), a, c, d, e);
		
	}
	public static DBTestCountOperation createOperation(	int a, int c, int d, String e) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeInt(a);
		dos.writeInt(c);
		dos.writeInt(d);
		dos.writeUTF(e);
		return new DBTestCountOperation( baos.toByteArray(), a, c, d, e);
		
	}

	
	protected DBTestCountOperation(byte[] arr) {
		super(arr);
	}
	protected DBTestCountOperation(byte[] arr, int a, int c, int d, String e) {
		super(arr);
		this.a = a;
		this.c = c;
		this.d = d;
		this.e = e;
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		dos.writeInt(a);
		dos.writeInt(c);
		dos.writeInt(d);
		dos.writeUTF(e);
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
			DBSelectResult res = store.executeQuery("select count(*) from t1;");
			res.next();
			Debug.println( "PRIM: COUNT = " + res.getInt(1));
			int r = res.getInt(1);
			store.executeUpdate("insert into t1 values ( "+ a + "," + r + "," + c + "," + d + ",'" + e + "');");
			return 1;
		} catch( Exception e) {
			return 0;
		}
	}

	@Override
	public void executeShadow(IDefDatabase store, IPrimaryExec exec) {
		try {
			DBSelectResult res = (DBSelectResult)exec.getResult(0);
			res.reset();
			res.next();
			int r = res.getInt(1);
			store.executeUpdate("insert into t1 values ( "+ a + "," + (r*2) + "," + c + "," + d + ",'" + e + "');");
			Debug.println( "PRIM: SHADOW = " + res.getInt(1));	
		} catch( Exception e) {
			Debug.kill(e);
		}
	}


}
