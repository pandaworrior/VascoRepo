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
package org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.mpi.vasco.sieve.staticanalysis.datastructures.regularexpression.RegularExpressionDef;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class CFGGraph.
 *
 * @param <T1> the generic type
 * @param <T2> the generic type
 */
public class CFGGraph<T1, T2> {

	/** The entry node list. */
	private List<CFGNode<T1, T2>> entryNodeList;

	/** The exit node list. */
	private List<CFGNode<T1, T2>> exitNodeList;
	
	/** The cfg identifier. */
	private T1 cfgIdentifier;

	/**
	 * The return node list. The difference between exit and return is that the
	 * exit is the node that will be connected to the counterpart of the other
	 * node. The return node is that the one is return statement.
	 */
	private List<CFGNode<T1, T2>> returnNodeList;

	/** The node list. */
	private List<CFGNode<T1, T2>> nodeList;

	/** The edge list. */
	private List<CFGEdge<T1, T2>> edgeList;

	/**
	 * Instantiates a new cFG graph.
	 */
	public CFGGraph() {
		this.entryNodeList = new ArrayList<CFGNode<T1, T2>>();
		this.exitNodeList = new ArrayList<CFGNode<T1, T2>>();
		this.returnNodeList = new ArrayList<CFGNode<T1, T2>>();
		this.nodeList = new ArrayList<CFGNode<T1, T2>>();
		this.edgeList = new ArrayList<CFGEdge<T1, T2>>();
		this.cfgIdentifier = null;
	}

	/**
	 * Instantiates a new cFG graph.
	 *
	 * @param e1 the e1
	 * @param e2 the e2
	 */
	public CFGGraph(List<CFGNode<T1, T2>> e1, List<CFGNode<T1, T2>> e2) {
		this.setEntryNodeList(e1);
		this.setExitNodeList(e2);
		this.returnNodeList = new ArrayList<CFGNode<T1, T2>>();
		this.nodeList = new ArrayList<CFGNode<T1, T2>>();
		this.edgeList = new ArrayList<CFGEdge<T1, T2>>();
		this.cfgIdentifier = null;
	}

	/**
	 * Instantiates a new cFG graph.
	 *
	 * @param e1 the e1
	 * @param e2 the e2
	 * @param nList the n list
	 */
	public CFGGraph(List<CFGNode<T1, T2>> e1, List<CFGNode<T1, T2>> e2,
			List<CFGNode<T1, T2>> nList) {
		this.setEntryNodeList(e1);
		this.setExitNodeList(e2);
		this.setReturnNodeList(nList);
		this.nodeList = new ArrayList<CFGNode<T1, T2>>();
		this.edgeList = new ArrayList<CFGEdge<T1, T2>>();
		this.cfgIdentifier = null;
	}

	/**
	 * Instantiates a new cFG graph.
	 *
	 * @param e1 the e1
	 * @param e2 the e2
	 * @param nList1 the n list1
	 * @param nList2 the n list2
	 */
	public CFGGraph(List<CFGNode<T1, T2>> e1, List<CFGNode<T1, T2>> e2,
			List<CFGNode<T1, T2>> nList1, List<CFGNode<T1, T2>> nList2) {
		this.setEntryNodeList(e1);
		this.setExitNodeList(e2);
		this.setReturnNodeList(nList1);
		this.setNodeList(nList2);
		this.edgeList = new ArrayList<CFGEdge<T1, T2>>();
		this.cfgIdentifier = null;
	}

	/**
	 * Instantiates a new cFG graph.
	 *
	 * @param e1 the e1
	 * @param e2 the e2
	 * @param nList1 the n list1
	 * @param nList2 the n list2
	 * @param eList the e list
	 */
	public CFGGraph(List<CFGNode<T1, T2>> e1, List<CFGNode<T1, T2>> e2,
			List<CFGNode<T1, T2>> nList1, List<CFGNode<T1, T2>> nList2,
			List<CFGEdge<T1, T2>> eList) {
		this.setEntryNodeList(e1);
		this.setEntryNodeList(e2);
		this.setReturnNodeList(nList1);
		this.setNodeList(nList2);
		this.setEdgeList(eList);
		this.cfgIdentifier = null;
	}

