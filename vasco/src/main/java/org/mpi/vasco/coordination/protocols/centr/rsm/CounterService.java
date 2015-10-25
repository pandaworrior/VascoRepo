package org.mpi.vasco.coordination.protocols.centr.rsm;

import org.jgroups.Channel;
import org.jgroups.Global;
import org.jgroups.protocols.raft.*;
import org.jgroups.raft.RaftHandle;
import org.jgroups.util.*;
import org.mpi.vasco.coordination.VascoServiceAgentFactory;
import org.mpi.vasco.coordination.protocols.util.ConflictTable;
import org.mpi.vasco.coordination.protocols.util.LockReply;
import org.mpi.vasco.coordination.protocols.util.LockRequest;
import org.mpi.vasco.coordination.protocols.util.Protocol;
import org.mpi.vasco.util.debug.Debug;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Provides a consensus based distributed map of counters which can be atomically updated across a cluster.
 * @author Originally from Bela Ban, modified by Cheng
 * TODO: add conflict table inside
 * TODO: add conflict symmetry and asymmetry
 * @since  0.2
 */
public class CounterService implements StateMachine, RAFT.RoleChange {
    protected Channel    ch;
    protected RaftHandle raft;
    protected long       repl_timeout=20000; // timeout (ms) to wait for a majority to ack a write
    
    protected ConflictTable conflictTable;

    // first level { key: table name + key, value : a set of counters for operations}
    // second level { key: conflicting operation name, values: counter value}
    protected Map<String, Map<String, Long>> counters;

    protected enum Command {getAndAdd}


    public CounterService(Channel ch) {
        setChannel(ch);
        this.setCounters(new Object2ObjectOpenHashMap<String, Map<String, Long>>(VascoServiceAgentFactory.BIG_MAP_INITIAL_SIZE));
    }

    public void setChannel(Channel ch) {
        this.ch=ch;
        this.raft=new RaftHandle(this.ch, this);
        raft.addRoleListener(this);
    }
    
    public void setConflictTable(ConflictTable _cTable){
    	this.conflictTable = _cTable;
    	Debug.println("Already set conflict table \n");
    	this.conflictTable.printOut();
    }
    
    public ConflictTable getConflictTable(){
    	return this.conflictTable;
    }

    public void           addRoleChangeListener(RAFT.RoleChange listener)  {raft.addRoleListener(listener);}
    public long           replTimeout()                 {return repl_timeout;}
    public CounterService replTimeout(long timeout)     {this.repl_timeout=timeout; return this;}
    public int            lastApplied()                 {return raft.lastApplied();}
    public int            commitIndex()                 {return raft.commitIndex();}
    public void           snapshot() throws Exception   {raft.snapshot();}
    public int            logSize()                     {return raft.logSizeInBytes();}
    public String         raftId()                      {return raft.raftId();}
    public CounterService raftId(String id)             {raft.raftId(id); return this;}

    
    public LockReply getAndAdd(LockRequest lcRequest) throws Exception{
    	Object revl = invoke(Command.getAndAdd, lcRequest, false);
    	byte[] arr = (byte[]) revl;
    	Debug.println("The length of the returned arr is " + arr.length + "\n");
    	LockReply lcp = new LockReply(arr, 0);
    	return lcp;
    }


