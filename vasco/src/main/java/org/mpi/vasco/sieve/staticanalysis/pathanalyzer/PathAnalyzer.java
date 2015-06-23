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

import japa.parser.ast.expr.Expression;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mpi.vasco.sieve.staticanalysis.codeparser.CodeNodeIdentifier;
import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGEdge;
import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGGraph;
import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGNode;
import org.mpi.vasco.sieve.staticanalysis.datastructures.path.PathAbstraction;
import org.mpi.vasco.sieve.staticanalysis.datastructures.path.ReducedPathAbstractionSet;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class PathAnalyzer.
 * 
 * @author chengli
 */
public class PathAnalyzer {
	
	/** The function filter. All
	 * functions in this filter must be processed */
	static HashSet<String> functionMustBeProcessedList = null;
	

	/**
	 * Sets the function must be processed list.
	 *
	 * @param fM the f m
	 */
	public static void setFunctionMustBeProcessedList(HashSet<String> fM) {
		functionMustBeProcessedList = fM;
	}
	
	/**
	 * Adds the function must be processed list from file.
	 *
	 * @param filePath the file path
	 */
	public static void addFunctionMustBeProcessedListFromFile(String filePath) {
		BufferedReader br;
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(filePath)));
			while ((line = br.readLine()) != null) {
				if(!line.equals("")) {
					String[] tuple = line.split(" ");
					if(tuple.length != 3) {
						System.out.println("The file of transactions that must be processed contains a line with wrong format");
						System.exit(-1);
					}else {
						addMustBeProcessedFunction(tuple[0], tuple[1], Integer.parseInt(tuple[2]));
					}
				}else {
					System.out.println("The file of transactions that must be processed contains an empty line");
				}
			}
			br.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static String assembleTransactionIdStr(String cName, String fName, int bLine) {
		return cName + "-" + fName + "-" + bLine;
	}
	
	/**
	 * Adds the function to filter.
	 *
	 * @param functionName the function name
	 * @param beginLine the begin line
	 */
	public static void addMustBeProcessedFunction(String cName, String fName, int bLine) {
		if(functionMustBeProcessedList == null) {
			functionMustBeProcessedList = new HashSet<String>();
		}
		String transactionIdentifier = assembleTransactionIdStr(cName, fName, bLine);
		functionMustBeProcessedList.add(transactionIdentifier);
	}
	
	/**
	 * Checks if is filter out.
	 *
	 * @param functionName the function name
	 * @param beginLine the begin line
	 * @return true, if is filter out
	 */
	public static boolean isThisFunctionMustBeProcessed(String className, String functionName, int beginLine) {
		if(functionMustBeProcessedList == null) {
			return true;
		}
		String txnIdStr = assembleTransactionIdStr(className, functionName, beginLine);
		if(functionMustBeProcessedList.contains(txnIdStr)) {
			return true;
		}else {
			return false;
		}
	}

	/**
	 * Get reduced path abstraction set for each path abstraction.
	 * 
	 * @param cfgPathAbMapping
	 *            the cfg to path ab mapping
	 * @return the hash map
	 */
	public static HashMap<CFGGraph<CodeNodeIdentifier, Expression>, ReducedPathAbstractionSet> obtainAllReducePathAbstractions(
			HashMap<CFGGraph<CodeNodeIdentifier, Expression>, PathAbstraction> cfgPathAbMapping) {
		HashMap<CFGGraph<CodeNodeIdentifier, Expression>, ReducedPathAbstractionSet> cfgReducedPathAbMapping = new HashMap<CFGGraph<CodeNodeIdentifier, Expression>, ReducedPathAbstractionSet>();
		Iterator<Entry<CFGGraph<CodeNodeIdentifier, Expression>, PathAbstraction>> it = cfgPathAbMapping
				.entrySet().iterator();
		float latency = 0;
		String finalOutput = "";
		int totalReducedPath = 0;
		int numOfFunctionMustBeProcessed = 0;
		while (it.hasNext()) {
			Entry<CFGGraph<CodeNodeIdentifier, Expression>, PathAbstraction> cfgPathAbEntry = it
					.next();
			CFGGraph<CodeNodeIdentifier, Expression> cfg = cfgPathAbEntry
					.getKey();
			if(PathAnalyzer.isThisFunctionMustBeProcessed(cfg.getCfgIdentifier().getClassName(), 
					cfg.getCfgIdentifier().getMethodName(), cfg.getCfgIdentifier().getBeginLine())) {
				PathAbstraction pathAb = cfgPathAbEntry.getValue();
				long eachStartTs = System.nanoTime();
				ReducedPathAbstractionSet reducePathAbSet = new ReducedPathAbstractionSet(
						pathAb);
				long eachEndTs = System.nanoTime();
				float eachLatency = (float)((eachEndTs - eachStartTs) / 1000000);
				latency += eachLatency;
				finalOutput += cfg.getCfgIdentifierPlainText() + " " + eachLatency + " " +
					reducePathAbSet.getReducedPathAbstractionCount()+ "\n";
				totalReducedPath += reducePathAbSet.getReducedPathAbstractionCount();
				//System.out.println("method : " + cfg.getMethodNameAndLocation());
				//System.out.println("original " + pathAb.toString());
				//System.out.println("plain " + pathAb.toStringPlainText());
				//System.out.println("reduced to " + reducePathAbSet.getReducedPathAbstractionCount());
				//reducePathAbSet.printOut();
				cfgReducedPathAbMapping.put(cfg, reducePathAbSet);
				numOfFunctionMustBeProcessed++;
			}else {
				Debug.println("This method is filtered out, no process " + cfg.getCfgIdentifierPlainText());
			}
		}
		finalOutput += "Total time to reduce " + cfgPathAbMapping.size() + " regx must be processed " +numOfFunctionMustBeProcessed+ " to "+  totalReducedPath +" is " + latency;
		System.out.println(finalOutput);
		return cfgReducedPathAbMapping;
	}
	
	/**
	 * Obtain reduced control flow graph corresponding to
	 * a particular reduced regular expression.
	 *
	 * @param cfgGraph the cfg graph
	 * @param reducedPathAb the reduced path ab
	 * @return the cFG graph
	 */
	public static CFGGraph<CodeNodeIdentifier, Expression> obtainReducedControlFlowGraph(CFGGraph<CodeNodeIdentifier, Expression> cfgGraph, PathAbstraction reducedPathAb){
		CFGGraph<CodeNodeIdentifier, Expression> canonicalCfgGraph = new CFGGraph<CodeNodeIdentifier, Expression>();
		canonicalCfgGraph.setCfgIdentifier(cfgGraph.getCfgIdentifier());
		List<Integer> nonOperatorEleList = reducedPathAb.getAllNonOperatorElements();
		Debug.println("-------->");
		//System.out.println("reduced path " + reducedPathAb.toStringWithNonOperatorsPlainText());
		CFGNode<CodeNodeIdentifier, Expression> parentNode = null;
		for(int i = 0; i < nonOperatorEleList.size(); i++){
			int en = nonOperatorEleList.get(i);
			Debug.println("edgeId: " + en);
			CFGEdge<CodeNodeIdentifier, Expression> edge = cfgGraph.getEdge(en);
			CFGNode<CodeNodeIdentifier, Expression> source = edge.getSource();
			CFGNode<CodeNodeIdentifier, Expression> destination = edge.getDestination();
			
			CFGNode<CodeNodeIdentifier, Expression> newSource = null ;//canonicalCfgGraph.getNodeByUniqueSequence(source.getSequenceId());
			CFGNode<CodeNodeIdentifier, Expression> newDestination = destination.cfgNodeClone(); //canonicalCfgGraph.getNodeByUniqueSequence(destination.getSequenceId());
			if(parentNode == null) {
				newSource = source.cfgNodeClone();
				canonicalCfgGraph.addEdge(newSource, newDestination);
			}else {
				//already has nodes inserted
				//check the parentNode has the same id of the current source, if so, add an edge from parentNode to the new destination
				if(source.getSequenceId() == parentNode.getSequenceId()) {
					canonicalCfgGraph.addEdge(parentNode, newDestination);
				}else {
					Debug.println("you cannot find dismatch which means that you have if and else");
					newSource = source.cfgNodeClone();
					canonicalCfgGraph.addEdge(parentNode, newSource);
					canonicalCfgGraph.addEdge(newSource, newDestination);
				}
			}
			if(i == 0){
				canonicalCfgGraph.addEntryNode(newSource);
			}
			if(i == nonOperatorEleList.size() - 1){
				canonicalCfgGraph.addReturnNode(newDestination);
			}
			
			parentNode = newDestination;
		}
		Debug.println("<--------");
		//canonicalCfgGraph.printOut();
		return canonicalCfgGraph;
	}
	
	/**
	 * Obtain all reduced control flow graphs.
	 *
	 * @param cfgGraph the cfg graph
	 * @param rPathAbSet the r path ab set
	 * @return the list
	 */
	public static List<CFGGraph<CodeNodeIdentifier, Expression>> obtainAllReducedControlFlowGraphs(CFGGraph<CodeNodeIdentifier, Expression> cfgGraph, ReducedPathAbstractionSet rPathAbSet){
		List<PathAbstraction> reducedPathAbList = rPathAbSet.getReducedPathAbstractionSet();
		List<CFGGraph<CodeNodeIdentifier, Expression>> reducedCfgList = new ArrayList<CFGGraph<CodeNodeIdentifier, Expression>>();
		for(PathAbstraction reducedPathAb : reducedPathAbList){
			reducedCfgList.add(PathAnalyzer.obtainReducedControlFlowGraph(cfgGraph, reducedPathAb));
		}
		return reducedCfgList;
	}
	
	/**
	 * Obtain all cfg to reduced cfg mappings.
	 *
	 * @param cfgPathAbMapping the cfg path ab mapping
	 * @return the hash map
	 */
	public static HashMap<CFGGraph<CodeNodeIdentifier, Expression>, List<CFGGraph<CodeNodeIdentifier, Expression>>> obtainAllCfgToReducedCfgMappings(
			HashMap<CFGGraph<CodeNodeIdentifier, Expression>, ReducedPathAbstractionSet> cfgPathAbMapping){
		
		HashMap<CFGGraph<CodeNodeIdentifier, Expression>, List<CFGGraph<CodeNodeIdentifier, Expression>>> cfgToReducedMapping = new
				HashMap<CFGGraph<CodeNodeIdentifier, Expression>, List<CFGGraph<CodeNodeIdentifier, Expression>>>();
		Iterator<Entry<CFGGraph<CodeNodeIdentifier, Expression>, ReducedPathAbstractionSet>> it = cfgPathAbMapping.entrySet().iterator();
		while(it.hasNext()){
			Entry<CFGGraph<CodeNodeIdentifier, Expression>, ReducedPathAbstractionSet> itEntry = it.next();
			CFGGraph<CodeNodeIdentifier, Expression> cfg = itEntry.getKey();
			ReducedPathAbstractionSet rPathAbSet = itEntry.getValue();
			List<CFGGraph<CodeNodeIdentifier, Expression>> reducedCfgList = obtainAllReducedControlFlowGraphs(cfg, rPathAbSet);
			cfgToReducedMapping.put(cfg, reducedCfgList);
		}
		return cfgToReducedMapping;
	}
	
	
	/**
	 * Obtain all reduced cfgs.
	 *
	 * @param cfgPathAbMapping the cfg path ab mapping
	 * @return the list
	 */
	public static List<CFGGraph<CodeNodeIdentifier, Expression>> obtainAllReducedCfgGraphs(
			HashMap<CFGGraph<CodeNodeIdentifier, Expression>, ReducedPathAbstractionSet> cfgPathAbMapping){
		
		List<CFGGraph<CodeNodeIdentifier, Expression>> reducedCfgGraphs = new
				ArrayList<CFGGraph<CodeNodeIdentifier, Expression>>();
		Iterator<Entry<CFGGraph<CodeNodeIdentifier, Expression>, ReducedPathAbstractionSet>> it = cfgPathAbMapping.entrySet().iterator();
		while(it.hasNext()){
			Entry<CFGGraph<CodeNodeIdentifier, Expression>, ReducedPathAbstractionSet> itEntry = it.next();
			CFGGraph<CodeNodeIdentifier, Expression> cfg = itEntry.getKey();
			ReducedPathAbstractionSet rPathAbSet = itEntry.getValue();
			List<CFGGraph<CodeNodeIdentifier, Expression>> reducedCfgList = obtainAllReducedControlFlowGraphs(cfg, rPathAbSet);
			reducedCfgGraphs.addAll(reducedCfgList);
		}
		return reducedCfgGraphs;
	}
	
     
}