	/**
	 * Gets the entry node list.
	 *
	 * @return the entry node list
	 */
	public List<CFGNode<T1, T2>> getEntryNodeList() {
		return entryNodeList;
	}

	/**
	 * Sets the entry node list.
	 *
	 * @param enList the en list
	 */
	public void setEntryNodeList(List<CFGNode<T1, T2>> enList) {
		this.entryNodeList = enList;
		for (CFGNode<T1, T2> node : enList) {
			this.addNode(node);
		}
	}

	/**
	 * Adds the entry node.
	 *
	 * @param eNode the e node
	 */
	public void addEntryNode(CFGNode<T1, T2> eNode) {
		if (!this.entryNodeList.contains(eNode)) {
			this.entryNodeList.add(eNode);
		}
		this.addNode(eNode);
	}

	/**
	 * Adds the entry node list.
	 *
	 * @param eNodeList the e node list
	 */
	public void addEntryNodeList(List<CFGNode<T1, T2>> eNodeList) {
		for (CFGNode<T1, T2> eNode : eNodeList) {
			this.addEntryNode(eNode);
		}
	}

	/**
	 * Gets the exit node list.
	 *
	 * @return the exit node list
	 */
	public List<CFGNode<T1, T2>> getExitNodeList() {
		return this.exitNodeList;
	}

	/**
	 * Gets the exit node list copy.
	 *
	 * @return the exit node list copy
	 */
	public List<CFGNode<T1, T2>> getExitNodeListCopy() {
		List<CFGNode<T1, T2>> etNodeListCopy = new ArrayList<CFGNode<T1, T2>>();
		for (CFGNode<T1, T2> etNode : this.exitNodeList) {
			etNodeListCopy.add(etNode);
		}
		return etNodeListCopy;
	}

	/**
	 * Sets the exit node list.
	 *
	 * @param enList the en list
	 */
	public void setExitNodeList(List<CFGNode<T1, T2>> enList) {
		this.exitNodeList = enList;
		for (CFGNode<T1, T2> node : enList) {
			this.addNode(node);
		}
	}

	/**
	 * Adds the exit node.
	 *
	 * @param exNode the ex node
	 */
	public void addExitNode(CFGNode<T1, T2> exNode) {
		if (!this.exitNodeList.contains(exNode)) {
			this.exitNodeList.add(exNode);
		}
		this.addNode(exNode);
	}

	/**
	 * Adds the exit node list.
	 *
	 * @param exNodeList the ex node list
	 */
	public void addExitNodeList(List<CFGNode<T1, T2>> exNodeList) {
		for (CFGNode<T1, T2> exNode : exNodeList) {
			this.addExitNode(exNode);
		}
	}

	/**
	 * Removes the exit node.
	 *
	 * @param exNode the ex node
	 */
	public void removeExitNode(CFGNode<T1, T2> exNode) {
		if (this.exitNodeList.contains(exNode)) {
			this.exitNodeList.remove(exNode);
		}
	}

	/**
	 * Gets the return node list.
	 *
	 * @return the return node list
	 */
	public List<CFGNode<T1, T2>> getReturnNodeList() {
		return returnNodeList;
	}

	/**
	 * Sets the return node list.
	 *
	 * @param returnNodeList the return node list
	 */
	public void setReturnNodeList(List<CFGNode<T1, T2>> returnNodeList) {
		this.returnNodeList = returnNodeList;
		for (CFGNode<T1, T2> node : returnNodeList) {
			this.addNode(node);
		}
	}

	/**
	 * Adds the return node.
	 *
	 * @param rNode the r node
	 */
	public void addReturnNode(CFGNode<T1, T2> rNode) {
		if (!this.returnNodeList.contains(rNode)) {
			this.returnNodeList.add(rNode);
			if(!rNode.isReturnNode()) {
				rNode.setReturnNode(true);
			}
		}
		this.addNode(rNode);
		/*//if exit node list is not empty, please clean it
		if(this.exitNodeList != null && this.exitNodeList.size() > 0){
			this.exitNodeList.clear();
		}*/
	}

