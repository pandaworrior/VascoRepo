package org.mpi.vasco.txstore.coordinator;
import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.txstore.messages.AckTxnMessage;
import org.mpi.vasco.txstore.messages.BeginTxnMessage;
import org.mpi.vasco.txstore.messages.CommitShadowOpMessage;
import org.mpi.vasco.txstore.messages.ProxyCommitMessage;
import org.mpi.vasco.txstore.messages.RemoteShadowOpMessage;
import org.mpi.vasco.txstore.util.Operation;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.TimeStamp;
import org.mpi.vasco.txstore.util.StorageList;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.ReadWriteSet;
import org.mpi.vasco.txstore.util.ReadSet;
import org.mpi.vasco.txstore.util.WriteSet;
import org.mpi.vasco.txstore.util.WriteSetEntry;
import org.mpi.vasco.txstore.util.ReadSetEntry;

import java.util.Hashtable;
import java.util.Vector;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TransactionRecord{

    ProxyTxnId txnId = null;
    StorageList slist;
    Hashtable<Integer, ReadWriteSet> rwSets;
    TimeStamp startTime;
    LogicalClock startClock;
    LogicalClock snapshotClock;
    LogicalClock mergeClock;
    TimeStamp finishTime;
    boolean blue = false;
    long blueEpoch = 0;
    WriteSet wSet = null;
    long realTimeStart = 0;
    boolean readOnly = true;
    boolean local = true;
    Operation shadowOp=null;
    int color;
    ProxyCommitMessage msg = null;
    BeginTxnMessage bMsg = null;
    AckTxnMessage aMsg = null;
    RemoteShadowOpMessage rOpMsg = null;
    CommitShadowOpMessage cSMsg = null;
    
    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    public TransactionRecord(){
    	rwSets = new Hashtable<Integer, ReadWriteSet>();
    	slist = new StorageList();
    }
    
    public void setTxnId(ProxyTxnId id){
    	txnId = id;
    }
    
    public void setTimestampLc(TimeStamp t, LogicalClock lc){
    	startTime = t;
    	startClock = lc;
    }
    
    public TransactionRecord(ProxyTxnId id, TimeStamp t, LogicalClock lc){
	txnId = id;
	rwSets = new Hashtable<Integer, ReadWriteSet>();
	startTime = t;
	startClock = lc;
	blue = false;
        realTimeStart = System.nanoTime();
        slist = new StorageList();
    }
    
    public void reset(){
    	txnId = null;
        slist.reset();
        rwSets.clear();
        startTime = null;
        startClock = null;
        snapshotClock = null;
        mergeClock = null;
        finishTime = null;
        wSet = null;
        realTimeStart = 0;
        readOnly = true;
        local = true;
        shadowOp=null;
        color = 0;
        msg = null;
        bMsg = null;
        rOpMsg = null;
        aMsg = null;
        cSMsg= null;
    }
    
    public void setProxyCommitMessage(ProxyCommitMessage tmMsg){
    	msg = tmMsg;
    }
    
    public void setBeginTxnMessage(BeginTxnMessage bmsg){
    	bMsg = bmsg;
    }
    
    public void setAckTxnMessage(AckTxnMessage tmpAmsg){
    	aMsg = tmpAmsg;
    }
    
    public void setRemoteShadowOpMessage(RemoteShadowOpMessage rMsg){
    	rOpMsg = rMsg;
    }
    
    public void setCommitShadowOpMessage(CommitShadowOpMessage cMsg){
    	cSMsg = cMsg;
    }

    
    public long realTimeStart(){
        return realTimeStart;
    }
    
    public long getLatency(){
    	return (System.nanoTime() - realTimeStart);
    }
    
    public void setStorageList(StorageList sl){
	slist = sl;
    }
    
    public void addStorage(int i){
    	slist.addStorage(i);
    }

    public StorageList getStorageList(){
	return slist;
    }


    public ProxyTxnId getTxnId(){
	return txnId;
    }

    public boolean isFinished(){
	return slist != null && slist.getStorageCount() == rwSets.size();	    
    }
    
    public boolean isRed(){
	    if(color == 1){
	    	return false;
	    }
	    return true;
    }

    public boolean isBlue(){
	    if(color == 1)
	    	return true;
	    return false;
    }

    public void forceBlue(){
	blue = true;
    }
    
    public boolean isReadonly(){
    	return readOnly;
    }
    
    /**
     * two fields to show whether it is local or remote
     */
    public void setRemote(){
    	local = false;
    }
    
    public boolean isLocal(){
    	return local;
    }

    public void setReadWriteSet(ReadWriteSet rws, int sto){
	Integer i = new Integer(sto);
	if (rwSets.get(i) != null)
	    throw new RuntimeException("already have a readwrite set form the server");
	rwSets.put(i, rws);
	WriteSetEntry[] ws = rws.getWriteSet().getWriteSet();
	if(ws.length > 0 && readOnly)
		readOnly = false;
    }


    public void setWriteSet(WriteSet ws){
	wSet = ws;
	WriteSetEntry[] _ws = ws.getWriteSet();
	if(_ws.length > 0 && readOnly)
		readOnly = false;
    }
    
    static ReadWriteSet[] rw = new ReadWriteSet[0];
    public ReadWriteSet[] getReadWriteSets(){
	return rwSets.values().toArray(rw);
    }


    public ReadSet getReadSet(){
	Vector<ReadSetEntry> v = new Vector<ReadSetEntry>();
	for(int i = 0; i < slist.getStorageCount(); i++){
	    ReadSet rs = rwSets.get(slist.getStorageInteger(i)).getReadSet();
	    for (int j = 0; j < rs.getReadSet().length; j++)
		v.add(rs.getReadSet()[j]);
	}
	return new ReadSet(v.toArray(new ReadSetEntry[0]));

    }

  public WriteSet getWriteSet(){
      if (wSet != null)
	  return wSet;
	Vector<WriteSetEntry> v = new Vector<WriteSetEntry>();
	for(int i = 0; i < slist.getStorageCount(); i++){
	    WriteSet rs = rwSets.get(slist.getStorageInteger(i)).getWriteSet();
	    for (int j = 0; j < rs.getWriteSet().length; j++)
		v.add(rs.getWriteSet()[j]);
	}
	return new WriteSet(v.toArray(new WriteSetEntry[0]));
    }
  
  /**
   * 
   * @param set shadow operation
   */
  public void setShadowOp(Operation op){
	  shadowOp = op;
  }
  
  public Operation getShadowOp(){
	  return shadowOp;
  }
  
  /**
   * 
   * @ set color
   */
  
  public void setColor(int c){
	  color = c;
  }

    public TimeStamp getStartTime(){
	return startTime;
    }

    public void setFinishTime(TimeStamp ft){
	finishTime = ft;
    }

    public TimeStamp getFinishTime(){
	return finishTime;
    }

    public LogicalClock getStartClock(){
	return startClock;
    }



    public LogicalClock getMergeClock(){
	return mergeClock;
    }
    
    public void setMergeClock(LogicalClock lc){
	mergeClock = lc;
    }

    public LogicalClock getSnapshotClock(){
	return snapshotClock;
    }
    
    /** set the snapshot clock based on the start time, the read times,
	and the last blue clock provided by the coordinator
    **/
    public void setSnapshotClock(LogicalClock lbc){
//        System.out.println("\t\t^^^^^^setsnapshot start");
	LogicalClock tmp = getStartClock();
	ReadWriteSet rws[] = getReadWriteSets();
	for (int j=0; j < rws.length; j++){
	    ReadSetEntry rs[] = rws[j].getReadSet().getReadSet();
	    for (int i = 0; i < rs.length; i++){
	    	//System.out.printf("record lc %s\n", rs[i].toString());
			tmp = tmp.maxClock(rs[i].getLogicalClock());
	    }
	}
	if (isBlue())
	    tmp = tmp.maxClock(lbc);
	snapshotClock = tmp;
  //      System.out.println("\t\t^^^^^^setsnapshot end");
    }

    public long getBlueEpoch(){
	return blueEpoch;
    }

    public void setBlueEpoch(long b){
	blueEpoch = b;
    }

     
    public Lock writeLock() {
        return lock.writeLock();
    }
    
    public Lock readLock(){
        return lock.readLock();
    
    }
    
    public static void main(String arg[]){

	Integer i = new Integer(4);
	Integer j = i;
	Debug.println(i+" "+j);
	j = j+1;
	Debug.println(i+" "+j);
    }

}