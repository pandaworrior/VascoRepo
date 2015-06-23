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
package org.mpi.vasco.sieve.staticanalysis.datastructures.tree;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class TreeNode.
 *
 * @param <T1> the generic type
 * @param <T2> the generic type
 */
public class TreeNode<T1, T2> {

	/** The node type. */
	private TreeNodeTypes nodeType;

	/** The node id. */
	private T1 nodeId;

	/** The node data. */
	private T2 nodeData = null;

	/** The parent. */
	private TreeNode<T1, T2> parent = null;

	/** The children. */
	private List<TreeNode<T1, T2>> children;

	/**
	 * Instantiates a new tree node.
	 *
	 * @param nId the n id
	 * @param nData the n data
	 * @param p the p
	 */
	public TreeNode(T1 nId, T2 nData, TreeNode<T1, T2> p) {
		this.nodeId = nId;
		this.nodeData = nData;
		this.parent = p;
		this.children = new ArrayList<TreeNode<T1, T2>>();
	}

	/**
	 * Instantiates a new tree node.
	 *
	 * @param nId the n id
	 * @param nData the n data
	 * @param p the p
	 * @param nType the n type
	 */
	public TreeNode(T1 nId, T2 nData, TreeNode<T1, T2> p, TreeNodeTypes nType) {
		this.nodeId = nId;
		this.nodeData = nData;
		this.parent = p;
		this.nodeType = nType;
		this.children = new ArrayList<TreeNode<T1, T2>>();
	}

	/**
	 * Instantiates a new tree node.
	 *
	 * @param nId the n id
	 * @param nData the n data
	 * @param p the p
	 * @param cL the c l
	 */
	public TreeNode(T1 nId, T2 nData, TreeNode<T1, T2> p,
			List<TreeNode<T1, T2>> cL) {
		this.nodeId = nId;
		this.nodeData = nData;
		this.parent = p;
		this.children = cL;
	}

	/**
	 * Instantiates a new tree node.
	 *
	 * @param nId the n id
	 * @param nData the n data
	 * @param p the p
	 * @param nType the n type
	 * @param cL the c l
	 */
	public TreeNode(T1 nId, T2 nData, TreeNode<T1, T2> p, TreeNodeTypes nType,
			List<TreeNode<T1, T2>> cL) {
		this.nodeId = nId;
		this.nodeData = nData;
		this.parent = p;
		this.nodeType = nType;
		this.children = cL;
	}

	/**
	 * Sets the node type.
	 *
	 * @param t the new node type
	 */
	public void setNodeType(TreeNodeTypes t) {
		this.nodeType = t;
	}

	/**
	 * Sets the node data.
	 *
	 * @param data the new node data
	 */
	public void setNodeData(T2 data) {
		this.nodeData = data;
	}

	/**
	 * Sets the children list.
	 *
	 * @param cL the c l
	 */
	public void setChildrenList(List<TreeNode<T1, T2>> cL) {
		this.children = cL;
	}

	/**
	 * Gets the parent.
	 *
	 * @return the parent
	 */
	public TreeNode<T1, T2> getParent() {
		return this.parent;
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
	 * Gets the node type.
	 *
	 * @return the node type
	 */
	public TreeNodeTypes getNodeType() {
		return this.nodeType;
	}

	/**
	 * Gets the children.
	 *
	 * @return the children
	 */
	public List<TreeNode<T1, T2>> getChildren() {
		return this.children;
	}

	/**
	 * Removes the child node.
	 *
	 * @param c the c
	 */
	public void removeChildNode(TreeNode<T1, T2> c) {
		assert (c.parent == this);
		assert (this.children.contains(c));
		this.children.remove(c);
		if (this.children.isEmpty()) {
			this.setNodeType(TreeNodeTypes.LEAF);
		}
	}

	/**
	 * Removes the current node.
	 */
	public void removeCurrentNode() {
		if (parent != null) {
			parent.removeChildNode(this);
		}
	}

	/**
	 * Adds the child node.
	 *
	 * @param c the c
	 */
	public void addChildNode(TreeNode<T1, T2> c) {
		c.parent = this;
		if (!this.children.contains(c)) {
			this.children.add(c);
			if (this.getNodeType() == TreeNodeTypes.LEAF) {
				this.setNodeType(TreeNodeTypes.INTERNAL);
			}
		}
	}

	/**
	 * Checks if is root.
	 *
	 * @return true, if is root
	 */
	public boolean isRoot() {
		if (this.nodeType == TreeNodeTypes.ROOT) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if is internal.
	 *
	 * @return true, if is internal
	 */
	public boolean isInternal() {
		if (this.nodeType == TreeNodeTypes.INTERNAL) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if is leaf.
	 *
	 * @return true, if is leaf
	 */
	public boolean isLeaf() {
		if (this.nodeType == TreeNodeTypes.LEAF) {
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
	 * @see java.lang.Object#toString()
	 * @return
	 */
	public String toString() {
		String str = this.nodeType + " Node: " + this.nodeId.toString() + " "
				+ this.nodeData.toString();
		return str;
	}
}