	/**
	 * Adds the return node list.
	 *
	 * @param rNodeList the r node list
	 */
	public void addReturnNodeList(List<CFGNode<T1, T2>> rNodeList) {
		for (CFGNode<T1, T2> rNode : rNodeList) {
			this.addReturnNode(rNode);
		}
	}

	/**
	 * Removes the return node.
	 *
	 * @param rNode the r node
	 */
	public void removeReturnNode(CFGNode<T1, T2> rNode) {
		if (this.returnNodeList.contains(rNode)) {
			this.returnNodeList.remove(rNode);
		}
	}
	
	/**
	 * Gets the node.
	 *
	 * @param nodeId the node id
	 * @return the node
	 */
	public CFGNode<T1, T2> getNode(T1 nodeId){
		for(CFGNode<T1, T2> cfgNode : this.nodeList){
			if(cfgNode.getNodeId().equals(nodeId)){
				return cfgNode;
			}
		}
		return null;
	}
	
	/**
	 * Gets the node by unique sequence.
	 *
	 * @param seq the seq
	 * @return the node by unique sequence
	 */
	public CFGNode<T1, T2> getNodeByUniqueSequence(int seq){
		for(CFGNode<T1, T2> cfgNode : this.nodeList){
			if(cfgNode.getSequenceId() == seq){
				return cfgNode;
			}
		}
		return null;
	}

	/**
	 * Gets the node list.
	 *
	 * @return the node list
	 */
	public List<CFGNode<T1, T2>> getNodeList() {
		return nodeList;
	}

	/**
	 * Gets the node list via bfs.
	 *
	 * @return the node list via bfs
	 */
	public List<CFGNode<T1, T2>> getNodeListViaBFS() {
		Stack<CFGNode<T1, T2>> nodeStack = new Stack<CFGNode<T1, T2>>();
		List<CFGNode<T1, T2>> bfsNodeList = new ArrayList<CFGNode<T1, T2>>();
		assert (this.getEntryNodeList().size() == 1);
		nodeStack.push(this.entryNodeList.get(0));
		while (!nodeStack.isEmpty()) {
			CFGNode<T1, T2> currentNode = nodeStack.pop();
			if (!bfsNodeList.contains(currentNode)) {
				bfsNodeList.add(currentNode);
				List<CFGNode<T1, T2>> successors = currentNode.getSuccessors();
				for (CFGNode<T1, T2> succNode : successors) {
					nodeStack.push(succNode);
				}
			}
		}
		return bfsNodeList;
	}

	/**
	 * Sets the node list.
	 *
	 * @param nodeList the node list
	 */
	public void setNodeList(List<CFGNode<T1, T2>> nodeList) {
		this.nodeList = nodeList;
	}

	/**
	 * Adds the node.
	 *
	 * @param node the node
	 */
	public void addNode(CFGNode<T1, T2> node) {
		if (!this.nodeList.contains(node)) {
			this.nodeList.add(node);
		}
	}

	/**
	 * Adds the node list.
	 *
	 * @param nList the n list
	 */
	public void addNodeList(List<CFGNode<T1, T2>> nList) {
		for (CFGNode<T1, T2> node : nList) {
			this.addNode(node);
		}
	}

	/**
	 * Adds the edge.
	 *
	 * @param source the source
	 * @param destination the destination
	 */
	public void addEdge(CFGNode<T1, T2> source, CFGNode<T1, T2> destination) {
		if (!this.nodeList.contains(source)) {
			this.nodeList.add(source);
		}
		if (!this.nodeList.contains(destination)) {
			this.nodeList.add(destination);
		}
		if (!source.isConnectTo(destination)) {
			CFGEdge<T1, T2> edge = new CFGEdge<T1, T2>(source, destination);
			this.edgeList.add(edge);
			Debug.println("add " + edge.toString());
		}
		source.addSuccessor(destination);
		destination.addPredecessor(source);
	}

	/**
	 * Adds the edge.
	 *
	 * @param edge the edge
	 */
	private void addEdge(CFGEdge<T1, T2> edge) {
		if (!this.nodeList.contains(edge.getSource())) {
			this.nodeList.add(edge.getSource());
		}
		if (!this.nodeList.contains(edge.getDestination())) {
			this.nodeList.add(edge.getDestination());
		}
		if (!this.edgeList.contains(edge)) {
			this.edgeList.add(edge);
		}
	}

