package org.mpi.vasco.txstore.proxy;

import org.mpi.vasco.txstore.util.Operation;

public interface ApplicationInterface{

    /** returns the integer identifier for the storage server responsible for the operation **/
    public int selectStorageServer(Operation op);
    public int selectStorageServer(byte[] op);
}