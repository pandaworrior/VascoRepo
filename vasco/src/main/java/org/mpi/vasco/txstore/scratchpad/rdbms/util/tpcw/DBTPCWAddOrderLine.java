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

public class DBTPCWAddOrderLine extends DBGenericOperation {
	protected int ol_id ;
	protected int ol_o_id;
	protected int ol_i_id;
    protected int ol_qty;
    protected double ol_discount;
    protected String ol_comment;

	protected DBTPCWAddOrderLine(byte[] arr) {
		super(arr);
	}

	public static DBTPCWAddOrderLine createOperation(DataInputStream dis)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		int ol_id = dis.readInt();
		dos.writeInt(ol_id);
		int ol_o_id = dis.readInt();
		dos.writeInt(ol_o_id);
		int ol_i_id = dis.readInt();
		dos.writeInt(ol_i_id);
		int ol_qty = dis.readInt();
		dos.writeInt(ol_qty);
		double ol_discount = dis.readDouble();
		dos.writeDouble(ol_discount);
		String ol_comment = dis.readUTF();
		dos.writeUTF(ol_comment);
		return new DBTPCWAddOrderLine(baos.toByteArray(), ol_id, ol_o_id, ol_i_id, 
			    ol_qty, ol_discount, ol_comment);

	}

	public static DBTPCWAddOrderLine createOperation(int ol_id, int ol_o_id, int ol_i_id, 
		    int ol_qty, double ol_discount, String ol_comment) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeInt(ol_id);
		dos.writeInt(ol_o_id);
		dos.writeInt(ol_i_id);
		dos.writeInt(ol_qty);
		dos.writeDouble(ol_discount);
		dos.writeUTF(ol_comment);
		return new DBTPCWAddOrderLine(baos.toByteArray(), ol_id, ol_o_id, ol_i_id, 
			    ol_qty, ol_discount, ol_comment);

	}

	protected DBTPCWAddOrderLine(byte[] arr, int ol_id, int ol_o_id, int ol_i_id, 
		    int ol_qty, double ol_discount, String ol_comment) {
		super(arr);
		this.ol_id = ol_id;
		this.ol_o_id = ol_o_id;
		this.ol_i_id = ol_i_id;
		this.ol_qty = ol_qty;
		this.ol_discount = ol_discount;
		this.ol_comment = ol_comment;
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
			store.executeUpdate("INSERT into order_line (ol_id, ol_o_id, ol_i_id, ol_qty, ol_discount, ol_comments) VALUES ("+ol_id+", "+ol_o_id+", "+ol_i_id+", "+ol_qty+", "+ol_discount+", '"+ol_comment+"')");
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
			store.executeUpdate("INSERT into order_line (ol_id, ol_o_id, ol_i_id, ol_qty, ol_discount, ol_comments) VALUES ("+ol_id+", "+ol_o_id+", "+ol_i_id+", "+ol_qty+", "+ol_discount+", '"+ol_comment+"')");
		} catch (Exception e) {
			System.err
					.println("There was an exception when performing the shadow operation");
			Debug.kill(e);
		}
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		dos.writeInt(ol_id);
		dos.writeInt(ol_o_id);
		dos.writeInt(ol_i_id);
		dos.writeInt(ol_qty);
		dos.writeDouble(ol_discount);
		dos.writeUTF(ol_comment);

	}

}
