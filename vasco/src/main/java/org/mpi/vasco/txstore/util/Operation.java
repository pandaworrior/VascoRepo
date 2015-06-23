package org.mpi.vasco.txstore.util;
import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.util.UnsignedTypes;

public class Operation implements java.io.Serializable {

	protected byte[] op;

	public Operation(byte[] b) {
		op = b;
	}

	public Operation(byte b[], int offset) {

		long length = UnsignedTypes.bytesToLong(b, offset);
		offset += UnsignedTypes.uint32Size;
		op = new byte[(int) length];
		for (int i = 0; i < op.length; i++)
			op[i] = b[i + offset];
	}

	public void getBytes(byte[] b, int offset) {
		UnsignedTypes.longToBytes(op.length, b, offset);
		offset += UnsignedTypes.uint32Size;
		for (int i = 0; i < op.length; i++)
			b[i + offset] = op[i];
	}

	public final int getByteSize() {
		return op.length + UnsignedTypes.uint32Size;
	}

	public byte[] getOperation() {
		return op;
	}

}
