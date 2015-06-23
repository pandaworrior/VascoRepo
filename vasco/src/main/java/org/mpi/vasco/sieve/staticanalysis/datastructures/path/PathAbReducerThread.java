/***************************************************************
Project name: georeplication
Class file name: PathAbReducerThread.java
Created at 10:43:31 PM by chengli

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
****************************************************************/

package org.mpi.vasco.sieve.staticanalysis.datastructures.path;

import java.util.List;

import org.mpi.vasco.sieve.staticanalysis.datastructures.path.PathAbstraction;
import org.mpi.vasco.sieve.staticanalysis.datastructures.path.ReducedPathAbstractionSet;
import org.mpi.vasco.sieve.staticanalysis.datastructures.regularexpression.RegularExpressionDef;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class PathAbReducerThread.
 *
 * @author chengli
 */
public class PathAbReducerThread implements Runnable{
	
	/** The path ab waiting for analyze. */
	PathAbstraction pathAb;
	
	/** The reduced path ab set. */
	ReducedPathAbstractionSet reducedPathAbSet;
	
	/**
	 * Instantiates a new path ab reducer thread.
	 *
	 * @param pathAb the path ab
	 * @param reducedPathAbSet the reduced path ab set
	 */
	public PathAbReducerThread(PathAbstraction pathAb,
			ReducedPathAbstractionSet reducedPathAbSet){
		this.setPathAb(pathAb);
		this.setReducedPathAbSet(reducedPathAbSet);
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 * here we run the path reduction algorithm
	 */
	@Override
	public void run() {
		//System.out.println(Thread.currentThread().getName() + " start to process " + pathAb.toStringPlainText());
		/* if this path abstraction doesn't have any special operator * and +
		 *remove all brackets and put it into the reduced 
		 */
		int operatorIndex = RegularExpressionDef.getFirstStarOperator(pathAb);
		if (operatorIndex != -1) {
			/* if current regular expression contains *
			 * then find the first * operator to perform reduction*/
			Debug.println("Handle star operator");
			int operandStartIndex = RegularExpressionDef .findIndexOfSubExpressionForStar(pathAb,
					operatorIndex);
			List<PathAbstraction> tempReducedPathAbList = RegularExpressionDef.
				reduceRegularExpressionWithStar(pathAb, operatorIndex, operandStartIndex);
			//System.out.println("reduced to " + tempReducedPathAbList.size()
			//		+ " reduced regular expression");
			/*for(PathAbstraction pa : tempReducedPathAbList) {
				System.out.println(pa.toStringPlainText());
			}*/
			this.reducedPathAbSet.incrementReductionCounter(tempReducedPathAbList.size());
			for (PathAbstraction reducedPathAb : tempReducedPathAbList) {
				Debug.println("added a reduced path abstraction into set");
				//Debug.println(reducedPathAb.toStringPlainText());
				this.reducedPathAbSet.createNewReductionJob(reducedPathAb);
			}
		}else{
			operatorIndex = RegularExpressionDef.getTopUnionOperator(pathAb);
			if (operatorIndex != -1) {
				/* if current regular expression contains +
		         * then find the first + operator to perform reduction*/
				List<PathAbstraction> tempReducedPathAbList = null;
				Debug.println("Handler union operator");
				int leftOperandIndex = RegularExpressionDef
					.findIndexOfLeftSubExpressionForUnion(pathAb, operatorIndex);
				int rightOperandIndex = RegularExpressionDef.
					findIndexOfRightSubExpressionForUnion(pathAb,
						operatorIndex);
				tempReducedPathAbList = RegularExpressionDef.reduceRegularExpressionWithUnion(pathAb,
						operatorIndex, leftOperandIndex, rightOperandIndex);
				/*System.out.println("original " + pathAb.toStringPlainText());
				for(PathAbstraction pa : tempReducedPathAbList) {
					System.out.println(pa.toStringPlainText());
				}*/
				this.reducedPathAbSet.incrementReductionCounter(tempReducedPathAbList.size());
				for (PathAbstraction reducedPathAb : tempReducedPathAbList) {
					Debug.println("added a reduced path abstraction into set");
					//Debug.println(reducedPathAb.toStringPlainText());
					this.reducedPathAbSet.createNewReductionJob(reducedPathAb);
				}
			}else {
				Debug.println("Now this path abstraction is done");
				synchronized(this.reducedPathAbSet){
					this.reducedPathAbSet.addReducePathAbstraction(pathAb);
				}
			}
		}
		this.reducedPathAbSet.decrementReductionCounter();
	}


	/**
	 * Gets the path ab.
	 *
	 * @return the pathAb
	 */
	public PathAbstraction getPathAb() {
		return pathAb;
	}


	/**
	 * Sets the path ab.
	 *
	 * @param pathAb the pathAb to set
	 */
	public void setPathAb(PathAbstraction pathAb) {
		this.pathAb = pathAb;
	}


	/**
	 * Gets the reduced path ab set.
	 *
	 * @return the reducedPathAbSet
	 */
	public ReducedPathAbstractionSet getReducedPathAbSet() {
		return reducedPathAbSet;
	}


	/**
	 * Sets the reduced path ab set.
	 *
	 * @param reducedPathAbSet the reducedPathAbSet to set
	 */
	public void setReducedPathAbSet(ReducedPathAbstractionSet reducedPathAbSet) {
		this.reducedPathAbSet = reducedPathAbSet;
	}

}
