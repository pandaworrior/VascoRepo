package org.mpi.vasco.coordination.protocols.messages;
import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.network.messages.ObjectPool;
import org.mpi.vasco.txstore.messages.AbortTxnMessage;
import org.mpi.vasco.txstore.messages.AckCommitTxnMessage;
import org.mpi.vasco.txstore.messages.AckTxnMessage;
import org.mpi.vasco.txstore.messages.BeginTxnMessage;
import org.mpi.vasco.txstore.messages.BlueTokenGrantAckMessage;
import org.mpi.vasco.txstore.messages.BlueTokenGrantMessage;
import org.mpi.vasco.txstore.messages.CommitShadowOpMessage;
import org.mpi.vasco.txstore.messages.CommitTxnMessage;
import org.mpi.vasco.txstore.messages.FinishRemoteMessage;
import org.mpi.vasco.txstore.messages.FinishTxnMessage;
import org.mpi.vasco.txstore.messages.GimmeTheBlueMessage;
import org.mpi.vasco.txstore.messages.MessageTags;
import org.mpi.vasco.txstore.messages.OperationEncodingMessage;
import org.mpi.vasco.txstore.messages.OperationMessage;
import org.mpi.vasco.txstore.messages.ProxyCommitMessage;
import org.mpi.vasco.txstore.messages.ReadWriteSetMessage;
import org.mpi.vasco.txstore.messages.RemoteShadowOpMessage;
import org.mpi.vasco.txstore.messages.ResultMessage;
import org.mpi.vasco.txstore.messages.StorageCommitTxnMessage;
import org.mpi.vasco.txstore.messages.TxnMetaInformationMessage;
import org.mpi.vasco.txstore.messages.TxnReadyMessage;


import org.mpi.vasco.util.UnsignedTypes;

public class MessageFactory{

	public ObjectPool<BeginTxnMessage> BeginTxnMPool;
    public ObjectPool<AckTxnMessage> AckMPool;
    public ObjectPool<OperationMessage> OpMPool;

    public ObjectPool<CommitShadowOpMessage> CommitShadowOpMPool;
	public ObjectPool<ProxyCommitMessage> ProxyCommitMPool;
	public ObjectPool<AckCommitTxnMessage> AckCommitTxnMPool;
	public ObjectPool<RemoteShadowOpMessage> RemoteShadowOpMPool;

    public MessageFactory(){
    	BeginTxnMPool = new ObjectPool<BeginTxnMessage>();
    	AckMPool = new ObjectPool<AckTxnMessage>();
    	OpMPool = new ObjectPool<OperationMessage>();
        CommitShadowOpMPool = new ObjectPool<CommitShadowOpMessage>();
    	ProxyCommitMPool = new ObjectPool<ProxyCommitMessage>();
    	AckCommitTxnMPool = new ObjectPool<AckCommitTxnMessage>();
    	RemoteShadowOpMPool = new ObjectPool<RemoteShadowOpMessage>();
    }

