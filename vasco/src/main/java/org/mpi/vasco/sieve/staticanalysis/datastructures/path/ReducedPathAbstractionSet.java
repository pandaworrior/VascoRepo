/********************************************************************
Copyright (c) 2013 chengli.
All rights reserved. This program and the accompanying materials
are made available under the terms of the GNU Public License v2.0
which accompanies this distribution, and is available at
http://www.gnu.org/licenses/old-licenses/gpl-2.0.html

Contributors:
    chengli - initial API and implementation

Contact:
    To distribute or use this code requires prior specific permission.
    In this case, please contact chengli@mpi-sws.org.
 ********************************************************************/
/**
 * 
 */
package org.mpi.vasco.sieve.staticanalysis.datastructures.path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.mpi.vasco.sieve.staticanalysis.datastructures.regularexpression.RegularExpressionDef;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class ReducedPathAbstractionSet.
 * It support concurrency.
 */
public class ReducedPathAbstractionSet {

	/** The reduced path ab set. */
	List<PathAbstraction> reducedPathAbSet;
	
	HashMap<String, PathAbstraction> noDuplicateReducedPathAbMap;
	
	/** The num of reduction in progress. 
	 * Once a reduction spawn, then the counter will be increased by 1;
	 * if a reduction finishes, then the counter will be decreased by 1;
	 * */
	AtomicInteger numOfReductionInProgress;
	
	/** The reduction completion monitor.
	 * When the reduction starts, the main thread
	 * to wait on this object,
	 * once a sub-reduction finishes, then it will update
	 * counter, when counter = 0, signal will be triggered.
	 *  */
	Object reductionCompletionMonitor;
	
	/** The Constant numOfThreads. */
	private final static int numOfThreads = 0;
	
	/** The executor to manage a thread pool. */
	ExecutorService executor;

	/**
	 * Instantiates a new reduced path abstraction set.
	 */
	public ReducedPathAbstractionSet() {
		this.reducedPathAbSet = new ArrayList<PathAbstraction>();
		this.noDuplicateReducedPathAbMap = new HashMap<String, PathAbstraction>();
		this.initializeReductionCounter();
		this.initializeThreadPool();
		this.initializeMonitor();
	}

	/**
	 * Instantiates a new reduced path abstraction set.
	 *
	 * @param pathAb the path ab
	 */
	public ReducedPathAbstractionSet(PathAbstraction pathAb) {
		this.reducedPathAbSet = new ArrayList<PathAbstraction>();
		this.noDuplicateReducedPathAbMap = new HashMap<String, PathAbstraction>();
		this.initializeReductionCounter();
		this.initializeThreadPool();
		this.initializeMonitor();
		// parse all reduced path
		this.setReducedPathAbstractionSet(pathAb);
	}

	/**
	 * Adds the reduce path abstraction.
	 *
	 * @param reducedPathAb the reduced path ab
	 */
	public void addReducePathAbstraction(PathAbstraction reducedPathAb) {
		//TODO: problem here
		/*String reducedPathAbStr = RegularExpressionDef
				.removeDuplicateOperators(reducedPathAb.toStringPlainText());
		for (PathAbstraction pathAb : this.reducedPathAbSet) {
			String pathAbStr = RegularExpressionDef
					.removeDuplicateOperators(pathAb.toStringPlainText());
			if (reducedPathAbStr.equals(pathAbStr)) {
				return;
			}
		}*/
		String pathStr = reducedPathAb.toStringWithNonOperatorsPlainText();
		if(!this.noDuplicateReducedPathAbMap.containsKey(pathStr)) {
			this.reducedPathAbSet.add(reducedPathAb);
			this.noDuplicateReducedPathAbMap.put(pathStr, reducedPathAb);
		}
	}

	/**
	 * Adds the reduced path abstraction set.
	 *
	 * @param rPathAbSet the r path ab set
	 */
	public void addReducedPathAbstractionSet(List<PathAbstraction> rPathAbSet) {
		for (PathAbstraction pathAb : rPathAbSet) {
			this.addReducePathAbstraction(pathAb);
		}
	}

