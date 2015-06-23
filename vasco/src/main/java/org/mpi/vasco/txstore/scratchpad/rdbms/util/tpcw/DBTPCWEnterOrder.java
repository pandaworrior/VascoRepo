package org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IPrimaryExec;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBGenericOperation;
import org.mpi.vasco.util.debug.Debug;

public class DBTPCWEnterOrder extends DBGenericOperation {
	protected int o_id;
	protected int o_c_id;
	protected long o_date;
	protected double o_sub_total;
	protected double o_total;
	protected String o_ship_type;
	protected long o_ship_date;
	protected int o_bill_addr_id;
	protected int o_ship_addr_id;

	protected DBTPCWEnterOrder(byte[] arr) {
		super(arr);
	}

	public static DBTPCWEnterOrder createOperation(DataInputStream dis)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		int o_id = dis.readInt();
		dos.writeInt(o_id);
		int customer_id = dis.readInt();
		dos.writeInt(customer_id);
		long o_date = dis.readLong();
		dos.writeLong(o_date);
		double o_sub_total = dis.readDouble();
		dos.writeDouble(o_sub_total);
		double o_total = dis.readDouble();
		dos.writeDouble(o_total);
		String o_ship_type = dis.readUTF();
		dos.writeUTF(o_ship_type);
		long o_ship_date = dis.readLong();
		dos.writeLong(o_ship_date);
		int o_bill_addr_id = dis.readInt();
		dos.writeLong(o_bill_addr_id);
		int o_ship_addr_id = dis.readInt();
		dos.writeInt(o_ship_addr_id);
		return new DBTPCWEnterOrder(baos.toByteArray(), o_id, customer_id,
				o_date, o_sub_total, o_total, o_ship_type, o_ship_date,
				o_bill_addr_id, o_ship_addr_id);

	}

	public static DBTPCWEnterOrder createOperation(int o_id, int customer_id,
			long o_date, double o_sub_total, double o_total,
			String o_ship_type, long o_ship_date, int o_bill_addr_id,
			int o_ship_addr_id) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeInt(o_id);
		dos.writeInt(customer_id);
		dos.writeLong(o_date);
		dos.writeDouble(o_sub_total);
		dos.writeDouble(o_total);
		dos.writeUTF(o_ship_type);
		dos.writeLong(o_ship_date);
		dos.writeLong(o_bill_addr_id);
		dos.writeInt(o_ship_addr_id);
		return new DBTPCWEnterOrder(baos.toByteArray(), o_id, customer_id,
				o_date, o_sub_total, o_total, o_ship_type, o_ship_date,
				o_bill_addr_id, o_ship_addr_id);

	}

	protected DBTPCWEnterOrder(byte[] arr, int o_id, int customer_id,
			long o_date, double o_sub_total, double o_total,
			String o_ship_type, long o_ship_date, int o_bill_addr_id,
			int o_ship_addr_id) {
		super(arr);
		this.o_c_id = customer_id;
		this.o_date = o_date;
		this.o_sub_total = o_sub_total;
		this.o_ship_type = o_ship_type;
		this.o_ship_date = o_ship_date;
		this.o_bill_addr_id = o_bill_addr_id;
		this.o_ship_addr_id = o_ship_addr_id;
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
			store.executeUpdate("INSERT into orders (o_id, o_c_id, o_date, o_sub_total, o_tax, o_total, o_ship_type, o_ship_date, o_bill_addr_id, o_ship_addr_id, o_status) VALUES ("
					+ o_id + ", " + o_c_id + ", "+new java.sql.Date(o_date)+", "+o_sub_total+", 8.25, "+o_total+", '"+o_ship_type+"', "+new java.sql.Date(o_ship_date)+", "+o_bill_addr_id+", "+o_ship_addr_id+", 'pending')");
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
			store.executeUpdate("INSERT into orders (o_id, o_c_id, o_date, o_sub_total, o_tax, o_total, o_ship_type, o_ship_date, o_bill_addr_id, o_ship_addr_id, o_status) VALUES ("
					+ o_id + ", " + o_c_id + ", "+new java.sql.Date(o_date)+", "+o_sub_total+", 8.25, "+o_total+", '"+o_ship_type+"', "+new java.sql.Date(o_ship_date)+", "+o_bill_addr_id+", "+o_ship_addr_id+", 'pending')");
		} catch (Exception e) {
			System.err
					.println("There was an exception when performing the shadow operation");
			Debug.kill(e);
		}
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		dos.writeInt(o_id);
		dos.writeInt(o_c_id);
		dos.writeLong(o_date);
		dos.writeDouble(o_sub_total);
		dos.writeDouble(o_total);
		dos.writeUTF(o_ship_type);
		dos.writeLong(o_ship_date);
		dos.writeLong(o_bill_addr_id);
		dos.writeInt(o_ship_addr_id);

	}

}
