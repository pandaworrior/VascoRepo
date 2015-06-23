package org.mpi.vasco.txstore.util;


public class TxnIdFactory{

    int dcId;
    int proxyId;
    int count;

    public TxnIdFactory(int dcId, int proxy){
	this.dcId = dcId;
	this.proxyId = proxy;
	count = 0;
    }

    public ProxyTxnId nextTxnId(){
	return new ProxyTxnId(dcId, proxyId, ++count);
    }

}