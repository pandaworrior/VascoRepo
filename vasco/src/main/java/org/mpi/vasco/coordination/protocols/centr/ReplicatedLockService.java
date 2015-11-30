package org.mpi.vasco.coordination.protocols.centr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mpi.vasco.coordination.VascoServiceAgentFactory;
import org.mpi.vasco.coordination.protocols.messages.LockReqMessage;
import org.mpi.vasco.coordination.protocols.util.ConflictTable;
import org.mpi.vasco.coordination.protocols.util.LockReply;
import org.mpi.vasco.coordination.protocols.util.LockRequest;
import org.mpi.vasco.coordination.protocols.util.Protocol;
import org.mpi.vasco.util.debug.Debug;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;

public class ReplicatedLockService extends DefaultRecoverable{
	
	//data here
	private boolean logPrinted = false;
    protected ConflictTable conflictTable;

    // first level { key: table name + key, value : a set of counters for operations}
    // second level { key: conflicting operation name, values: counter value}
    protected HashMap<String, HashMap<String, Long>> counters;
    
    public void setConflictTable(ConflictTable _cTable){
    	this.conflictTable = _cTable;
    	Debug.println("Already set conflict table \n");
    	this.conflictTable.printOut();
    }
    
    public ConflictTable getConflictTable(){
    	return this.conflictTable;
    }
    
    public String countersToString() {
        StringBuilder sb=new StringBuilder();
        for(Entry<String, HashMap<String, Long>> fstLEntry: counters.entrySet()) {
        	sb.append("key: " + fstLEntry.getKey() + "\n");
        	sb.append("{");
        	for(Map.Entry<String, Long> sndLEntry : fstLEntry.getValue().entrySet()){
        		sb.append("op: " + sndLEntry.getKey() + ",");
        		sb.append(" value: ");
        		sb.append(sndLEntry.getValue().longValue());
        	}
        	sb.append("}\n");
        }
        return sb.toString();
    }
	
	public ReplicatedLockService(int id, String xmlFile){
		//set the conflictable
        this.setConflictTable(new ConflictTable(xmlFile));
        this.setCounters(new HashMap<String, HashMap<String, Long>>(VascoServiceAgentFactory.BIG_MAP_INITIAL_SIZE));
		new ServiceReplica(id, this, this);
		System.out.println("Replicated Lock Server "+ id +" is already set up!");
	}
	

	@Override
	public byte[] executeUnordered(byte[] arg0, MessageContext arg1) {
		throw new RuntimeException("Should not call executeUnordered");
	}

	@Override
	public byte[][] appExecuteBatch(byte[][] commands, MessageContext[] msgCtx) {
		byte[][] replies = new byte[commands.length][];
		int index = 0;
    	for(byte[] command: commands) {
    		if(msgCtx != null && msgCtx[index] != null && msgCtx[index].getConsensusId() % 1000 == 0 && !logPrinted) {
    			System.out.println("Replicated Lock Server executing eid: " + msgCtx[index].getConsensusId());
    			logPrinted = true;
    		} else
    			logPrinted = false;
    		
    		LockReqMessage lockRequestMsg = new LockReqMessage(command);
    		LockReply lcReply = this._getAndAdd(lockRequestMsg.getLockReq());
    		replies[index++] = lcReply.getBytes();
    	}
		return replies;
	}

	@Override
	public byte[] getSnapshot() {
		try {
			System.out.println("getState called");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(this.getCounters());
			out.flush();
			bos.flush();
			out.close();
			bos.close();
			return bos.toByteArray();
		} catch (IOException ioe) {
			System.err.println("[ERROR] Error serializing state: "
					+ ioe.getMessage());
			return "ERROR".getBytes();
		}
	}

	@Override
	public void installSnapshot(byte[] state) {
		try {
			System.out.println("setState called");
			ByteArrayInputStream bis = new ByteArrayInputStream(state);
			ObjectInput in = new ObjectInputStream(bis);
			try {
				HashMap<String, HashMap<String, Long>> recoveredCounter = (HashMap<String, HashMap<String, Long>>) in.readObject();
				this.setCounters(recoveredCounter);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			in.close();
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	//{compoundKey, {(countername, longvalue)}
    protected LockReply _getAndAdd(LockRequest lrRequest) {
    	//Debug.println("Execute get and add for request " + lrRequest.toString() + "\n");
    	LockReply lcReply = new LockReply(lrRequest.getOpName(), Protocol.PROTOCOL_SYM);
    	//get a list of conflicts:
    	Set<String> conflicts = this.conflictTable.getConflictByOpName(lrRequest.getOpName(), Protocol.PROTOCOL_SYM).getConfList();
    	
        synchronized(counters) {
        	for(String keyStr : lrRequest.getKeyList()){
        		HashMap<String, Long> countersPerKey = this.getCounters().get(keyStr);
        		if(countersPerKey == null){
        			countersPerKey = new HashMap<String, Long>(VascoServiceAgentFactory.SMALL_MAP_INITIAL_SIZE);
        			this.counters.put(keyStr, countersPerKey);
        		}
        		for(String conflictStr : conflicts){
    				Long counter = countersPerKey.get(conflictStr);
        			if(counter == null){
        				lcReply.addKeyCounterPair(keyStr, conflictStr, 0L);
        				countersPerKey.put(conflictStr, 1L);
        			}else{
        				lcReply.addKeyCounterPair(keyStr, conflictStr, counter.longValue());
        				countersPerKey.put(conflictStr, counter.longValue() + 1L);
        			}
    			}
        	}
        }
        //Debug.println("\t ----> printOutCounters");
        //Debug.println(this.countersToString());
        //Debug.println("\t<---- printOutCounters");
        return lcReply;
    }

	public HashMap<String, HashMap<String, Long>> getCounters() {
		return counters;
	}

	public void setCounters(HashMap<String, HashMap<String, Long>> counters) {
		this.counters = counters;
	}
	
	public static void main(final String[] args) throws Exception {
    	if(args.length != 2){
    		help();
    		return;
    	}
    	
    	String xmlFile = args[0];
    	int myId = Integer.parseInt(args[1]);
        new ReplicatedLockService(myId, xmlFile);

    }

    private static void help() {
        System.out.println("ReplicatedLockService [serverclientconflictxmlfile] [myId]");
    }

}