	/**
	 * Adds the edge list.
	 *
	 * @param eList the e list
	 */
	public void addEdgeList(List<CFGEdge<T1, T2>> eList) {
		for (CFGEdge<T1, T2> edge : eList) {
			this.addEdge(edge);
		}
	}

	/**
	 * Removes the edge.
	 *
	 * @param source the source
	 * @param destination the destination
	 */
	public void removeEdge(CFGNode<T1, T2> source, CFGNode<T1, T2> destination) {
		source.removeSuccessor(destination);
		destination.removePredecessor(source);
		CFGEdge<T1, T2> edge = this.getEdge(source, destination);
		if (edge != null) {
			this.edgeList.remove(edge);
			Debug.println("remove " + edge.toString());
		}else{
			System.err.println("edge doesn't exist " + source.toString() + " " +
					destination.toString());
		}
	}

	/**
	 * Removes the edges starting from one node.
	 *
	 * @param node the node
	 */
	public void removeEdgesStartingFromOneNode(CFGNode<T1, T2> node) {
		List<CFGNode<T1, T2>> successors = node.getSuccessorsCopy();
		for (CFGNode<T1, T2> succeNode : successors) {
			this.removeEdge(node, succeNode);
			//node.removeSuccessor(succeNode);
			//succeNode.removePredecessor(node);
		}
	}

	/**
	 * Gets the edge.
	 *
	 * @param source the source
	 * @param destination the destination
	 * @return the edge
	 */
	public CFGEdge<T1, T2> getEdge(CFGNode<T1, T2> source,
			CFGNode<T1, T2> destination) {
		for (CFGEdge<T1, T2> edge : this.edgeList) {
			if (edge.getSource() == source
					&& edge.getDestination() == destination) {
				return edge;
			}
		}
		return null;
	}
	
	/**
	 * Gets the edge.
	 *
	 * @param edgeId the edge id
	 * @return the edge
	 */
	public CFGEdge<T1, T2> getEdge(int edgeId) {
		for (CFGEdge<T1, T2> edge : this.edgeList) {
			if (edge.getEdgeId() == edgeId) {
				return edge;
			}
		}
		return null;
	}

	/**
	 * Checks if is direct connected.
	 *
	 * @param n1 the n1
	 * @param n2 the n2
	 * @return true, if is direct connected
	 */
	private boolean isDirectConnected(CFGNode<T1, T2> n1, CFGNode<T1, T2> n2) {
		assert (this.nodeList.contains(n1) && this.nodeList.contains(n2));
		if (n1.isConnectTo(n2)) {
			return true;
		} else {
			return false;
		}

	}
	
	/**
	 * Gets the all physically preceding nodes backward to the root.
	 *
	 * @param node the node
	 * @return the all physically preceding nodes backward
	 */
	public List<CFGNode<T1, T2>> getAllPhysicallyPrecedingNodesBackward(CFGNode<T1, T2> node){
		return null;
	}

	/**
	 * Convert graph into matrix.
	 *
	 * @return the string[][]
	 */
	public String[][] convertGraphIntoMatrix() {
		int nodeCount = this.nodeList.size();
		String[][] esMatrix = new String[nodeCount][nodeCount];
		for (int i = 0; i < nodeCount; i++) {
			String debugStr = "";
			for (int j = 0; j < nodeCount; j++) {
				CFGNode<T1, T2> node_i = this.nodeList.get(i);
				CFGNode<T1, T2> node_j = this.nodeList.get(j);
				// test if they are connected
				// if there is an edge from node i to node j
				if (this.isDirectConnected(node_i, node_j)) {
					CFGEdge<T1, T2> edge = this.getEdge(node_i, node_j);
					esMatrix[i][j] = Integer.toString(edge.getEdgeId());
					debugStr += esMatrix[i][j] + " ";
				} else {
					esMatrix[i][j] = RegularExpressionDef.Empty;
					debugStr += "empty" + " ";
				}
			}
			Debug.println("matrix " + debugStr);
		}
		return esMatrix;
	}

