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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: Auto-generated Javadoc
/**
 * The Class CFGNode. It defines the node in a control flow graph
 * 
 * @param <T1>
 *            the generic type
 * @param <T2>
 *            the generic type
 * @author chengli
 */
public class CFGNode<T1, T2> {
	
	private int sequenceId;
	
	private static AtomicInteger sequencer = new AtomicInteger(0);

	/** The node id. */
	private T1 nodeId;

	/** The node data. */
	private T2 nodeData;

	/** The successors. */
	private List<CFGNode<T1, T2>> successors;

	/** The predecessor. */
	private List<CFGNode<T1, T2>> predecessor;
	
	/** The is return node. */
	private boolean isReturnNode;

	/**
	 * Instantiates a new cFG node.
	 *
	 * @param nodeId the node id
	 */
	public CFGNode(T1 nodeId) {
		this.nodeId = nodeId;
		this.nodeData = null;
		this.setSuccessors(new ArrayList<CFGNode<T1, T2>>());
		this.setPredecessor(new ArrayList<CFGNode<T1, T2>>());
		this.setReturnNode(false);
		this.setSequenceId(sequencer.getAndIncrement());
	}

	/**
	 * Instantiates a new cFG node.
	 *
	 * @param nodeId the node id
	 * @param nData the n data
	 */
	public CFGNode(T1 nodeId, T2 nData) {
		this.nodeId = nodeId;
		this.nodeData = nData;
		this.setSuccessors(new ArrayList<CFGNode<T1, T2>>());
		this.setPredecessor(new ArrayList<CFGNode<T1, T2>>());
		this.setReturnNode(false);
		this.setSequenceId(sequencer.getAndIncrement());
	}

	/**
	 * Instantiates a new cFG node.
	 *
	 * @param nodeId the node id
	 * @param nData the n data
	 * @param nSList the n s list
	 * @param nPList the n p list
	 */
	public CFGNode(T1 nodeId, T2 nData, ArrayList<CFGNode<T1, T2>> nSList,
			ArrayList<CFGNode<T1, T2>> nPList) {
		this.nodeId = nodeId;
		this.nodeData = nData;
		this.setSuccessors(nSList);
		this.setPredecessor(nPList);
		this.setReturnNode(false);
		this.setSequenceId(sequencer.getAndIncrement());
	}

	/**
	 * Gets the node id.
	 *
	 * @return the node id
	 */
	public T1 getNodeId() {
		return this.nodeId;
	}

	/**
	 * Gets the node data.
	 *
	 * @return the node data
	 */
	public T2 getNodeData() {
		return this.nodeData;
	}

	/**
	 * Sets the node data.
	 *
	 * @param nData the new node data
	 */
	public void setNodeData(T2 nData) {
		this.nodeData = nData;
	}

	/**
	 * Gets the successors.
	 *
	 * @return the successors
	 */
	public List<CFGNode<T1, T2>> getSuccessors() {
		return successors;
	}
	
	/**
	 * Gets the successors copy.
	 *
	 * @return the successors copy
	 */
	public List<CFGNode<T1, T2>> getSuccessorsCopy() {
		List<CFGNode<T1, T2>> successorsCopy = new ArrayList<CFGNode<T1, T2>>();
		for(CFGNode<T1, T2> cfgNode : this.getSuccessors()){
			successorsCopy.add(cfgNode);
		}
		return successorsCopy;
	}

	/**
	 * Sets the successors.
	 *
	 * @param successors the successors
	 */
	public void setSuccessors(List<CFGNode<T1, T2>> successors) {
		this.successors = successors;
	}

	/**
	 * Adds the successor.
	 *
	 * @param succesor the succesor
	 */
	public void addSuccessor(CFGNode<T1, T2> succesor) {
		if (!this.successors.contains(succesor)) {
			this.successors.add(succesor);
		}
	}

	/**
	 * Removes the successor.
	 *
	 * @param succesor the succesor
	 */
	public void removeSuccessor(CFGNode<T1, T2> succesor) {
		if (this.successors.contains(succesor)) {
			this.successors.remove(succesor);
		}
	}

	/**
	 * Gets the predecessor.
	 *
	 * @return the predecessor
	 */
	public List<CFGNode<T1, T2>> getPredecessor() {
		return predecessor;
	}
	
	/**
	 * Gets the predecessor copy.
	 *
	 * @return the predecessor copy
	 */
	public List<CFGNode<T1, T2>> getPredecessorCopy() {
		List<CFGNode<T1, T2>> predecessorList = new ArrayList<CFGNode<T1, T2>>();
		for(CFGNode<T1, T2> cfgNode : this.predecessor){
			predecessorList.add(cfgNode);
		}
		return predecessorList;
	}

	/**
	 * Sets the predecessor.
	 *
	 * @param predecessor the predecessor
	 */
	public void setPredecessor(List<CFGNode<T1, T2>> predecessor) {
		this.predecessor = predecessor;
	}

	/**
	 * Adds the predecessor.
	 *
	 * @param predecessor the predecessor
	 */
	public void addPredecessor(CFGNode<T1, T2> predecessor) {
		if (!this.predecessor.contains(predecessor)) {
			this.predecessor.add(predecessor);
		}
	}

	/**
	 * Removes the predecessor.
	 *
	 * @param predecessor the predecessor
	 */
	public void removePredecessor(CFGNode<T1, T2> predecessor) {
		if (this.predecessor.contains(predecessor)) {
			this.predecessor.remove(predecessor);
		}
	}

	/**
	 * Checks if this node is connect to another node by a directed edge.
	 * 
	 * @param n
	 *            the n
	 * @return true, if is connect to
	 */
	public boolean isConnectTo(CFGNode<T1, T2> n) {
		if (this.successors.contains(n)) {
			return true;
		} else {
			return false;
		}
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
		String str = this.getSequenceId()+"-" + this.getNodeId().toString() + " ";
		if(this.isReturnNode()) {
			str += "return ";
		}
		if (nodeData == null) {
			str += "empty";
		} else {
			/*if (nodeData.toString().equals("")) {
				str += "Break";
			} else {
				str += nodeData.toString();
			}*/
			str += nodeData.toString();
		}
		return str;
	}
	
	/**
	 * Cfg node clone. Only copy the data,
	 * instead of successor and predecessor
	 *
	 * @return the cFG node
	 */
	public CFGNode<T1, T2> cfgNodeClone(){
		CFGNode<T1, T2> cfgNode = new CFGNode<T1, T2>(this.getNodeId(), this.getNodeData());
		cfgNode.setSequenceId(this.getSequenceId());
		return cfgNode;
	}
	
	/**
	 * Checks if is return node.
	 *
	 * @return true, if is return node
	 */
	public boolean isReturnNode(){
		return this.isReturnNode;
	}

	/**
	 * Sets the return node.
	 *
	 * @param isReturnNode the isReturnNode to set
	 */
	public void setReturnNode(boolean isReturnNode) {
		this.isReturnNode = isReturnNode;
	}
	
	/**
	 * Checks if is empty exit node. An empty exit node
	 * is the one artificially created by ourselves
	 *
	 * @return true, if is empty exit node
	 */
	public boolean isEmptyExitNode(){
		if(!this.isReturnNode() && this.getNodeData() == null){
			return true;
		}
		return false;
	}

	/**
	 * @param sequenceId the sequenceId to set
	 */
	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}

	/**
	 * @return the sequenceId
	 */
	public int getSequenceId() {
		return sequenceId;
	}

}
