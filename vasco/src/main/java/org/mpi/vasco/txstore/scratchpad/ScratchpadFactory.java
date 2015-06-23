package org.mpi.vasco.txstore.scratchpad;

import org.mpi.vasco.txstore.util.ProxyTxnId;
 
public interface ScratchpadFactory{

    /** creates a new scratchpad for transaction txn **/
    public ScratchpadInterface createScratchPad(ProxyTxnId txn);
    
    // /** functions added by Cheng to maintain a scratchpad pool**/
    public void releaseScratchpad(ScratchpadInterface sp);
    
    public int getAvailablePoolSize();
    
    public void releaseScratchpad(ScratchpadInterface sp,ProxyTxnId txnid);
}