	/**
	 * Initalize reg exp array.
	 *
	 * @return the string[]
	 */
	public String[] initalizeRegExpArray() {
		int nodeCount = this.nodeList.size();
		String[] regularExpArray = new String[nodeCount];
		String debugStr = "";
		for (int topIndex = 0; topIndex < nodeCount; topIndex++) {
			if (this.returnNodeList.contains(this.nodeList.get(topIndex))) {// check
																			// return
																			// node
				regularExpArray[topIndex] = RegularExpressionDef.Epsilon;
				debugStr += "epsilon" + " ";
			} else {
				regularExpArray[topIndex] = RegularExpressionDef.Empty;
				debugStr += "empty" + " ";
			}
		}
		Debug.println("regExp array: " + debugStr);
		return regularExpArray;
	}

	/**
	 * Gets the edge list.
	 *
	 * @return the edge list
	 */
	public List<CFGEdge<T1, T2>> getEdgeList() {
		return edgeList;
	}

	/**
	 * Sets the edge list.
	 *
	 * @param edgeList the edge list
	 */
	public void setEdgeList(List<CFGEdge<T1, T2>> edgeList) {
		this.edgeList = edgeList;
	}

	/**
	 * Merge with other control flow graph. from the current exit node to the
	 * entry node of the sub control graph.
	 * If the current control flow graph contain exit nodes, then
	 * connect exit nodes to the entry nodes of cfg
	 * Else,
	 * then add entry nodes of cfg to this currrent control flow graph since
	 * they are in parallel.
	 * 
	 * @param cfg
	 *            the cfg
	 */
	public void mergeWithOtherControlFlowGraph(CFGGraph<T1, T2> cfg) {		
		// take all non-exit leafs
		List<CFGNode<T1, T2>> enNodeList = cfg.getEntryNodeList();
		assert(cfg.getEntryNodeList() != null && cfg.getEntryNodeList().size() > 0);
		
		List<CFGNode<T1, T2>> etNodeList = this.getExitNodeList();
		
		if(etNodeList.size() > 0) {
			for (CFGNode<T1, T2> etNode : etNodeList) {
				for (CFGNode<T1, T2> enNode : enNodeList) {
					this.addEdge(etNode, enNode);
				}
			}
			// set the cfg.exitnodelist to the current exitnodelist
			this.setExitNodeList(cfg.getExitNodeList());
		}else {
			this.addEntryNodeList(enNodeList);
		}

		// add all nodes and edges from cfg to this control flow graph
		List<CFGEdge<T1, T2>> cfgEdgeList = cfg.getEdgeList();
		for (CFGEdge<T1, T2> cfgEdge : cfgEdgeList) {
			this.addEdge(cfgEdge);
		}
		
		List<CFGNode<T1, T2>> rNodeList = cfg.getReturnNodeList();
		for (CFGNode<T1, T2> rNode : rNodeList) {
			this.addReturnNode(rNode);
		}
	}

	/**
	 * Merge with other control flow graphs. all other control flow graphs are
	 * in parallel. Connect the exit node to the entry point of each sub control
	 * flow graph
	 * 
	 * @param cfgList
	 *            the cfg list
	 */
	public void mergeWithOtherControlFlowGraphs(List<CFGGraph<T1, T2>> cfgList) {
		// take all non-exit leafs
		List<CFGNode<T1, T2>> etNodeList = this.getExitNodeList();
		for (CFGNode<T1, T2> cfgNode : etNodeList) {
			for (CFGGraph<T1, T2> subCfg : cfgList) {
				List<CFGNode<T1, T2>> subCfgEnNodeList = subCfg
						.getEntryNodeList();
				for (CFGNode<T1, T2> subCfgNode : subCfgEnNodeList) {
					this.addEdge(cfgNode, subCfgNode);
				}
			}
		}

		// emptify the exitnodelist
		this.exitNodeList.clear();

		// add all nodes and edges from cfg to this control flow graph
		for (CFGGraph<T1, T2> cfg : cfgList) {
			
			List<CFGEdge<T1, T2>> cfgEdgeList = cfg.getEdgeList();
			for (CFGEdge<T1, T2> cfgEdge : cfgEdgeList) {
				this.addEdge(cfgEdge);
			}

			List<CFGNode<T1, T2>> etNodeSubCfgList = cfg.getExitNodeList();
			for (CFGNode<T1, T2> etNode : etNodeSubCfgList) {
				this.addExitNode(etNode);
			}

			List<CFGNode<T1, T2>> rNodeList = cfg.getReturnNodeList();
			for (CFGNode<T1, T2> rNode : rNodeList) {
				this.addReturnNode(rNode);
			}
		}
	}

