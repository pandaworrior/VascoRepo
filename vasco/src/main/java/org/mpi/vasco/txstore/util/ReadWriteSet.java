package org.mpi.vasco.txstore.util;

import java.util.Vector;
import org.mpi.vasco.util.debug.Debug;

public class ReadWriteSet {

	ReadSet rs;
	WriteSet ws;

	public ReadWriteSet(ReadSet rs, WriteSet ws) {
		this.rs = rs;
		this.ws = ws;
	}

	public ReadWriteSet(ReadSetEntry[] rs, WriteSetEntry ws[]) {
		this(new ReadSet(rs), new WriteSet(ws));
	}

	static ReadSetEntry[] _rse = new ReadSetEntry[0];
	static WriteSetEntry[] _wse = new WriteSetEntry[0];

	public ReadWriteSet(Vector<ReadSetEntry> rs, Vector<WriteSetEntry> ws) {
		this(rs.toArray(_rse), ws.toArray(_wse));
	}

	public ReadWriteSet(byte b[], int offset) {
		rs = new ReadSet(b, offset);
		offset += rs.getByteSize();
		ws = new WriteSet(b, offset);
	}

	public void getBytes(byte[] b, int offset) {
		if (offset + getByteSize() < b.length) {
			Debug.println("needed size " + offset + getByteSize()
					+ "  capacity " + b.length);
			throw new RuntimeException("not enough bytes!");
		}
		rs.getBytes(b, offset);
		offset += rs.getByteSize();
		ws.getBytes(b, offset);
		offset += ws.getByteSize();
	}

	public final int getByteSize() {
		return rs.getByteSize() + ws.getByteSize();
	}

	public ReadSet getReadSet() {
		return rs;
	}

	public WriteSet getWriteSet() {
		return ws;
	}

	public String toString() {
		return "[RS: " + rs + " || WS: " + ws + "]";
	}

}
