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
package org.mpi.vasco.sieve.staticanalysis.pathanalyzer;

import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.expr.Expression;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.mpi.vasco.sieve.staticanalysis.codeparser.CodeNodeIdentifier;
import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGGraph;
import org.mpi.vasco.sieve.staticanalysis.datastructures.path.PathAbstraction;
import org.mpi.vasco.util.debug.Debug;

/**
 * The Class PathAnalyzer.
 * 
 * @author chengli
 */
public class PathAbstractionCreator {
	
	public static void printOutPathAbstractions(
			HashMap<CFGGraph<CodeNodeIdentifier, Expression>, PathAbstraction> pathAbMap) {
		Iterator<Entry<CFGGraph<CodeNodeIdentifier, Expression>, PathAbstraction>> it = pathAbMap.entrySet().iterator();
		while(it.hasNext()) {
			Entry<CFGGraph<CodeNodeIdentifier, Expression>, PathAbstraction> en = it.next();
			CFGGraph<CodeNodeIdentifier, Expression> cfg = en.getKey();
			PathAbstraction pathAb = en.getValue();
			System.out.println("method name: " + cfg.getCfgIdentifierPlainText() + " \n pathAb: " + pathAb.toStringPlainText());
		}
	}

	/**
	 * Get path abstraction from control flow graph.
	 * 
	 * @param classMethodCFGMappings
	 *            the class method cfg mappings
	 * @return the hash map
	 */
	public static HashMap<CFGGraph<CodeNodeIdentifier, Expression>, PathAbstraction> obtainAllPathAbstraction(
			HashMap<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>> classMethodCFGMappings) {
		HashMap<CFGGraph<CodeNodeIdentifier, Expression>, PathAbstraction> cfgPathAbMapping = new HashMap<CFGGraph<CodeNodeIdentifier, Expression>, PathAbstraction>();
		Iterator<Entry<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>>> outerIt = classMethodCFGMappings
				.entrySet().iterator();
		while (outerIt.hasNext()) {
			Entry<ClassOrInterfaceDeclaration, HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>> classMap = outerIt
					.next();
			Iterator<Entry<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>> it = classMap
					.getValue().entrySet().iterator();
			while (it.hasNext()) {
				Entry<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>> pair = (Entry<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>) it
						.next();
				CFGGraph<CodeNodeIdentifier, Expression> cfg1 = pair.getValue();
				cfg1.printOut();
				PathAbstraction pathAb = new PathAbstraction(cfg1);
				pathAb.printOut();
				cfgPathAbMapping.put(cfg1, pathAb);
			}
		}
		return cfgPathAbMapping;
	}
	
	/**
	 * Obtain all path abstraction for whole project.
	 *
	 * @param CFGMappings the cFG mappings
	 * @return the hash map
	 */
	public static HashMap<CFGGraph<CodeNodeIdentifier, Expression>, PathAbstraction> obtainAllPathAbstractionForWholeProject(
			HashMap<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>> CFGMappings) {
		float latency = 0;
		String finalOutput = "";
		int count = 0;
		HashMap<CFGGraph<CodeNodeIdentifier, Expression>, PathAbstraction> cfgPathAbMapping = new HashMap<CFGGraph<CodeNodeIdentifier, Expression>, PathAbstraction>();
		Iterator<Entry<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>> it = CFGMappings.entrySet().iterator();
		while (it.hasNext()) {
			Entry<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>> pair = (Entry<MethodDeclaration, CFGGraph<CodeNodeIdentifier, Expression>>) it
					.next();
			CFGGraph<CodeNodeIdentifier, Expression> cfg1 = pair.getValue();
			//cfg1.printOut();
			Debug.println("method: " + cfg1.getCfgIdentifierPlainText());
			if(PathAnalyzer.isThisFunctionMustBeProcessed(cfg1.getCfgIdentifier().getClassName(), 
					cfg1.getCfgIdentifier().getMethodName(), 
					cfg1.getCfgIdentifier().getBeginLine())) {
				//cfg1.printOut();
				long eachStartTs = System.nanoTime();
				PathAbstraction pathAb = new PathAbstraction(cfg1);
				Debug.println(" original path abstraction \n" + pathAb.toStringPlainText());
				long eachEndTs = System.nanoTime();
				float eachLatency = (float) ((eachEndTs - eachStartTs) / 1000000);
				//System.out.println("Time for extracting path of method " + pair.getKey().getName() + " is (ms) " + eachLatency);
				finalOutput += cfg1.getCfgIdentifierPlainText() + " " +eachLatency + "\n";
				latency += eachLatency;
				//pathAb.printOutInPlainText();
				cfgPathAbMapping.put(cfg1, pathAb);
				count++;
			}else {
				Debug.println("This method is filtered out, no process " + cfg1.getCfgIdentifierPlainText());
			}
		}
		//System.out.println(finalOutput);
		//System.out.println("Total time for extracting path for " +cfgPathAbMapping.size()+ " methods is (ms) " + latency);
		return cfgPathAbMapping;
	}
	
}
