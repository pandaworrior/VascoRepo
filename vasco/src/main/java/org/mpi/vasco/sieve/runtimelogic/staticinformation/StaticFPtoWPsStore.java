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
 * The Class StaticFingerPrintStore is used to load
 * all static finger prints and their corresponding
 * template weakest preconditions. It also serve as
 * a search engine for finding the correct set of
 * formulas based on the runtime generated sequence of
 * crdt operations. Assume the output from the static part
 * is a list of entries, each entry has a pair of 
 * <signature, weakestprecondition>.
 */
package org.mpi.vasco.sieve.runtimelogic.staticinformation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mpi.vasco.sieve.staticanalysis.templatecreator.StaticFingerPrintGenerator;
import org.mpi.vasco.util.debug.Debug;
import org.mpi.vasco.util.weakestprecondtion.WeakestPrecondition;

// TODO: Auto-generated Javadoc
/**
 * The Class StaticFPtoWPsStore.
 */
public class StaticFPtoWPsStore {
	
	/** The static analysis outputfile full path. */
	String staticAnalysisOutputfileFullPath = "";
	
	/** The root. */
	StaticFPtoWPsStoreNode root;
	
	/** The opener of finger print. */
	static String openerOfFingerPrint = "fingerprint:";
	
	/** The opener of weakest precondition. */
	public static String openerOfWeakestPrecondition = "weakestprecondition:";
	
	public static String openerOfSimplifiedName = "simplifiedname:";
	
	/** The node id counter. */
	int nodeIdCounter = 0;
	
	/** The num of wp node. */
	int numOfWPNode = 0;
	
	/**
	 * Instantiates a new static f pto w ps store.
	 *
	 * @param staticAnalysisOutputfileFullPath the static analysis outputfile full path
	 */
	public StaticFPtoWPsStore(String staticAnalysisOutputfileFullPath){
		Debug.println("create wp store");
		this.setStaticAnalysisOutputfileFullPath(staticAnalysisOutputfileFullPath);
		root = new StaticFPtoWPsStoreNode(getNextNodeId(), "root");
		root.setRoot(true);
	}

	/**
	 * Gets the static analysis outputfile full path.
	 *
	 * @return the static analysis outputfile full path
	 */
	public String getStaticAnalysisOutputfileFullPath() {
		return staticAnalysisOutputfileFullPath;
	}

	/**
	 * Sets the static analysis outputfile full path.
	 *
	 * @param staticAnalysisOutputfileFullPath the new static analysis outputfile full path
	 */
	public void setStaticAnalysisOutputfileFullPath(
			String staticAnalysisOutputfileFullPath) {
		this.staticAnalysisOutputfileFullPath = staticAnalysisOutputfileFullPath;
	}

	/**
	 * Gets the root.
	 *
	 * @return the root
	 */
	public StaticFPtoWPsStoreNode getRoot() {
		return root;
	}

	/**
	 * Sets the root.
	 *
	 * @param root the new root
	 */
	public void setRoot(StaticFPtoWPsStoreNode root) {
		this.root = root;
	}
	
	/**
	 * Gets the next node id.
	 *
	 * @return the next node id
	 */
	public int getNextNodeId() {
		return this.nodeIdCounter++;
	}
	