	/**
	 * Merge control flow graph to current one. The list of sub control flow
	 * graphs are preceding the current control flow graph.
	 * 
	 * @param subCfg
	 *            the sub cfg
	 */
	public void mergeControlFlowGraphToCurrent(CFGGraph<T1, T2> subCfg) {
		// add all nodes to the current nodes list
		// add all edges to the current edge list
		// add all return node to the current return node list
		//this.addNodeList(subCfg.getNodeList());
		this.addEdgeList(subCfg.getEdgeList());
		this.addReturnNodeList(subCfg.getReturnNodeList());

		// add edges from the exit node of the subcfg to the entrynode of the
		// current one
		List<CFGNode<T1, T2>> etNodeList = this.getEntryNodeList();
		for (CFGNode<T1, T2> etNode : etNodeList) {
			List<CFGNode<T1, T2>> exNodeList = subCfg.getExitNodeList();
			for (CFGNode<T1, T2> exNode : exNodeList) {
				this.addEdge(exNode, etNode);
			}
		}

		// emptify entry node list of the current cfg
		this.entryNodeList.clear();
		// add entry node from all subcfg to the current cfg's entry node list
		List<CFGNode<T1, T2>> etNodeSubCfgList = subCfg.getEntryNodeList();
		for (CFGNode<T1, T2> etNode : etNodeSubCfgList) {
			this.addEntryNode(etNode);
		}
	}

	/**
	 * Merge control flow graphs casacading.
	 *
	 * @param cfgList the cfg list
	 */
	public void mergeControlFlowGraphsCasacading(List<CFGGraph<T1, T2>> cfgList) {
		CFGGraph<T1, T2> cfg = null;
		for (int i = 0; i < cfgList.size(); i++) {
			if (i == 0) {
				cfg = cfgList.get(0);
			} else {
				cfg.mergeWithOtherControlFlowGraph(cfgList.get(i));
			}
		}
		this.mergeControlFlowGraphToCurrent(cfg);
	}

	/**
	 * Merge with control flow preceding specific node. This function is use to
	 * connect a method declaration control flow graph to its caller statement
	 * or expression.
	 * 
	 * @param callerNode
	 *            the caller expression node
	 * @param otherCfg
	 *            the method declaration called by the callerNode
	 */
	public void mergeWithControlFlowPrecedingSpecificNode(
			CFGNode<T1, T2> callerNode, CFGGraph<T1, T2> otherCfg) {
		
		//make a copy of otherCfg
		CFGGraph<T1, T2> otherCfgCopy = otherCfg.getCfgCopy();
		Debug.println("print when you got a copy -------->");
		//otherCfgCopy.printOut();
		Debug.println("<-------- print when you got a copy");
		// add all edges from otherCfg to this
		for (CFGEdge<T1, T2> edge : otherCfgCopy.getEdgeList()) {
			this.addEdge(edge);
		}

		// direct all precessors of callerNode to otherCfg's entry nodes
		// remove the edges from the caller's precessors and the caller
		List<CFGNode<T1, T2>> precessors = callerNode.getPredecessorCopy();
		Iterator<CFGNode<T1, T2>> it = precessors.iterator();
		while(it.hasNext()){
			CFGNode<T1, T2> precessor = it.next();
			for (CFGNode<T1, T2> enNode : otherCfgCopy.getEntryNodeList()) {
				this.addEdge(precessor, enNode);
			}
			this.removeEdge(precessor, callerNode);
		}
		
		//direct all exit node of the otherCfg to caller node
		for(CFGNode<T1, T2> exNode : otherCfgCopy.getExitNodeList()) {
			Debug.println("this is exit node " + exNode.toString());
			//get all its precessors
			List<CFGNode<T1, T2>> exNodePrecessors = exNode.getPredecessorCopy();
			for(CFGNode<T1, T2> precessor : exNodePrecessors){
				this.addEdge(precessor, callerNode);
				this.removeEdge(precessor, exNode);
			}
		}

		// direct all return nodes of the otherCfg to callerNode
		for (CFGNode<T1, T2> rNode : otherCfgCopy.getReturnNodeList()) {
			this.addEdge(rNode, callerNode);
		}
		
		//if the caller node is the entry point of the current cfg,
		//then you have to add all entry node of the othercfg to the current one
		if(this.getEntryNodeList().contains(callerNode)) {
			this.getEntryNodeList().remove(callerNode);
			this.addEntryNodeList(otherCfgCopy.getEntryNodeList());
		}
	}