    public MessageBase fromBytes(byte[] bytes){
	int offset = 0;
	int tag = UnsignedTypes.bytesToInt(bytes, offset);
	offset += UnsignedTypes.uint16Size;


	switch(tag){
	case MessageTags.BEGINTXN:
		BeginTxnMessage msg = BeginTxnMPool.borrowObject();
		if( msg == null)
			return new BeginTxnMessage(bytes);
		else{
			msg.decodeMessage(bytes);
			return msg;
		}
	case MessageTags.ACKTXN:
		AckTxnMessage aMsg = AckMPool.borrowObject();
		if(aMsg == null)
			return new AckTxnMessage(bytes);
		else{
			aMsg.decodeMessage(bytes);
			return aMsg;
		}
	case MessageTags.RESULT:
	    return new ResultMessage(bytes);
	case MessageTags.FINISHTXN:
	    return new FinishTxnMessage(bytes);
	case MessageTags.OPERATION:
		OperationMessage oMsg = OpMPool.borrowObject();
		if(oMsg == null)
			return new OperationMessage(bytes);
		else{
			oMsg.decodeMessage(bytes);
			return oMsg;
		}
	case MessageTags.READWRITESET:
	    return new ReadWriteSetMessage(bytes);
	case MessageTags.COMMITTXN:
	    return new CommitTxnMessage(bytes);
        case MessageTags.STORAGECOMMITTXN:
            return new StorageCommitTxnMessage(bytes);
	case MessageTags.ABORTTXN:
	    return new AbortTxnMessage(bytes);
	case MessageTags.OPERATIONENCODING:
	    return new OperationEncodingMessage(bytes);
	case MessageTags.TXNREADY:
	    return new TxnReadyMessage(bytes);
	case MessageTags.TXNMETAINFORMATION:
	    return new TxnMetaInformationMessage(bytes);
	case MessageTags.FINISHREMOTE:
	    return new FinishRemoteMessage(bytes); 
	case MessageTags.GIMMETHEBLUE:
	    return new GimmeTheBlueMessage(bytes);
	case MessageTags.BLUETOKENGRANT:
		return new BlueTokenGrantMessage(bytes);
	case MessageTags.BLUETOKENGRANTACK:
		return new BlueTokenGrantAckMessage(bytes);
	case MessageTags.COMMITSHADOW:
		CommitShadowOpMessage csOpMsg = CommitShadowOpMPool.borrowObject();
		if(csOpMsg == null)
			return new CommitShadowOpMessage(bytes);
		else{
			csOpMsg.decodeMessage(bytes);
			return csOpMsg;
		}
	case MessageTags.PROXYCOMMIT:
		ProxyCommitMessage pcMsg = ProxyCommitMPool.borrowObject();
		if(pcMsg == null )
			return new ProxyCommitMessage(bytes);
		else{
			pcMsg.decodeMessage(bytes);
			return pcMsg;
		}
	case MessageTags.ACKCOMMIT:
		AckCommitTxnMessage acMsg = AckCommitTxnMPool.borrowObject();
		if(acMsg == null)
			return new AckCommitTxnMessage(bytes);
		else{
			acMsg.decodeMessage(bytes);
			return acMsg;
		}
	case MessageTags.REMOTESHADOW:
		RemoteShadowOpMessage rOpMsg = RemoteShadowOpMPool.borrowObject();
		if(rOpMsg == null)
			return new RemoteShadowOpMessage(bytes);
		else{
			rOpMsg.decodeMessage(bytes);
			return rOpMsg;
		}
	default:

	    throw new RuntimeException("Invalid message tag:  "+tag);
            
	}
	
    }
    
    public BeginTxnMessage borrowBeginTxnMessage(){
    	return BeginTxnMPool.borrowObject();
    }
    
    public void returnBeginTxnMessage(BeginTxnMessage msg){
    	msg.reset();
    	BeginTxnMPool.returnObject(msg);	
    }
    
    public OperationMessage borrowOperationMessage(){
    	return OpMPool.borrowObject();
    }
    
    public void returnOperationMessage(OperationMessage msg){
    	msg.reset();
    	OpMPool.returnObject(msg);
    }
    
    public ProxyCommitMessage borrowProxyCommitMessage(){
    	return ProxyCommitMPool.borrowObject();
    }
    
    public void returnProxyCommitMessage(ProxyCommitMessage msg){
    	msg.reset();
    	ProxyCommitMPool.returnObject(msg);
    }
    
    public AckCommitTxnMessage borrowAckCommitTxnMessage(){
    	return AckCommitTxnMPool.borrowObject();
    }
    
    public void returnAckCommitTxnMessage(AckCommitTxnMessage msg){
    	msg.reset();
    	AckCommitTxnMPool.returnObject(msg);
    }
    
    public CommitShadowOpMessage borrowCommitShadowOpMessage(){
    	return CommitShadowOpMPool.borrowObject();
    }
    
    public void returnCommitShadowOpMessage(CommitShadowOpMessage msg){
    	msg.reset();
    	CommitShadowOpMPool.returnObject(msg);
    }
    
    public AckTxnMessage borrowAckTxnMessage(){
		return AckMPool.borrowObject();
    }
    
    public void returnAckTxnMessage(AckTxnMessage msg){
    	msg.reset();
    	AckMPool.returnObject(msg);
    }
    
    public RemoteShadowOpMessage borrowRemoteShadowOpMessage(){
    	return RemoteShadowOpMPool.borrowObject();
    }
    
    public void returnRemoteShadowOpMessage(RemoteShadowOpMessage msg){
    	msg.reset();
    	RemoteShadowOpMPool.returnObject(msg);
    }

}