	/**
	 * Load all static output.
	 * The format of the doc is that:
	 * numOfLines is even.
	 * Every line with the even number
	 * and its succeeding line form a pair.
	 * The first element is started with "fingerprint:"
	 * The second element is started with "weakestprecondition:"
	 */
	public void loadAllStaticOutput(){
		//open file read all out
		BufferedReader br;
		List<String> contentLines = new ArrayList<String>();
		try {
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(this.getStaticAnalysisOutputfileFullPath())));
			String line;
			while ((line = br.readLine()) != null) {
				contentLines.add(line);
			}
			br.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assert(contentLines.size() % 3 == 0);
		Debug.println("receive input size is " +contentLines.size()/3);
		for(int i = 0; i < contentLines.size(); i = i+3){
			String fpStr = contentLines.get(i);
			assert(fpStr.startsWith(openerOfFingerPrint));
			String wpStr = contentLines.get(i+1);
			assert(wpStr.startsWith(openerOfWeakestPrecondition));
			//add this info to store
			String pureFpStr = fpStr.substring(openerOfFingerPrint.length());
			String[] fingerPrints = null;
			if(pureFpStr.indexOf(StaticFingerPrintGenerator.fingerPrintDelimiter) != -1) {
				fingerPrints = pureFpStr.split(StaticFingerPrintGenerator.escapeFingerPrintDelimiter);
			}else {
				fingerPrints = new String[1];
				fingerPrints[0] = pureFpStr;
			}
			String pureWpStr = wpStr.substring(openerOfWeakestPrecondition.length());
			String simplifiedOpName = contentLines.get(i+2).substring(openerOfSimplifiedName.length());
			this.addStaticFPtoWPsStoreNode( fingerPrints, 
					new WeakestPrecondition(pureWpStr, simplifiedOpName));
		}
		
