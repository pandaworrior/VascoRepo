package org.mpi.vasco.txstore.scratchpad.rdbms.util;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.mpi.vasco.txstore.scratchpad.ScratchpadException;
import org.mpi.vasco.txstore.scratchpad.rdbms.PrimaryExecResults;
import org.mpi.vasco.txstore.util.Result;
import org.mpi.vasco.util.debug.Debug;


public class DBOpPair
{
	public static final byte OPPAIR_SINGLE = 71;
	public static final byte OPPAIR_GENERIC = 72;
	
	public static void encode( DBOpPair pair0, DataOutputStream dos) throws IOException {
		if( pair0 instanceof DBSingleOpPair) {
			DBSingleOpPair pair = (DBSingleOpPair)pair0;
			dos.writeByte( OPPAIR_SINGLE);
			dos.writeByte((byte)pair.pk.length);
			for( int i = 0; i < pair.pk.length; i++)
				dos.writeUTF( pair.pk[i]);
			DBOperation.encode(pair.op, dos);
		} else if( pair0 instanceof DBGenericOpPair) {
			DBGenericOpPair pair = (DBGenericOpPair)pair0;
			dos.writeByte( OPPAIR_GENERIC);
			dos.writeShort( pair.results.results.size());
			Iterator<Result> it = pair.results.results.iterator();
			while( it.hasNext()) {
				Result r = it.next();
				byte[] arr = r.getResult();
				int size = r.getByteSize() > arr.length ? arr.length : r.getByteSize();
				dos.writeBoolean( r instanceof DBSelectResult);
				dos.writeShort( size);
				dos.write( r.getResult(), 0, size);
			}
			DBOperation.encode(pair.op, dos);
		} else
			throw new RuntimeException( "Cannot encode : " + pair0);
	}
	
	public static DBOpPair decode( DataInputStream dis) throws IOException, ScratchpadException {
		byte type = dis.readByte();
		if( type == OPPAIR_SINGLE) {
			byte pklen = dis.readByte();
			String []pks = new String[ pklen];
			for( int i = 0; i < pklen; i++)
				pks[i] = dis.readUTF();
			return new DBSingleOpPair( (DBSingleOperation)DBOperation.decode(dis), pks);

		} else if( type == OPPAIR_GENERIC) {
			PrimaryExecResults results = new PrimaryExecResults();
			int size = dis.readShort();
			for( int i = 0; i < size; i++) {
				boolean isSelect = dis.readBoolean();
				int len = dis.readShort();
				byte[] arr = new byte[len];
				dis.readFully(arr);
				if( isSelect)
					results.addResult( DBSelectResult.createResult(arr));
				else
					results.addResult( DBUpdateResult.createResult(arr));
			}
			return new DBGenericOpPair( (DBGenericOperation)DBOperation.decode(dis), results);
		} else
			throw new RuntimeException( "Unknows op paircode : " + type);
		
	}
}
