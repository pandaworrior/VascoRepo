package org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Date;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.util.debug.Debug;

public class DBTPCWShdRefreshSession extends DBShadowOperation{
	
	private int customer_id;
	private Date loginTs;
	private Date expireTs;
	
	public static DBTPCWShdRefreshSession createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int customer_id = dis.readInt();
		dos.writeInt(customer_id);
		long loginTs = dis.readLong();
		dos.writeLong(loginTs);
		long expireTs = dis.readLong();
		dos.writeLong(expireTs);
		return new DBTPCWShdRefreshSession( baos.toByteArray(), customer_id, loginTs, expireTs);
		
	}
	public static DBTPCWShdRefreshSession createOperation(int customer_id, long loginTs, long expireTs) throws IOException {
		Debug.println("create refreshsession cart operation\n");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_TPCW_REFRESHSESSION);
		dos.writeInt(customer_id);
		dos.writeLong(loginTs);
		dos.writeLong(expireTs);
		Debug.println("create refreshsession cart operation done\n");
		return new DBTPCWShdRefreshSession( baos.toByteArray(), customer_id, loginTs, expireTs);
		
	}

	public DBTPCWShdRefreshSession(byte[] arr) {
		super(arr);
	}
	
	protected DBTPCWShdRefreshSession(byte[] arr, int customer_id, long loginTs, long expireTs) {
		super(arr);
		this.customer_id = customer_id;
		this.loginTs = new Date(loginTs);
		this.expireTs = new Date(expireTs);
	}
	

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		// TODO Auto-generated method stub
		Debug.print("refresh session encode\n");
		dos.writeInt(customer_id);
		dos.writeLong(loginTs.getTime());
		dos.writeLong(expireTs.getTime());
	}

	@Override
	public int execute(IDatabase store) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void executeShadow(IDefDatabase iDefDatabase) {
		// TODO Auto-generated method stub
		try {
			Debug.println("Shadow refresh session execution " + this.customer_id);
			iDefDatabase.executeUpdate("UPDATE customer SET c_login = '"+this.loginTs+"', c_expiration = '"+this.expireTs+"' WHERE c_id = "+this.customer_id);
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow operation");
			e.printStackTrace();
		}
	}

	@Override
	public boolean isQuery() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean registerIndividualOperations() {
		// TODO Auto-generated method stub
		return false;
	}

}