		Debug.println("generated wp node number " + this.numOfWPNode);
	}
	
	/**
	 * Creates the chain of nodes.
	 *
	 * @param fingerPrintChain the finger print chain
	 * @param beginIndex the begin index
	 * @param wp the wp
	 * @return the static f pto w ps store node
	 */
	public StaticFPtoWPsStoreNode createChainOfNodes(String[] fingerPrintChain, int beginIndex, WeakestPrecondition wp){
		StaticFPtoWPsStoreNode firstNode = null;
		StaticFPtoWPsStoreNode parentNode = null;
		for(int i = beginIndex; i < fingerPrintChain.length; i++){
			StaticFPtoWPsStoreNode currentNode = new StaticFPtoWPsStoreNode(getNextNodeId(), fingerPrintChain[i]);
			if( i == beginIndex){
				firstNode = currentNode;
			}
			if(parentNode != null){
				parentNode.addOneChildNode(currentNode);
			}
			parentNode = currentNode;
		}
		assert(parentNode != null);
		parentNode.setWeakestPrecondition(wp);
		numOfWPNode++;
		return firstNode;
	}
	
	/**
	 * Adds the static f pto w ps store node.
	 *
	 * @param fingerPrintChain the finger print chain
	 * @param wp the wp
	 */
	public void addStaticFPtoWPsStoreNode(String[] fingerPrintChain, WeakestPrecondition wp){
		Debug.println("Trying to add a wp node");
		StaticFPtoWPsStoreNode current = root;
		boolean isNew = false;
		for(int i = 0; i < fingerPrintChain.length; i++){
			String singleFP = fingerPrintChain[i];
			StaticFPtoWPsStoreNode findMatchChildNode = current.findMatchingChildNode(singleFP);
			if(findMatchChildNode != null){
				Debug.println("find a node, please go to the next");
				current = findMatchChildNode;
				continue;
			}else {
				Debug.println("cannot find the next one node, creating a node and add to it");
				StaticFPtoWPsStoreNode firstNodeOfChain = this.createChainOfNodes(fingerPrintChain, i, wp);
				current.addOneChildNode(firstNodeOfChain);
				isNew = true;
				break;
			}
		}
		
		if(isNew == false) {
			Debug.println("perhaps the path is already injected, please check");
			WeakestPrecondition oldWp = this.fetchWeakestPreconditionByGivenSequenceOfOperations(
					new ArrayList<String>(Arrays.asList(fingerPrintChain)));
			if(oldWp == null) {
				Debug.println(" this path is injected, but wp is not set");
				current.setWeakestPrecondition(wp);
				this.numOfWPNode++;
			}else {
				if(wp.equalTo(oldWp)) {
					Debug.println("These two wps are equal");
				}else {
					System.out.println("These two wps are not equal old one : " + oldWp.toString() + " new one: " + wp.toString());
					System.exit(-1);
				}
			}
		}
	}
	
	/**
	 * Fetch weakest precondition by given sequence of operations.
	 *
	 * @param prefix the prefix
	 * @param subSignature the sub signature
	 * @return the weakest precondition
	 */
	/*public WeakestPrecondition fetchWeakestPreconditionByGivenSequenceOfOperations(List<Operation> opList){
		assert(opList.size() > 0);
		WeakestPrecondition wp = null;
		StaticFPtoWPsStoreNode current = root;
		int i = 0;
		for(; i < opList.size(); i++){
			StaticFPtoWPsStoreNode findMatchChildNode = current.findMatchingChildNode(opList.get(i).getOperationFingerPrint());
			if(findMatchChildNode == null){
				return wp;
			}
			current = findMatchChildNode;
		}
		wp = current.getWeakestPrecondition();
		return wp;
	}*/
	
	public StaticFPtoWPsStoreNode findNodeBackward(Map<Integer, StaticFPtoWPsStoreNode> prefix,
			String subSignature) {
		StaticFPtoWPsStoreNode swpNode = null;
		Iterator<Entry<Integer, StaticFPtoWPsStoreNode>> it = prefix.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Integer, StaticFPtoWPsStoreNode> entry = it.next();
			String nodeSignature = entry.getValue().getOpFingerPrint();
			if(subSignature.equals(nodeSignature)) {
				swpNode = entry.getValue();
				break;
			}
		}
		return swpNode;
	}
	
	/**
	 * Fetch weakest precondition by given sequence of operations.
	 *
	 * @param signatureList the signature list
	 * @return the weakest precondition
	 */
	public WeakestPrecondition fetchWeakestPreconditionByGivenSequenceOfOperations(List<String> signatureList){
		assert(signatureList.size() > 0);
		WeakestPrecondition wp = null;
		StaticFPtoWPsStoreNode current = root;
		StaticFPtoWPsStoreNode deepestNodeVisited = null;
		
		Map<Integer, StaticFPtoWPsStoreNode> prefix = Collections.synchronizedMap(
				  new LinkedHashMap<Integer, StaticFPtoWPsStoreNode>());
		Iterator<String> it = signatureList.iterator();
		
		boolean isFoundInPrefix = true;
		boolean isFoundAlongPath = true;
		while(it.hasNext()){
			if(!isFoundInPrefix && !isFoundAlongPath)
				break;
			
			String subSignature = it.next();
			StaticFPtoWPsStoreNode findMatchChildNode = current.findMatchingChildNode(subSignature);
			if(findMatchChildNode == null){
				if(prefix.isEmpty() || current.getNodeId() == deepestNodeVisited.getNodeId()) {
					//either the prefix is empty, which means that you only search forward
					//or the current node you search is the last node on the prefix, which means you already move forward
					isFoundAlongPath = false;
				}
				//dismatching, please go back to match in the node along the prefix
				StaticFPtoWPsStoreNode findMatchBackwardNode = findNodeBackward(prefix, 
						subSignature);
				if(findMatchBackwardNode == null) {
					current = deepestNodeVisited;
					isFoundInPrefix = false;
				}else {
					current = findMatchBackwardNode;
					isFoundInPrefix = true;
				}
			}else {
				current = findMatchChildNode;
				// matching, please go the next one
				if(!prefix.containsKey(new Integer(current.getNodeId()))) {
					//added unique node to the ordered prefix
					prefix.put(current.getNodeId(), current);
					deepestNodeVisited = current;
					isFoundAlongPath = true;
				}
			}
		}
		if(deepestNodeVisited != null) {
			if(deepestNodeVisited.isLastNodeOnPath()) {
				wp = deepestNodeVisited.getWeakestPrecondition();
				Debug.println("You find your wp " + wp.toString());
			}else {
				System.out.println("You didn't find wp 1");
			}
		}else {
			System.out.println("You didn't find wp 2");
		}
		return wp;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String contentStr = "";
		return contentStr;
	}
	
	/**
	 * Prints the out all content.
	 */
	public void printOutAllContent() {
		System.out.println("the weakest preconditions sorted by paths " + this.toString());
	}
}
