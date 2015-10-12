package org.mpi.vasco.txstore.proxy;
import java.sql.ResultSet;

import org.mpi.vasco.util.debug.Debug;


import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.Operation;
import org.mpi.vasco.txstore.util.Result;

public interface ClosedLoopProxyInterface{

    public byte[] execute(byte[] op, ProxyTxnId txn);
    public byte[] execute(byte[] op, ProxyTxnId txn, int storageId);

    public Result execute(Operation op, ProxyTxnId txn);
    public Result execute(Operation op, ProxyTxnId txn, int storageId);
    
    public ResultSet executeOrig(Operation op, ProxyTxnId txnid);
    public ResultSet executeOrig(Operation op, ProxyTxnId pr, int sid);

    public ProxyTxnId beginTxn();

    public void abort(ProxyTxnId txn);

    // returns true if the transaction commits, false otherwise
    public boolean commit(ProxyTxnId txn);

	public boolean commit(ProxyTxnId txId, DBShadowOperation op, String opName);
}