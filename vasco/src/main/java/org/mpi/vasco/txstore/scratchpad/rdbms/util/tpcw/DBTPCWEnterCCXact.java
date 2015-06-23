package org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Date;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IPrimaryExec;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBGenericOperation;
import org.mpi.vasco.util.debug.Debug;


public class DBTPCWEnterCCXact extends DBGenericOperation {
	protected int o_id;    
	protected  String cc_type;
	protected   long cc_number;
	protected  String cc_name;
	protected  long cc_expiry;
	protected  double total;   
	protected long cc_xact_date;
	protected  int ship_addr_id;

	protected DBTPCWEnterCCXact(byte[] arr) {
		super(arr);
	}

	public static DBTPCWEnterCCXact createOperation(DataInputStream dis)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		int o_id = dis.readInt();
		dos.writeInt(o_id);
		String cc_type = dis.readUTF();
		dos.writeUTF(cc_type);
		long cc_number = dis.readLong();
		dos.writeLong(cc_number);
		String cc_name = dis.readUTF();
		dos.writeUTF(cc_name);
		long cc_expiry = dis.readLong();
		dos.writeLong(cc_expiry);
		double total = dis.readDouble();
		dos.writeDouble(total);
		long cc_xact_date = dis.readLong();
		dos.writeLong(cc_xact_date);
		int ship_addr_id = dis.readInt();
		dos.writeInt(ship_addr_id);
		return new DBTPCWEnterCCXact(baos.toByteArray(), o_id,cc_type,cc_number, cc_name,
				   cc_expiry, total, cc_xact_date, ship_addr_id);

	}

	public static DBTPCWEnterCCXact createOperation(int o_id,        // Order id
			   String cc_type,
			   long cc_number,
			   String cc_name,
			   long cc_expiry,
			   double total,   // Total from shopping cart
			   long cc_xact_date,
			   int ship_addr_id) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeInt(o_id);
		dos.writeUTF(cc_type);
		dos.writeLong(cc_number);
		dos.writeUTF(cc_name);
		dos.writeLong(cc_expiry);
		dos.writeDouble(total);
		dos.writeLong(cc_xact_date);
		dos.writeInt(ship_addr_id);
		return new DBTPCWEnterCCXact(baos.toByteArray(), o_id,cc_type,cc_number, cc_name,
				   cc_expiry, total, cc_xact_date, ship_addr_id);

	}

	protected DBTPCWEnterCCXact(byte[] arr, int o_id,        // Order id
			   String cc_type,
			   long cc_number,
			   String cc_name,
			   long cc_expiry,
			   double total,   // Total from shopping cart
			   long cc_xact_date,
			   int ship_addr_id) {
		super(arr);
		this.o_id = o_id;
		this.cc_type = cc_type;
		this.cc_number = cc_number;
		this.cc_name = cc_name;
		this.cc_expiry = cc_expiry;
		this.total = total;
		this.cc_xact_date = cc_xact_date;
		this.ship_addr_id = ship_addr_id;
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
			store.executeUpdate("INSERT into cc_xacts (cx_o_id, cx_type, cx_num, cx_name, cx_expire, cx_xact_amt, cx_xact_date, cx_co_id) VALUES ("+o_id+", '"+cc_type+"', "+cc_number+", "+cc_name+", "+new java.sql.Date(cc_expiry)+", "+total+", "+new java.sql.Date(cc_xact_date)+","+ship_addr_id+", (SELECT co_id FROM address, country WHERE addr_id = "+ship_addr_id+" AND addr_co_id = co_id))");
			return 1;
		} catch (Exception e) {
			System.err
					.println("There was an exception in the primary datacenter!!!");
			e.printStackTrace();
			return 0;
		}

	}

	@Override
	public void executeShadow(IDefDatabase store, IPrimaryExec exec) {
		try {
			Debug.print("Shadow execution");
			store.executeUpdate("INSERT into cc_xacts (cx_o_id, cx_type, cx_num, cx_name, cx_expire, cx_xact_amt, cx_xact_date, cx_co_id) VALUES ("+o_id+", '"+cc_type+"', "+cc_number+", "+cc_name+", "+new java.sql.Date(cc_expiry)+", "+total+", "+new java.sql.Date(cc_xact_date)+","+ship_addr_id+", (SELECT co_id FROM address, country WHERE addr_id = "+ship_addr_id+" AND addr_co_id = co_id))");
		} catch (Exception e) {
			System.err
					.println("There was an exception when performing the shadow operation");
			Debug.kill(e);
		}
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		dos.writeInt(o_id);
		dos.writeUTF(cc_type);
		dos.writeLong(cc_number);
		dos.writeUTF(cc_name);
		dos.writeLong(cc_expiry);
		dos.writeDouble(total);
		dos.writeLong(cc_xact_date);
		dos.writeInt(ship_addr_id);
		
	}

}
