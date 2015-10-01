package org.mpi.vasco.util.debug;

public class PerProfile {
	class TimePair{
		long startTime;
		long endTime;
		
		public long getStartTime(){
			return this.startTime;
		}
		
		public long getEndTime(){
			return this.endTime;
		}
	}
	
	public static long totalTxn = 0;
	static long batchSizeForOutput = 1000;
	long numOfTxnDiscardBegin;
	long numOfTxnDiscardEnd;
	public static long startTime;
	public static long startTimeAtMeasure = -1;
	public static long endTimeAtMeasure;
	public static long startTimeForEveryBatch = -1;
	public static long endTimeForEveryBatch;
	
	public static long startTimePerTxn;
	public static long endTimePerTxn;
	
	public static long overallLatency = 0;
	public static long latencyPerBatch = 0;
	
	public static void startMeasure(){
		startTimePerTxn = System.nanoTime();
		if(startTimeAtMeasure == -1){
			startTimeAtMeasure = startTimePerTxn;
		}
		if(startTimeForEveryBatch == -1){
			startTimeForEveryBatch = startTimePerTxn;
		}
	}
	
	public static double computeThroughputCurrentBatch(){
		return (batchSizeForOutput*1.0)/((endTimeForEveryBatch - startTimeForEveryBatch)*0.000000001);
	}
	
	public static double computeAvgLatencyCurrentBatch(){
		return (latencyPerBatch * 0.000001)/(batchSizeForOutput * 1.0);
	}
	
	public static double computeThroughputOverall(){
		return (totalTxn*1.0)/((endTimeAtMeasure - startTimeAtMeasure)*0.000000001);
	}
	
	public static double computeAvgLatencyOverall(){
		return (overallLatency * 0.000001)/(totalTxn * 1.0);
	}
	
	public static void computeAndPrintBatchInfo(){
		double thputBatch = computeThroughputCurrentBatch();
		double avgLatenBatch = computeAvgLatencyCurrentBatch();
		double thputOverall = computeThroughputOverall();
		double avgLatencyOverall = computeAvgLatencyOverall();
		
		System.out.println("thput batch (txn/s) " + thputBatch + " batch avglaten (ms)" + avgLatenBatch +
				" thput all (txn/s) " + thputOverall + " avgLaten all (ms) " + avgLatencyOverall + "\n");
	}
	
	public static void endMeasure(){
		endTimePerTxn = System.nanoTime();
		
		long latency = endTimePerTxn - startTimePerTxn;
		overallLatency += latency;
		latencyPerBatch += latency;
		totalTxn++;
		if(totalTxn % batchSizeForOutput == 0){
			endTimeAtMeasure = endTimePerTxn;
			endTimeForEveryBatch = endTimePerTxn;
			//print here
			computeAndPrintBatchInfo();
			startTimeForEveryBatch = -1;
			latencyPerBatch = 0;
		}
	}
	
}
