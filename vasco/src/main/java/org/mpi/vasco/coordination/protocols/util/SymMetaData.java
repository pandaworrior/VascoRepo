package org.mpi.vasco.coordination.protocols.util;

public class SymMetaData {
	LockReply reply;
	LockRequest request;
	
	public SymMetaData(LockRequest lcRequest, LockReply lcReply){
		this.request = lcRequest;
		this.reply = lcReply;
	}
    
    public LockReply getLockReply(){
    	return this.reply;
    }
    
    public void setLockReply(LockReply lcR){
    	this.reply = lcR;
    }
    
    public LockRequest getLockRequest(){
    	return this.request;
    }
}
