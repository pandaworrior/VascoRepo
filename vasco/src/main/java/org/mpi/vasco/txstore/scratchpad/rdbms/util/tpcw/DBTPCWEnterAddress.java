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


public class DBTPCWEnterAddress extends DBGenericOperation {
	protected int addr_id;
	protected String street1;
	protected String street2;
	protected String city;
	protected String state;
	protected String zip;
	protected int country_id;

	protected DBTPCWEnterAddress(byte[] arr) {
		super(arr);
	}

	public static DBTPCWEnterAddress createOperation(DataInputStream dis)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		int addr_id = dis.readInt();
		dos.writeInt(addr_id);
		String street1 = dis.readUTF();
		dos.writeUTF(street1);
		String street2 = dis.readUTF();
		dos.writeUTF(street2);
		String city = dis.readUTF();
		dos.writeUTF(city);
		String state = dis.readUTF();
		dos.writeUTF(state);
		String zip = dis.readUTF();
		dos.writeUTF(zip);
		int country_id = dis.readInt();
		dos.writeInt(country_id);
		return new DBTPCWEnterAddress(baos.toByteArray(), addr_id, street1, street2,
				city, state, zip, country_id);

	}

	public static DBTPCWEnterAddress createOperation(int addr_id, String street1,
			String street2, String city, String state, String zip,
			int country_id) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeInt(addr_id);
		dos.writeUTF(street1);
		dos.writeUTF(street2);
		dos.writeUTF(city);
		dos.writeUTF(state);
		dos.writeUTF(zip);
		dos.writeInt(country_id);
		return new DBTPCWEnterAddress(baos.toByteArray(), addr_id, street1, street2,
				city, state, zip, country_id);

	}

	protected DBTPCWEnterAddress(byte[] arr, int addr_id, String street1, String street2,
			String city, String state, String zip, int country_id) {
		super(arr);
		this.addr_id = addr_id;
		this.street1 = street1;
		this.street2 = street2;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.country_id = country_id;
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
			store.executeUpdate("INSERT into address (addr_id, addr_street1, addr_street2, addr_city, addr_state, addr_zip, addr_co_id) VALUES ( "+addr_id +", '"+street1+"', '"+street2+"', '"+city+"', '"+state+"', '"+zip+"', "+country_id+")");
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
			store.executeUpdate("INSERT into address (addr_id, addr_street1, addr_street2, addr_city, addr_state, addr_zip, addr_co_id) VALUES ( "+addr_id +", '"+street1+"', '"+street2+"', '"+city+"', '"+state+"', '"+zip+"', "+country_id+")");
		} catch (Exception e) {
			System.err
					.println("There was an exception when performing the shadow operation");
			Debug.kill(e);
		}
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		dos.writeInt(addr_id);
		dos.writeUTF(street1);
		dos.writeUTF(street2);
		dos.writeUTF(city);
		dos.writeUTF(state);
		dos.writeUTF(zip);
		dos.writeInt(country_id);
		
	}

}
