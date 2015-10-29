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
 * This class is to check the weakest precondition for
 * any runtime generated shadow operation instance.
 * It takes a shadow operation instance as input, then
 * it will match this shadow operation to the correct
 * template to get a weakest precondition. Finally
 * it substitute the formulas in the weakest precondition
 * with the concrete values, and evaluate the true or false
 * value of the materialized formulas.
 */
package org.mpi.vasco.sieve.runtimelogic.weakestpreconditionchecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.ShadowOperationCreator;
import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation.DBOpEntry;
import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation.ShadowOperation;
import org.mpi.vasco.sieve.runtimelogic.staticinformation.StaticFPtoWPsStore;
import org.mpi.vasco.util.commonfunc.StringOperations;
import org.mpi.vasco.util.debug.Debug;
import org.mpi.vasco.util.weakestprecondtion.WeakestPrecondition;

/**
 * The Class WeakestPreconditionChecker.
 *
 * @author chengli
 */
public class WeakestPreconditionChecker {
	
	/** The mapping from f pto wp. */
	static StaticFPtoWPsStore mappingFromFPtoWP;
	
	/** The random. */
	static Random random = new Random();
	
	static String nonConflictOpName = "nonconflictop";
	
	/**
	 * Sets the static f pto w ps store.
	 *
	 * @param sFpWpStore the new static f pto w ps store
	 */
	static public void setStaticFPtoWPsStore(StaticFPtoWPsStore sFpWpStore){
		Debug.println("set weakestpreconditions to WP checker");
		mappingFromFPtoWP = sFpWpStore;
	}
	
	public static double computeLatency(long startTime) {
		long endTime = System.nanoTime();
		double latency = (endTime - startTime) * 0.000001;
		return latency;
	}
	
	/**
	 * Evaluate weakest precondition.
	 *
	 * @param shdOpCreator the shd op creator
	 * @param shdOp the shd op
	 * @return true, if successful
	 */
	/*static boolean evaluateWeakestPrecondition(List<Operation> opList, List<PrimitiveType> crdtObjs){
		//first find the corresponding wp
		WeakestPrecondition wp = mappingFromFPtoWP.fetchWeakestPreconditionByGivenSequenceOfOperations(opList);
		assert(wp != null);
		//populate the formula with concrete values
		List<Formula> formList = wp.getFormulaList();
		for(int i = 0; i < formList.size(); i++){
			Formula f = formList.get(i);
			for(int j = 0; j < crdtObjs.size(); j++){
				PrimitiveType pt = crdtObjs.get(j);
				if(f.isCrdtObjTouchThisFormula(pt)){
					//replace value 
					String formWithValueStr = f.populateLeftOperandToString(pt.getDataName()); //TODO: it has to be modified
					//evaluate the value
					if(!SimpleExpressionEvaluator.evalBoolExpression(formWithValueStr)){
						return false;
					}
				}
			
			}
		}
		return true;
	}*/
	
	public static WeakestPrecondition getWeakestPrecondition(ShadowOperationCreator shdOpCreator,
			ShadowOperation shdOp){
		if(shdOp == null || shdOp.isEmpty()){
			return null;
		}
		List<String> runtimeFingerPrints = shdOpCreator.getFpGenerator().computeFingerPrint(shdOp);
		//System.out.println("Latency to generate fps is: " + computeLatency(startTime) + " for the number of entries " + runtimeFingerPrints.size());
		//long fetchStartTime = System.nanoTime();
		WeakestPrecondition wp = mappingFromFPtoWP.fetchWeakestPreconditionByGivenSequenceOfOperations(runtimeFingerPrints);
		//System.out.println("Latency to fetch wp is: " + computeLatency(fetchStartTime) + " for the number of entries " + runtimeFingerPrints.size());
		//long evalStartTime = System.nanoTime();
		if(wp == null) {
			String errorStr = "\n shadow operation created: \n" +shdOp.toString();
			errorStr += "\n fingerprint generated: \n" + StringOperations.concatStringSplitByDot(runtimeFingerPrints);
			System.out.println(errorStr);
			throw new RuntimeException("You cannot find a wp!");
		}else{
			return wp;
		}
	}
	
	public static boolean evaluateWeakestPrecondition(WeakestPrecondition wp,
			ShadowOperation shdOp) {
		// evaluate this
		ArrayList<DBOpEntry> operationList = shdOp.getOperationList();
		for (int listIndex = 0; listIndex < operationList.size(); listIndex++) {
			DBOpEntry dbOp = operationList.get(listIndex);
			String populateFormulaStr = wp.populateAllFormulas(dbOp);
			if (populateFormulaStr.equals("true")) {
				continue;
			} else {
				if (populateFormulaStr.equals("false")) {
					// System.out.println("Latency to evaluate wp false is " +
					// computeLatency(evalStartTime) +
					// " for the number of entries " +
					// runtimeFingerPrints.size());
					return false;
				} else {
					// System.out.println("try to evaluate " +
					// populateFormulaStr);
					// if(!SimpleExpressionEvaluator.evalBoolExpression(populateFormulaStr))
					// {
					if (!SimpleInequalityExpressionEvaluator
							.evalBoolExpression(populateFormulaStr)) {
						Debug.println("The formula evaluation is false "
								+ populateFormulaStr);
						// System.out.println("Latency to evaluate wp false is "
						// + computeLatency(evalStartTime) +
						// " for the number of entries " +
						// runtimeFingerPrints.size());
						return false;
					}
				}
			}
		}
		Debug.println("The formula evaluation is true");
		// System.out.println("Latency to evaluate wp true is " +
		// computeLatency(evalStartTime) + " for the number of entries " +
		// runtimeFingerPrints.size());
		return true;
	}
	
	/**
	 * Gets the color.
	 *
	 * @param shdOpCreator the shd op creator
	 * @param shdOp the shd op
	 * @return the color
	 */
	public static int isCoordinationNeeded(WeakestPrecondition wp, ShadowOperation shdOp) {
		Debug.println("Ask the wp evaluation to evaluate the formulas");
		if(wp != null){
			if(!evaluateWeakestPrecondition(wp, shdOp)){
				Debug.println("WP evaluates to false");
				return 1;
			}
		}
		Debug.println("WP evaluates to true");
		return 0;
		/*if(evaluateWeakestPrecondition(wp, shdOp)) {
			Debug.println("WP evaluates to true");
			return 0;
		}else {
			Debug.println("WP evaluates to false");
			return 1;
		}*/
	}
	
	public static String getShadowOpName(ShadowOperationCreator shdOpCreator, ShadowOperation shdOp){
		WeakestPrecondition wp = getWeakestPrecondition(shdOpCreator, shdOp);
		int result = isCoordinationNeeded(wp, shdOp);
		if(result == 0){
			return nonConflictOpName;
		}else{
			return wp.getSimplifiedOpName();
		}
	}

}
