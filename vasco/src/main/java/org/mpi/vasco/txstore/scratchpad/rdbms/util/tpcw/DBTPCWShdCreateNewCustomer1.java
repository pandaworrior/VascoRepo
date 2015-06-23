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

public class DBTPCWShdCreateNewCustomer1 extends DBShadowOperation {

	int c_id;
	String c_uname;
	String c_passwd;
	double c_discount;
	double c_balance = 0.0;
	double c_ytd_pmt = 0.0;
    String c_fname;
    String c_lname;
    String c_phone;
    String c_email;
	Date c_last_visit;
	Date c_since;
	Date c_login;
	Date c_expiration;
	Date c_birthdate;
	String c_data;

	int addr_id;
	String addr_street1;
	String addr_street2;
	String addr_city;
	String addr_state;
	String addr_zip;
	int country_id;

	public static DBTPCWShdCreateNewCustomer1 createOperation(DataInputStream dis)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		int c_id = dis.readInt();
		dos.writeInt(c_id);
		String c_uname = dis.readUTF();
		dos.writeUTF(c_uname);
		String c_passwd = dis.readUTF();
		dos.writeUTF(c_passwd);
		double c_discount = dis.readDouble();
		dos.writeDouble(c_discount);
	    String c_fname = dis.readUTF();
	    dos.writeUTF(c_fname);
	    String c_lname = dis.readUTF();
	    dos.writeUTF(c_lname);
	    String c_phone = dis.readUTF();
	    dos.writeUTF(c_phone);
	    String c_email = dis.readUTF();
	    dos.writeUTF(c_email);
		long c_last_visit = dis.readLong();
		dos.writeLong(c_last_visit);
		long c_since = dis.readLong();
		dos.writeLong(c_since);
		long c_login = dis.readLong();
		dos.writeLong(c_login);
		long c_expiration = dis.readLong();
		dos.writeLong(c_expiration);
		long c_birthdate = dis.readLong();
		dos.writeLong(c_birthdate);
		String c_data = dis.readUTF();
		dos.writeUTF(c_data);
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
		return new DBTPCWShdCreateNewCustomer1(baos.toByteArray(), 	c_id,
		c_uname, c_passwd, c_discount, c_fname, c_lname, c_phone, c_email, c_last_visit, c_since, c_login, c_expiration,
		c_birthdate, c_data,addr_id, street1,
				street2, city, state, zip, country_id);

	}

	public static DBTPCWShdCreateNewCustomer1 createOperation(	int c_id,
	String c_uname, String c_passwd, double c_discount,	    String c_fname, String c_lname,   String c_phone,   String c_email, long c_last_visit, long c_since,
	long c_login, long c_expiration, long c_birthdate, String c_data, int addr_id,
			String street1, String street2, String city, String state,
			String zip, int country_id) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_TPCW_CREATENEWCUSTOMER1);
		dos.writeInt(c_id);
		dos.writeUTF(c_uname);
		dos.writeUTF(c_passwd);
		dos.writeDouble(c_discount);
		dos.writeUTF(c_fname);
		dos.writeUTF(c_lname);
		dos.writeUTF(c_phone);
		dos.writeUTF(c_email);
		dos.writeLong(c_last_visit);
		dos.writeLong(c_since);
		dos.writeLong(c_login);
		dos.writeLong(c_expiration);
		dos.writeLong(c_birthdate);
		dos.writeUTF(c_data);
		dos.writeInt(addr_id);
		dos.writeUTF(street1);
		dos.writeUTF(street2);
		dos.writeUTF(city);
		dos.writeUTF(state);
		dos.writeUTF(zip);
		dos.writeInt(country_id);
		return new DBTPCWShdCreateNewCustomer1(baos.toByteArray(), c_id,
				c_uname, c_passwd, c_discount, c_fname, c_lname, c_phone, c_email, c_last_visit, c_since, c_login, c_expiration,
				c_birthdate, c_data,addr_id, street1,
				street2, city, state, zip, country_id);

	}

	protected DBTPCWShdCreateNewCustomer1(byte[] arr, int c_id,
			String c_uname, String c_passwd, double c_discount,	 String c_fname, String c_lname,   String c_phone,   String c_email, long c_last_visit, long c_since,
			long c_login, long c_expiration, long c_birthdate, String c_data, int addr_id, String street1,
			String street2, String city, String state, String zip,
			int country_id) {
		super(arr);
		this.c_id = c_id;
		this.c_uname = c_uname;
		this.c_passwd = c_passwd;
		this.c_discount = c_discount;
		this.c_balance = 0.0;
		this.c_ytd_pmt = 0.0;
	    this.c_fname = c_fname;
	    this.c_lname = c_lname;
	    this.c_phone = c_phone;
	    this.c_email = c_email;
		this.c_last_visit = new Date(c_last_visit);
		this.c_since = new Date(c_since);
		this.c_login = new Date(c_login);
		this.c_expiration = new Date(c_expiration);
		this.c_birthdate = new Date(c_birthdate);
		this.c_data = c_data;
		this.addr_id = addr_id;
		this.addr_street1 = street1;
		this.addr_street2 = street2;
		this.addr_city = city;
		this.addr_state = state;
		this.addr_zip = zip;
		this.country_id = country_id;
	}

	public DBTPCWShdCreateNewCustomer1(byte[] arr) {
		super(arr);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		// TODO Auto-generated method stub

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
			Debug.print("Shadow create customer 1 execution");
			String customerQuery = "INSERT into customer (c_id, c_uname, c_passwd, c_fname, c_lname, c_addr_id, c_phone, c_email, " +
					"c_since, c_last_login, c_login, c_expiration, c_discount, c_balance, c_ytd_pmt, c_birthdate, c_data) " +
					"VALUES ("+this.c_id+", '"+this.c_uname+"', '"+this.c_passwd+"', '"+this.c_fname+"', '"+this.c_lname+"', "+this.addr_id+", '" +
							this.c_phone+"', '"+this.c_email+"', '"+this.c_since+"', '"+this.c_last_visit+"', '"+this.c_login+"', '"+this.c_expiration+"', "+this.c_discount+
							", "+this.c_balance+", "+this.c_ytd_pmt+", '"+this.c_birthdate+"', '"+this.c_data+"')";
			iDefDatabase.executeUpdate(customerQuery);
			String addressQuery = "INSERT into address (addr_id, addr_street1, addr_street2, addr_city, addr_state, addr_zip, addr_co_id) VALUES " +
					"( "+addr_id +", '"+addr_street1+"', '"+addr_street2+"', '"+addr_city+"', '"+addr_state+"', '"+addr_zip+"', "+country_id+")";
			iDefDatabase.executeUpdate(addressQuery);
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow operation");
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