	/**
	 * Merge with control flow in parallel.
	 *
	 * @param otherCfg the other cfg
	 */
	public void mergeWithControlFlowInParallel(
			CFGGraph<T1, T2> otherCfg) {
		for(CFGEdge<T1, T2> cfgEdge : otherCfg.getEdgeList()){
			this.addEdge(cfgEdge);
		}
		
		for(CFGNode<T1, T2> cfgNode : otherCfg.getEntryNodeList()){
			this.addEntryNode(cfgNode);
		}
		
		for(CFGNode<T1, T2> cfgNode : otherCfg.getExitNodeList()){
			this.addExitNode(cfgNode);
		}
		
		for(CFGNode<T1, T2> cfgNode : otherCfg.getReturnNodeList()){
			this.addReturnNode(cfgNode);
		}
	}
	
	/**
	 * Direct all exit nodes (but not return node) to specific node.
	 * 
	 * @param cfgNode
	 *            the cfg node
	 */
	public void directAllExitNodesToSpecificNode(CFGNode<T1, T2> cfgNode) {
		List<CFGNode<T1, T2>> exitNodeList = this.getExitNodeList();
		for (CFGNode<T1, T2> leafNode : exitNodeList) {
			this.addEdge(leafNode, cfgNode);
		}
	}

	/**
	 * Checks if is exit node list empty.
	 *
	 * @return true, if is exit node list empty
	 */
	public boolean isExitNodeListEmpty() {
		if (this.getExitNodeList().isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Gets the cfg copy.
	 *
	 * @return the cfg copy
	 */
	public CFGGraph<T1, T2> getCfgCopy(){
		HashMap<CFGNode<T1, T2>, CFGNode<T1, T2>> oldToNewMapping = new HashMap<CFGNode<T1, T2>, CFGNode<T1, T2>>();
		CFGGraph<T1, T2> cfgCopy = new CFGGraph<T1, T2> ();
		for(CFGNode<T1, T2> cfgNode : this.getNodeList()){
			CFGNode<T1, T2> cfgNodeCopy = cfgNode.cfgNodeClone();
			cfgCopy.addNode(cfgNodeCopy);
			oldToNewMapping.put(cfgNode, cfgNodeCopy);
			if(this.getEntryNodeList().contains(cfgNode)){
				cfgCopy.addEntryNode(cfgNodeCopy);
			}
			if(this.getExitNodeList().contains(cfgNode)){
				cfgCopy.addExitNode(cfgNodeCopy);
			}
			if(this.getReturnNodeList().contains(cfgNode)){
				cfgCopy.addReturnNode(cfgNodeCopy);
			}
		}
		
		for(CFGEdge<T1, T2> cfgEdge : this.getEdgeList()){
			CFGNode<T1, T2> source = oldToNewMapping.get(cfgEdge.getSource());
			CFGNode<T1, T2> dest = oldToNewMapping.get(cfgEdge.getDestination());
			cfgCopy.addEdge(source, dest);
		}
		return cfgCopy;
	}
	
	/**
	 * Consistency check.
	 */
	public void consistencyCheck(){
		if(this.getExitNodeList() != null && this.getExitNodeList().size() > 0){
			if(this.getReturnNodeList() != null){
				assert(this.getReturnNodeList().size() == 0);
			}
		}else{
			assert(this.getReturnNodeList() != null && this.getReturnNodeList().size() > 0);
		}
		if(this.getReturnNodeList() != null && this.getReturnNodeList().size() > 0){
			if(this.getExitNodeList() != null){
				assert(this.getExitNodeList().size() == 0);
			}
		}else{
			assert(this.getExitNodeList() != null && this.getExitNodeList().size() > 0);
		}
	}

	/**
	 * Prints the out.
	 */
	public void printOut() {
		System.out.println("---------> Print out a graph");
		/*System.out.println("Print all nodes");
		for (CFGNode<T1, T2> node : this.nodeList) {
			System.out.println(node.toString());
		}
		System.out.println("Print all edges");
		for (CFGEdge<T1, T2> edge : this.edgeList) {
			System.out.println(edge.toString());
		}*/

		System.out.println("Print entry nodes");
		for (CFGNode<T1, T2> node : this.entryNodeList) {
			System.out.println(node.toString());
		}

		System.out.println("Print exit nodes");
		for (CFGNode<T1, T2> node : this.exitNodeList) {
			System.out.println(node.toString());
		}

		System.out.println("Print return nodes");
		for (CFGNode<T1, T2> node : this.returnNodeList) {
			System.out.println(node.toString());
		}
		
		this.printOutNodesEdgesInOrder();
		
		System.out.println("Print out a graph <---------");
		//check consistency
		this.consistencyCheck();
	}
	
	/**
	 * Checks if is node contained.
	 *
	 * @param cfgNode the cfg node
	 * @return true, if is node contained
	 */
	public boolean isNodeContained(CFGNode<T1, T2> cfgNode){
		if(this.getNodeList().contains(cfgNode)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Removes the all successors for specific node.
	 *
	 * @param cfgNode the cfg node
	 */
	public void removeAllSuccessorsForSpecificNode(CFGNode<T1, T2> cfgNode){
		assert(this.isNodeContained(cfgNode) == true);
		this.removeEdgesStartingFromOneNode(cfgNode);
	}
	
	/**
	 * Prints the out nodes edges in order.
	 */
	public void printOutNodesEdgesInOrder(){
		Stack<CFGNode<T1, T2>> cfgNodeStack = new Stack<CFGNode<T1, T2>>();
		for(CFGNode<T1, T2> entryNode : this.getEntryNodeList()){
			cfgNodeStack.push(entryNode);
		}
		
		List<CFGNode<T1, T2>> alreadyVisitedNodeList = new ArrayList<CFGNode<T1, T2>>();
		while(cfgNodeStack.size() > 0){
			CFGNode<T1, T2> currentNode = cfgNodeStack.pop();
			if(!alreadyVisitedNodeList.contains(currentNode)){
				alreadyVisitedNodeList.add(currentNode);
				List<CFGNode<T1, T2>> successors = currentNode.getSuccessors();
				for(CFGNode<T1, T2> cfgNode : successors){
					CFGEdge<T1, T2> cfgEdge = this.getEdge(currentNode, cfgNode);
					assert(cfgEdge != null);
					System.out.println(cfgEdge.toString());
					cfgNodeStack.push(cfgNode);
				}
			}
		}
	}

	/**
	 * Sets the cfg identifier.
	 *
	 * @param cfgIdentifier the cfgIdentifier to set
	 */
	public void setCfgIdentifier(T1 cfgIdentifier) {
		this.cfgIdentifier = cfgIdentifier;
	}

	/**
	 * Gets the cfg identifier.
	 *
	 * @return the cfgIdentifier
	 */
	public T1 getCfgIdentifier() {
		return cfgIdentifier;
	}
	
	/**
	 * Gets the cfg identifier plain text.
	 *
	 * @return the cfg identifier plain text
	 */
	public String getCfgIdentifierPlainText() {
		if(this.getCfgIdentifier() == null) {
			System.out.println("You know the cfg identifier is not set");
			System.exit(-1);
		}
		return this.getCfgIdentifier().toString();
	}
}