    public String countersToString() {
        StringBuilder sb=new StringBuilder();
        for(Map.Entry<String, Map<String, Long>> fstLEntry: counters.entrySet()) {
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


    @Override
    public byte[] apply(byte[] data, int offset, int length) throws Exception {
    	Debug.println("Apply command here \n");
        //ByteArrayDataInputStream in=new ByteArrayDataInputStream(data, offset, length);
        //Command command=Command.values()[in.readByte()];
        Command command=Command.values()[data[0]];
        LockRequest lcRequest = new LockRequest(data, 1, length - 1);
        LockReply lcReply = null;
        switch(command) {
            case getAndAdd:
                lcReply=_getAndAdd(lcRequest);
                Debug.println("The reply generated is " + lcReply.toString() + "\n");
                Debug.println("The reply byte string length is " + lcReply.getByteSize() + "\n");
                return Util.objectToByteBuffer(lcReply.getBytes());//for compare all results whether equal to each other right?
            default:
                throw new IllegalArgumentException("command " + command + " is unknown");
        }
        //return Util.objectToByteBuffer(null);
    }


    @Override
    public void writeContentTo(DataOutput out) throws Exception {
        /*synchronized(counters) {
            int size=counters.size();
            out.writeInt(size);
            for(Map.Entry<String,Long> entry: counters.entrySet()) {
                AsciiString name=new AsciiString(entry.getKey());
                Long value=entry.getValue();
                Bits.writeAsciiString(name, out);
                Bits.writeLong(value, out);
            }
        }*/
    }

    @Override
    public void readContentFrom(DataInput in) throws Exception {
        /*int size=in.readInt();
        for(int i=0; i < size; i++) {
            AsciiString name=Bits.readAsciiString(in);
            Long value=Bits.readLong(in);
            counters.put(name.toString(), value);
        }*/
    }

    /*public void dumpLog() {
        raft.logEntries((entry, index) -> {
            StringBuilder sb=new StringBuilder().append(index).append(" (").append(entry.term()).append("): ");
            if(entry.command() == null) {
                sb.append("<marker record>");Long
                System.out.println(sb);
                return;
            }
            if(entry.internal()) {
                try {
                    InternalCommand cmd=(InternalCommand)Util.streamableFromByteBuffer(InternalCommand.class,
                                                                                       entry.command(), entry.offset(), entry.length());
                    sb.append("[internal] ").append(cmd);
                }
                catch(Exception ex) {
                    sb.append("[failure reading internal cmd] ").append(ex);
                }
                System.out.println(sb);
                return;
            }
            ByteArrayDataInputStream in=new ByteArrayDataInputStream(entry.command(), entry.offset(), entry.length());
            try {
                Command cmd=Command.values()[in.readByte()];
                String name=Bits.readAsciiString(in).toString();
                switch(cmd) {
                    case create:
                    case set:
                    case addAndGet:
                        sb.append(print(cmd, name, 1, in));
                        break;
                    case delete:
                    case get:
                    case incrementAndGet:
                    case decrementAndGet:
                        sb.append(print(cmd, name, 0, in));
                        break;
                    case compareAndSet:
                        sb.append(print(cmd, name, 2, in));
                        break;
                    default:
                        throw new IllegalArgumentException("command " + cmd + " is unknown");
                }
            }
            catch(Throwable t) {
                sb.append(t);
            }
            System.out.println(sb);
        });
    }*/

    @Override
    public void roleChanged(Role role) {
        System.out.println("-- changed role to " + role);
    }

    /**
     * The core function to invoke the raft consensus protocol.
     * We serialize the lock request to byte string and send
     * to all replicas for replication.
     * */
    protected Object invoke(Command command, LockRequest lrq, boolean ignore_return_value, long ... values) throws Exception {
        Debug.println("Invoke command " + command + " for request " + lrq.toString() + "\n");
    	ByteArrayDataOutputStream out=new ByteArrayDataOutputStream(lrq.getByteSize() + 1);
        try {
            out.writeByte(command.ordinal());
            out.write(lrq.getBytes());
        }catch(Exception ex) {
            throw new Exception("serialization failure (cmd=" + command + ", lockrequest=" + lrq.toString() + ")");
        }

        byte[] buf=out.buffer();
        byte[] rsp=raft.set(buf, 0, out.position(), repl_timeout, TimeUnit.MILLISECONDS);
        return ignore_return_value? null: Util.objectFromByteBuffer(rsp);
    }

    protected static String print(Command command, String name, int num_args, DataInput in) {
        StringBuilder sb=new StringBuilder(command.toString()).append("(").append(name);
        for(int i=0; i < num_args; i++) {
            try {
                long val=Bits.readLong(in);
                sb.append(", ").append(val);
            }
            catch(IOException e) {
                break;
            }
        }
        sb.append(")");
        return sb.toString();
    }

    //{compoundKey, {(countername, longvalue)}
    protected LockReply _getAndAdd(LockRequest lrRequest) {
    	Debug.println("Execute get and add for request " + lrRequest.toString() + "\n");
    	LockReply lcReply = new LockReply(lrRequest.getOpName(), Protocol.PROTOCOL_SYM);
    	//get a list of conflicts:
    	Set<String> conflicts = this.conflictTable.getConflictByOpName(lrRequest.getOpName(), Protocol.PROTOCOL_SYM).getConfList();
    	
        synchronized(counters) {
        	for(String keyStr : lrRequest.getKeyList()){
        		Map<String, Long> countersPerKey = this.getCounters().get(keyStr);
        		if(countersPerKey == null){
        			countersPerKey = new Object2ObjectOpenHashMap<String, Long>(VascoServiceAgentFactory.BIG_MAP_INITIAL_SIZE);
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
        Debug.println("\t ----> printOutCounters");
        Debug.println(this.countersToString());
        Debug.println("\t<---- printOutCounters");
        return lcReply;
    }

	public Map<String, Map<String, Long>> getCounters() {
		return counters;
	}

	public void setCounters(Map<String, Map<String, Long>> counters) {
		this.counters = counters;
	}

}