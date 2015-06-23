package org.mpi.vasco.txstore.util;

import org.mpi.vasco.util.UnsignedTypes;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

public class LogicalClock implements java.io.Serializable {

    public static String DefaultForInTrx = "0-";
    long blue;
    long[] dcCount;
//    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    ReentrantLock lock = new ReentrantLock();
    public LogicalClock(int dcs) {
        blue = 0;
        this.dcCount = new long[dcs];
        for (int i = 0; i < dcs; i++) {
            this.dcCount[i] = 0;
        }
    }

    public LogicalClock(long dc[], long blue) {
        this.blue = blue;
        this.dcCount = dc;
    }

    public LogicalClock(String s) {
        if (s == null || "null".equalsIgnoreCase(s)) {
            s = DefaultForInTrx;
        }
        String tmp[] = s.split("-");
        dcCount = new long[tmp.length - 1];
        blue = Long.parseLong(tmp[0]);
        for (int i = 1; i < tmp.length; i++) {
            dcCount[i - 1] = Long.parseLong(tmp[i]);
        }

    }

    public LogicalClock(byte b[], int offset) {
        blue = UnsignedTypes.bytesToLongLong(b, offset);
        offset += UnsignedTypes.uint64Size;
        int sz = UnsignedTypes.bytesToInt(b, offset);
        offset += UnsignedTypes.uint64Size;
        dcCount = new long[sz];
        for (int i = 0; i < sz; i++) {
            dcCount[i] = UnsignedTypes.bytesToLongLong(b, offset);
            offset += UnsignedTypes.uint64Size;
        }
    }

    public LogicalClock copy() {
        lockForReads();
        try {
            LogicalClock tmp = new LogicalClock(dcCount.length);
            tmp.blue = blue;
            for (int i = 0; i < dcCount.length; i++) {
                tmp.dcCount[i] = dcCount[i];
            }
            return tmp;
        } finally {
            readLock().unlock();
        }
    }

    public void getBytes(byte[] b, int offset) {
        lockForReads();
        try {
            if (offset + getByteSize() > b.length) {
                throw new RuntimeException("not enough bytes: " + (offset + getByteSize()) + "<" + b.length);
            }
            UnsignedTypes.longlongToBytes(blue, b, offset);
            offset += UnsignedTypes.uint64Size;
            UnsignedTypes.intToBytes(dcCount.length, b, offset);
            offset += UnsignedTypes.uint64Size;
            for (int i = 0; i < dcCount.length; i++) {
                UnsignedTypes.longlongToBytes(dcCount[i], b, offset);
                offset += UnsignedTypes.uint64Size;
            }
        } finally {
            readLock().unlock();
        }
    }

    public final int getByteSize() {
        return UnsignedTypes.uint64Size + UnsignedTypes.uint64Size + dcCount.length * UnsignedTypes.uint64Size;
    }

    public long getBlueCount() {
        lockForReads();
        try {
            return blue;
        } finally {
            readLock().unlock();
        }

    }

    public long[] getDcEntries() {
        lockForReads();
        try {
            return dcCount;
        } finally {
            readLock().unlock();
        }
    }

    public long getDcEntry(int dc) {
        lockForReads();
        try {
            return dcCount[dc];
        } finally {
            readLock().unlock();
        }

    }

    public boolean comparable(LogicalClock lc) {
        lockForReads();
        lc.lockForReads();
        try {
            //    	System.out.printf("lc length %d, dc length %d\n", lc.dcCount.length, dcCount.length);
            return lc.dcCount.length == dcCount.length;
        } finally {
            readLock().unlock();
            lc.readLock().unlock();
        }

    }

    /**
     * 
     *
     * @param lc
     * @return a logical clock that is the pairwise maximum of these two clocks
     */
    public LogicalClock maxClock(LogicalClock lc) {
//        System.out.println("maxclock no locks"+readLock());
        lockForReads();
//        System.out.println("maxclock one lock "+lc.readLock());
        lc.lockForReads();
//        System.out.println("maxclock got both locks");
        try {
            if (!comparable(lc)) {
                throw new RuntimeException("incomparable logicalclocks: " + dcCount.length + " " + lc.dcCount.length);
            }
  //          System.out.println("compared!");
            long[] tmp = new long[dcCount.length];
            for (int i = 0; i < tmp.length; i++) {
                if (dcCount[i] > lc.dcCount[i]) {
                    tmp[i] = dcCount[i];
                } else {
                    tmp[i] = lc.dcCount[i];
                }
            }
            long b;
            if (lc.blue > blue) {
                b = lc.blue;
            } else {
                b = blue;
            }
            return new LogicalClock(tmp, b);
        } finally {
            lc.readLock().unlock();
            readLock().unlock();
    //        System.out.println("maxclock unlocked");
           
        }

    }

