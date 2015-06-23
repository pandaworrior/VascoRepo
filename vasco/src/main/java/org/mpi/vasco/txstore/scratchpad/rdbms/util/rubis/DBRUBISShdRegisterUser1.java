package org.mpi.vasco.txstore.scratchpad.rdbms.util.rubis;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.util.debug.Debug;

public class DBRUBISShdRegisterUser1 extends DBShadowOperation{
	private int id;
	private String firstname;
	private String lastname;
	private String nickname;
	private String password;
	private String email;
	private String creation_date;
	private int region;
	

	public static DBRUBISShdRegisterUser1 createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int id = dis.readInt();
		dos.writeInt(id);
		String firstname = dis.readUTF();
		dos.writeUTF(firstname);
		String lastname = dis.readUTF();
		dos.writeUTF(lastname);
		String nickname = dis.readUTF();
		dos.writeUTF(nickname);
		String password = dis.readUTF();
		dos.writeUTF(password);
		String email = dis.readUTF();
		dos.writeUTF(email);
		String creation_date = dis.readUTF();
		dos.writeUTF(creation_date);
		int region = dis.readInt();
		dos.writeInt(region);
		return new DBRUBISShdRegisterUser1( baos.toByteArray(), id,firstname,lastname,nickname,password,email,creation_date,region);
		
	}
	public static DBRUBISShdRegisterUser1 createOperation(int id, String firstname,String lastname,String nickname,String password,String email, String creation_date,int region) throws IOException {
		Debug.println("register user operation\n");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_RUBIS_REGISTERUSER1);
		dos.writeInt(id);
		dos.writeUTF(firstname);
		dos.writeUTF(lastname);
		dos.writeUTF(nickname);
		dos.writeUTF(password);
		dos.writeUTF(email);
		dos.writeUTF(creation_date);
		dos.writeInt(region);
		Debug.println("register user operation done\n");
		return new DBRUBISShdRegisterUser1( baos.toByteArray(), id,firstname,lastname,nickname,password,email,creation_date,region);
		
	}
	
	protected DBRUBISShdRegisterUser1(byte[] arr) {
		super(arr);
	}
	
	protected DBRUBISShdRegisterUser1(byte[] arr, int id, String firstname,String lastname,String nickname,String password,String email, String creation_date,int region) {
		super(arr);
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.nickname = nickname;
		this.password = password;
		this.email = email;
		this.creation_date = creation_date;
		this.region = region;
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
		return 0;
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		Debug.print("register user encode\n");
		dos.writeInt(id);
		dos.writeUTF(firstname);
		dos.writeUTF(lastname);
		dos.writeUTF(nickname);
		dos.writeUTF(password);
		dos.writeUTF(email);
		dos.writeUTF(creation_date);
		dos.writeInt(region);
	}
	
	
	@Override
	public void executeShadow(IDefDatabase iDefDatabase) {
		// TODO Auto-generated method stub
		try {
			Debug.println("Shadow Register User execution " + id);
			iDefDatabase.executeUpdate("INSERT INTO " +
		          	"users(id,firstname,lastname,nickname,password,email,rating,balance,creation_date,region) " +
		          	"VALUES ("+id+", \""
		            + firstname
		            + "\", \""
		            + lastname
		            + "\", \""
		            + nickname
		            + "\", \""
		            + password
		            + "\", \""
		            + email
		            + "\", 0, 0,\""
		            + creation_date
		            + "\", "
		            + region
		            + ")");
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow register user operation");
			e.printStackTrace();
		}
	}

}

