package org.mpi.vasco.txstore.scratchpad.rdbms.util;
import org.mpi.vasco.util.debug.Debug;

import java.util.*;

import java.io.*;

import org.mpi.vasco.txstore.scratchpad.ScratchpadException;
import org.mpi.vasco.txstore.util.OperationLog;
import org.mpi.vasco.txstore.util.Result;

public class DBOperationLog
	extends OperationLog
{
	transient boolean hasDecoded;
	transient List<DBOpPair> log;
	
	public static DBOperationLog createLog( List<DBOpPair> log) throws ScratchpadException {
		try {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeShort( log.size());
		Iterator<DBOpPair> it = log.iterator();
		while( it.hasNext()) {
			DBOpPair op = it.next();
			DBOpPair.encode(op, dos);
		}
		return new DBOperationLog( log, baos.toByteArray());
		} catch (IOException e) {
			throw new ScratchpadException( "Cannot encode result", e);
		}
	}
	
	public static DBOperationLog createLog( OperationLog log) throws ScratchpadException {
		return new DBOperationLog( log.getOperationLogBytes());
	}
	protected DBOperationLog( List<DBOpPair> log, byte[] logBA) {
		super( logBA);
		this.log = log;
		this.hasDecoded = true;
	}
	
	protected DBOperationLog( byte[] logBA) {
		super( logBA);
		this.log = null;
		this.hasDecoded = false;
	}
	
	private void decode() throws ScratchpadException {
		try {
			hasDecoded = true;
			ByteArrayInputStream bais = new ByteArrayInputStream( super.getOperationLogBytes());
			DataInputStream dis = new DataInputStream( bais);
			log = new ArrayList<DBOpPair>();
			int nLines = dis.readShort();
			if( nLines == 0)
				return;
			for( int i = 0; i < nLines; i++) {
				log.add( DBOpPair.decode(dis));
			}
		} catch( IOException e) {
			throw new ScratchpadException( "Cannot decode result", e);
		}
	}
		
	public List<DBOpPair> getLog() throws ScratchpadException {
		if( ! hasDecoded)
			decode();
		return log;
	}

	
	public String toString() {
		if( ! hasDecoded)
			try {
				decode();
			} catch (ScratchpadException e) {
				// do ntohing
				e.printStackTrace();
			}
		StringBuffer buffer = new StringBuffer();
		for( int i = 0; i < log.size(); i++) {
			DBOpPair op = log.get(i);
			buffer.append( op.toString());
			buffer.append( "\n");
		}
		return buffer.toString();
	}
}