    public void increment(int dcId) {
        lockForWrites();
        try {
            dcCount[dcId]++;
        } finally {
            writeLock().unlock();
        }
    }

    public void incrementBlue() {
        lockForWrites();
        try {
            blue++;
        } finally {
            writeLock().unlock();
        }
    }

    // returns true if the caller is <= than lc in all elements and < in at least one
    public boolean strictLessThan(LogicalClock lc) {
        lockForReads();
        lc.lockForReads();
        try {
            boolean res = blue <= lc.blue;
            boolean one = blue < lc.blue;
            for (int i = 0; res && i < dcCount.length; i++) {
                res = dcCount[i] <= lc.dcCount[i];
                one = one || dcCount[i] < lc.dcCount[i];
            }
            return res && one;
        } finally {
            lc.readLock().unlock();
            readLock().unlock();
        }
    }

    public boolean precedes(LogicalClock lc) {

        lockForReads();
        lc.lockForReads();
        try {
            boolean res = blue <= lc.blue;

            for (int i = 0; res && i < dcCount.length; i++) {
                res = dcCount[i] <= lc.dcCount[i];
            }
            return res;
        } finally {
            lc.readLock().unlock();
            readLock().unlock();
        }
    }

    public boolean precededBy(LogicalClock lc) {
        return lc.precedes(this);
    }

    // returns
    public boolean partialLessThan(LogicalClock lc) {
        lockForReads();
        lc.lockForReads();
        try {
            boolean res = blue > lc.blue;
            // res is true if this > blue
            for (int i = 0; i < dcCount.length && !res; i++) // loop 
            {
                res = dcCount[i] > lc.dcCount[i];
            }
            return !res;
        } finally {
            lc.readLock().unlock();
            readLock().unlock();
        }
    }
    // < dc_id, dc_id, blue >

    public boolean equals(LogicalClock lc) {
        lockForReads();
        lc.lockForReads();
        try {
            boolean res = blue == lc.blue && comparable(lc);
            for (int i = 0; res && i < dcCount.length; i++) {
                res = res && dcCount[i] == lc.dcCount[i];
            }
            return res;
        } finally {
            lc.readLock().unlock();
            readLock().unlock();
        }
    }

    /**
    returns true iff this is less than lc in at most 1 position and by at most 1.
    returns false otherwise
     **/
    public boolean lessThanByAtMostOne(LogicalClock lc) {
        lockForReads();
        lc.lockForReads();
        try {
            boolean res = comparable(lc);

            boolean one = false;
            for (int i = 0; res && i < dcCount.length; i++) {
                if (dcCount[i] < lc.dcCount[i]) {
                    if (dcCount[i] == lc.dcCount[i] - 1) {
                        if (!one) {
                            one = true;
                        } else {
                            res = false;
                        }
                    } else {
                        res = false;
                    }
                }
            }
            return res && one;
        } finally {
            lc.readLock().unlock();
            readLock().unlock();
        }

    }

    /**
    returns true if blue is >= lc.blue -1
     **/
    public boolean lessThanByAtMostOneBlue(LogicalClock lc) {
        lockForReads();
        lc.lockForReads();
        try {
            return blue + 1 >= lc.blue;

        } finally {
            lc.readLock().unlock();
            readLock().unlock();
        }
    }

    public String toString() {
        String tmp = "" + blue;
        for (int i = 0; i < dcCount.length; i++) {
            tmp += "-" + dcCount[i];
        }

        return tmp;
    }

    public int hashcode() {
        lockForReads();

        try {
            long sum = 0;
            for (int i = 0; i < dcCount.length; i++) {
                sum += (int) dcCount[i];
            }
            return (int) (sum * 1000 + blue);
        } finally {

            readLock().unlock();
        }

    }

    public Lock readLock() {
//        return lock.readLock();
        return lock;
    }

    private void lockForReads(){
        try{

            readLock().lock();
//            while (!readLock().tryLock(1, java.util.concurrent.TimeUnit.SECONDS)){
//        System.out.println("************\n**  could not get read lock on "+readLock()+
//                                       "\n** writelock is                "+writeLock()+
//                           "************");
//    }
        }catch(Exception e){
            e.printStackTrace();
            System.exit(-1);
            
        }    
    }
    
    private void lockForWrites(){
 //       System.out.println(Thread.currentThread()+" locking for writes "+writeLock());
        writeLock().lock();
    }
    
    public Lock writeLock() {
 //       System.out.println(Thread.currentThread()+" locking for reads "+readLock());
        //return lock.writeLock();
        return lock;
    }
}