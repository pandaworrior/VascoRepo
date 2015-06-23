package org.mpi.vasco.txstore.scratchpad;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mpi.vasco.txstore.util.TimeStamp;
import org.mpi.vasco.txstore.util.WriteSet;
import org.mpi.vasco.txstore.util.ReadWriteSet;
import org.mpi.vasco.txstore.util.Result;
import org.mpi.vasco.txstore.util.Operation;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.ReadSet;
import org.mpi.vasco.txstore.util.OperationLog;

public interface ScratchpadInterface{

    /**
       Begin a transaction with identifier txnId
     **/
    public void beginTransaction(ProxyTxnId txnId);

    /**
       Return the readset from the current transaction
     **/
    public ReadSet getReadSet();
    

    /**
       Return the writeset from the current transaction
     **/
    public WriteSet getWriteSet();

    /**
       Execute operation as part of the current transaction, returning result
     **/
    public Result execute(Operation op) throws ScratchpadException;

    
    /**
    Execute operation as part of the current transaction, returning result without parsing it
     **/
    public ResultSet executeOrig(Operation op) throws SQLException;
  
    /**
       Complete a transaction, returning the read/write set for
       conflict evaluation by the coordinator.  A transaction that is
       complete() is ready to be committed (or aborted) but has not
       yet reached that state.  The read/write sets will not change,
       but the effects of the transaciton are not yet durable.
     **/
    public ReadWriteSet complete();


    /**
       retursn the operation log for the current scratchpad transaction
     * @throws ScratchpadException 
     **/
    public OperationLog getOperationLog() throws ScratchpadException;


    /**
       abort the current transaction, discarding any side effects of operations
     * @throws ScratchpadException 
     **/
    public void abort() throws ScratchpadException;

    /**
       Commit the current transaction at logical clock and timestamp.
       This is used to commit local transactions.  Returns the
       OperationLog (i.e. writeset that will be transfered to the
       other data center for application)
     **/
    public OperationLog commit(LogicalClock lc, TimeStamp ts) throws ScratchpadException;


  /**
       Apply an operationLog received from the remote data center.  These
       operations have already beent ransformed appropriately (???) at
       the remote data center and need to be applied "as is"
 * @throws ScratchpadException 
     **/
 //   public void applyOperationLog(OperationLog opLog, LogicalClock lc, TimeStamp ts) throws ScratchpadException;


    /**
       Apply the remote operation log to the scratchpad.  This *will*
       be committed through a call to finalize(logicalclock,
       timestamp) at a later date with a specified lc and ts
     **/
    public void applyOperationLog(OperationLog opLog) throws ScratchpadException;

    /**
       Commit a remote transaction at logical clock and timestamp.
       Does not return anything.
     * @throws ScratchpadException 
     **/
    public void finalize(LogicalClock lc, TimeStamp ts) throws ScratchpadException;
    
    /**
     * commit shadow operation
     * @throws ScratchpadException 
     */
    
    public void commitShadowOP(Operation op,  final LogicalClock lc, final TimeStamp ts) throws ScratchpadException; 

}