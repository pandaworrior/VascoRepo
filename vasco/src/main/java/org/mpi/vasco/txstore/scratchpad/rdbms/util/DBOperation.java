package org.mpi.vasco.txstore.scratchpad.rdbms.util;

import java.io.*;
import org.mpi.vasco.txstore.util.Operation;
import org.mpi.vasco.util.debug.Debug;

/**
 * Encodes a SQL database oepration
 * @author nmp
 *
 */
public abstract class DBOperation
	extends Operation
{
	public static byte OP_SINGLEOP = 30;
	public static byte OP_GENERICOP = 31;
	public static byte OP_SHADOWOP = 32;
	
	public DBOperation( byte[] arr) {
		super( arr);
	}
	
	public static DBOperation decode( Operation op) throws IOException {
		return decode( new DataInputStream( new ByteArrayInputStream( op.getOperation())));
	}

	public static DBOperation decode( DataInputStream dis) throws IOException {
		byte b = dis.readByte();
		if( b == OP_SINGLEOP) {
			Debug.println("single op\n");
			return new DBSingleOperation( dis.readUTF());
		} else if( b == OP_GENERICOP) {
			Debug.println("generic op\n");
			return DBGenericOperation.decodeGeneric( dis);
		} else if( b == OP_SHADOWOP){
			Debug.println("shadow op\n");
			return DBShadowOperation.decodeShadow( dis);
		}
		throw new RuntimeException( "DBOperation decode Cannot decode code: " + b);
	}

	public static void encode( DBOperation op0, DataOutputStream dos) throws IOException {
		if( op0 instanceof DBSingleOperation) {
			Debug.println("encode single op\n");
			DBSingleOperation op = (DBSingleOperation)op0;
			dos.writeByte( OP_SINGLEOP);
			dos.writeUTF( op.sql);
		} else if( op0 instanceof DBGenericOperation) {
			Debug.println("encode generic op\n");
			DBGenericOperation op = (DBGenericOperation)op0;
			dos.writeByte( OP_GENERICOP);
			DBGenericOperation.encodeGeneric( op, dos);
		} else if ( op0 instanceof DBShadowOperation){
			Debug.println("encode shadow op\n");
			DBShadowOperation op = (DBShadowOperation)op0;
			dos.writeByte( OP_SHADOWOP);
			DBShadowOperation.encodeShadow( op, dos);
		}
		else 
			throw new RuntimeException( "Cannot encode: " + op0);
	}
	
}
