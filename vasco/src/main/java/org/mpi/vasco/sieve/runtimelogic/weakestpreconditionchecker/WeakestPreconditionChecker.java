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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.ShadowOperationCreator;
import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation.DBOpEntry;
import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation.RuntimeFingerPrintGenerator;
import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation.ShadowOperation;
import org.mpi.vasco.sieve.runtimelogic.staticinformation.StaticFPtoWPsStore;
import org.mpi.vasco.sieve.staticanalysis.templatecreator.template.Operation;
import org.mpi.vasco.util.commonfunc.StringOperations;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.PrimitiveType;
import org.mpi.vasco.util.debug.Debug;
import org.mpi.vasco.util.weakestprecondtion.Formula;
import org.mpi.vasco.util.weakestprecondtion.WeakestPrecondition;

// TODO: Auto-generated Javadoc
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
	
	public static boolean evaluateWeakestPrecondition(ShadowOperationCreator shdOpCreator,
			ShadowOperation shdOp) {
		//get the runtime fingerprint
		//long startTime = System.nanoTime();
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
		}else {
			//evaluate this
			ArrayList<DBOpEntry> operationList = shdOp.getOperationList();
			for(int listIndex = 0; listIndex < operationList.size(); listIndex++) {
				DBOpEntry dbOp = operationList.get(listIndex);
				String populateFormulaStr = wp.populateAllFormulas(dbOp);
				if(populateFormulaStr.equals("true")) {
					continue;
				}else {
					if(populateFormulaStr.equals("false")) {
						//System.out.println("Latency to evaluate wp false is " + computeLatency(evalStartTime) + " for the number of entries " + runtimeFingerPrints.size());
						return false;
					}else {
						//System.out.println("try to evaluate " + populateFormulaStr);
						//if(!SimpleExpressionEvaluator.evalBoolExpression(populateFormulaStr)) {
						if(!SimpleInequalityExpressionEvaluator.evalBoolExpression(populateFormulaStr)) {
							Debug.println("The formula evaluation is false " + populateFormulaStr);
							//System.out.println("Latency to evaluate wp false is " + computeLatency(evalStartTime) + " for the number of entries " + runtimeFingerPrints.size());
							return false;
						}
					}
				}
			}
		}
		Debug.println("The formula evaluation is true");
		//System.out.println("Latency to evaluate wp true is " + computeLatency(evalStartTime) + " for the number of entries " + runtimeFingerPrints.size());
		return true;
	}
	
	/**
	 * Gets the color.
	 *
	 * @param shdOpCreator the shd op creator
	 * @param shdOp the shd op
	 * @return the color
	 */
	public static int getColor(ShadowOperationCreator shdOpCreator, ShadowOperation shdOp) {
		Debug.println("Ask the wp evaluation to evaluate the formulas");
		if(shdOp == null || shdOp.isEmpty())
			return 0;
		if(evaluateWeakestPrecondition(shdOpCreator, shdOp)) {
			Debug.println("WP evaluates to true, color is 0");
			return 0;
		}else {
			Debug.println("WP evaluates to false, color is 1");
			return 1;
		}
	}
	
	/*static boolean evaluateWeakestPrecondition() {
		return random.nextBoolean();
	}
	
	static public int getColor() {
		if(evaluateWeakestPrecondition()) {
			return 0;
		}else {
			return 1;
		}
	}*/

}