	/**
	 * Sets the reduced path abstraction set.
	 *
	 * @param pathAb the new reduced path abstraction set
	 */
	private void setReducedPathAbstractionSet(PathAbstraction pathAb) {
		Debug.println("Reduce the path abstraction: "
				+ pathAb.toStringPlainText());
		//create a thread to work on this reduction
		this.incrementReductionCounter(1);
		this.createNewReductionJob(pathAb);
		
		//wait until the computation finishes
		synchronized(this.reductionCompletionMonitor) {
			try {
				this.reductionCompletionMonitor.wait();
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		this.shutDownExecutorService();
	}
	
	/**
	 * Creates the new reduction job.
	 *
	 * @param pathAb the path ab
	 */
	public void createNewReductionJob(PathAbstraction pathAb) {
		Runnable worker = new PathAbReducerThread(pathAb, this);
		if(this.executor.isShutdown()) {
			this.initializeThreadPool();
		}
		this.executor.execute(worker);
	}
	
	/**
	 * Sets the reduced path abstraction set.
	 * TODO: remove if the multi-threaded one works
	 * Depreciated one
	 * @param pathAb the new reduced path abstraction set
	 */
	public void setReducedPathAbstractionSetCopy(PathAbstraction pathAb) {
		Debug.println("Reduce the path abstraction: "
				+ pathAb.toStringPlainText());

		ReducedPathAbstractionSet processSet = new ReducedPathAbstractionSet();
		processSet.addReducePathAbstraction(pathAb);
		while (!processSet.isEmpty()) {
			PathAbstraction currentPathAb = processSet.remove(0);
			Debug.println("Current path ab: "
					+ currentPathAb.toStringPlainText());
			int operatorIndex = RegularExpressionDef
					.getFirstStarOperator(currentPathAb);
			if (operatorIndex == -1) {
				// if current regular expression is already * free
				this.addReducePathAbstraction(currentPathAb);
			} else {
				// if current regular expression contains *
				// then find the first * operator to perform reduction
				Debug.println("Handle star operator");
				int operandStartIndex = RegularExpressionDef
						.findIndexOfSubExpressionForStar(currentPathAb,
								operatorIndex);
				List<PathAbstraction> tempReducedPathAbList = RegularExpressionDef
						.reduceRegularExpressionWithStar(currentPathAb,
								operatorIndex, operandStartIndex);
				Debug.println("reduced to " + tempReducedPathAbList.size()
						+ " reduced regular expression");
				for (PathAbstraction reducedPathAb : tempReducedPathAbList) {
					Debug.println("added a reduced path abstraction into set");
					Debug.println(reducedPathAb.toStringPlainText());
					processSet.addReducePathAbstraction(reducedPathAb);
				}
			}
		}
		Debug.println("reduced to " + this.reducedPathAbSet.size());
		Debug.println("------------------------------");
		processSet.addReducedPathAbstractionSet(this.reducedPathAbSet);
		this.reducedPathAbSet.clear();
		while (!processSet.isEmpty()) {
			Debug.println("something left");
			PathAbstraction currentPathAb = processSet.remove(0);
			Debug.println("Current path abstraction: "
					+ currentPathAb.toStringPlainText());
			int operatorIndex = RegularExpressionDef
					.getTopUnionOperator(currentPathAb);
			if (operatorIndex == -1) {
				// if current regular expression is already + operator free,
				// then push this regexp into the output
				currentPathAb.removeBrackets();
				this.addReducePathAbstraction(currentPathAb);
			} else {
				// if current regular expression contains +
				// then find the first + operator to perform reduction
				List<PathAbstraction> tempReducedPathAbList = null;
				Debug.println("Handler union operator");
				int leftOperandIndex = RegularExpressionDef
						.findIndexOfLeftSubExpressionForUnion(currentPathAb,
								operatorIndex);
				int rightOperandIndex = RegularExpressionDef
						.findIndexOfRightSubExpressionForUnion(currentPathAb,
								operatorIndex);
				tempReducedPathAbList = RegularExpressionDef
						.reduceRegularExpressionWithUnion(currentPathAb,
								operatorIndex, leftOperandIndex,
								rightOperandIndex);
				for (PathAbstraction reducedPathAb : tempReducedPathAbList) {
					Debug.println("added a reduced path abstraction into set");
					Debug.println(reducedPathAb.toStringPlainText());
					processSet.addReducePathAbstraction(reducedPathAb);
				}
			}
		}
	}

	/**
	 * Removes the.
	 *
	 * @param index the index
	 * @return the path abstraction
	 */
	public PathAbstraction remove(int index) {
		return this.reducedPathAbSet.remove(index);
	}

	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		return this.reducedPathAbSet.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/**
	 * To string.
	 *
	 * @return the string
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String str = "";
		for (PathAbstraction pa : this.reducedPathAbSet) {
			str += pa.toString() + "\n";
		}
		return str;
	}

	/**
	 * To string plain text.
	 *
	 * @return the string
	 */
	public String toStringPlainText() {
		String str = "";
		/*for (PathAbstraction pa : this.reducedPathAbSet) {
			str += pa.toStringWithNonOperatorsPlainText() + "\n";
		}*/
		return str;
	}

	/**
	 * Prints the out.
	 */
	public void printOut() {
		Debug.println("Print out the reduced path abstraction set");
		if(Debug.debug) {
			for (PathAbstraction pa : this.reducedPathAbSet) {
				System.out.println(pa.toStringWithNonOperatorsPlainText());
			}
		}
	}
	
	/**
	 * Gets the reduced path abstraction set.
	 *
	 * @return the reduced path abstraction set
	 */
	public List<PathAbstraction> getReducedPathAbstractionSet(){
		return this.reducedPathAbSet;
	}
	
	/**
	 * Initialize reduction counter.
	 */
	private void initializeReductionCounter() {
		this.numOfReductionInProgress = new AtomicInteger();
	}
	
	/**
	 * Decrement reduction counter.
	 */
	public void decrementReductionCounter() {
		int count = this.numOfReductionInProgress.decrementAndGet();
		if(count == 0) {
			synchronized(this.reductionCompletionMonitor) {
				this.reductionCompletionMonitor.notify();
			}
		}
	}
	
	/**
	 * Increment reduction counter.
	 *
	 * @param numOfReduction the num of reduction
	 */
	public void incrementReductionCounter(int numOfReduction) {
		this.numOfReductionInProgress.addAndGet(numOfReduction);
	}
	
	/**
	 * Initialize thread pool.
	 */
	private void initializeThreadPool() {
		int newNumOfThreads = 1; //Runtime.getRuntime().availableProcessors() * 2;
		this.executor = Executors.newFixedThreadPool(newNumOfThreads);
	}
	
	/**
	 * Initialize thread pool.
	 *
	 * @param num the num
	 */
	private void initializeThreadPool(int num) {
		this.executor = Executors.newFixedThreadPool(numOfThreads);
	}
	
	/**
	 * Initialize monitor.
	 */
	private void initializeMonitor() {
		this.reductionCompletionMonitor = new Object();
	}
	
	/**
	 * Gets the reduced path abstraction count.
	 *
	 * @return the reduced path abstraction count
	 */
	public int getReducedPathAbstractionCount() {
		return this.getReducedPathAbstractionSet().size();
	}
	
	private void shutDownExecutorService() {
		this.executor.shutdown();
	}
	
}
