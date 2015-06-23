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
import java.util.HashMap;

import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class Tree.
 *
 * @param <T1> the generic type
 * @param <T2> the generic type
 */
public class Tree<T1, T2> {

	/** The root. */
	private TreeNode<T1, T2> root;

	/** The tree map. */
	private HashMap<T1, TreeNode<T1, T2>> treeMap;

	/**
	 * Instantiates a new tree.
	 */
	public Tree() {
		this.root = null;
		this.treeMap = new HashMap<T1, TreeNode<T1, T2>>();
	}

	/**
	 * Instantiates a new tree.
	 *
	 * @param r the r
	 */
	public Tree(TreeNode<T1, T2> r) {
		this.root = r;
		this.treeMap = new HashMap<T1, TreeNode<T1, T2>>();
	}

	/**
	 * Instantiates a new tree.
	 *
	 * @param r the r
	 * @param tM the t m
	 */
	public Tree(TreeNode<T1, T2> r, HashMap<T1, TreeNode<T1, T2>> tM) {
		this.root = r;
		this.treeMap = tM;
	}

	/**
	 * Sets the root tree node.
	 *
	 * @param r the r
	 */
	public void setRootTreeNode(TreeNode<T1, T2> r) {
		this.root = r;
		this.treeMap.put(r.getNodeId(), r);
	}

	/**
	 * Removes the root tree node.
	 *
	 * @param r the r
	 */
	public void removeRootTreeNode(TreeNode<T1, T2> r) {
		this.root = null;
	}

	/**
	 * Sets the node hash map.
	 *
	 * @param tM the t m
	 */
	public void setNodeHashMap(HashMap<T1, TreeNode<T1, T2>> tM) {
		this.treeMap = tM;
	}

	/**
	 * Adds the tree node.
	 *
	 * @param parent the parent
	 * @param child the child
	 */
	public void addTreeNode(TreeNode<T1, T2> parent, TreeNode<T1, T2> child) {
		parent.addChildNode(child);
		this.treeMap.put(child.getNodeId(), child);
	}

	/**
	 * Removes the tree node.
	 *
	 * @param parent the parent
	 * @param child the child
	 */
	public void removeTreeNode(TreeNode<T1, T2> parent, TreeNode<T1, T2> child) {
		parent.removeChildNode(child);
	}

	/**
	 * Gets the tree node by id.
	 *
	 * @param id the id
	 * @return the tree node by id
	 */
	public TreeNode<T1, T2> getTreeNodeById(T1 id) {
		return this.treeMap.get(id);
	}

	/**
	 * Depth first search.
	 *
	 * @param n the n
	 * @param l the l
	 */
	private void depthFirstSearch(TreeNode<T1, T2> n,
			ArrayList<TreeNode<T1, T2>> l) {
		l.add(n);
		for (TreeNode<T1, T2> child : n.getChildren()) {
			this.depthFirstSearch(child, l);
		}
	}

	/**
	 * Gets the tree node in dfs order.
	 *
	 * @return the tree node in dfs order
	 */
	public ArrayList<TreeNode<T1, T2>> getTreeNodeInDFSOrder() {
		ArrayList<TreeNode<T1, T2>> nodeList = new ArrayList<TreeNode<T1, T2>>();
		this.depthFirstSearch(this.root, nodeList);
		return nodeList;
	}

	/**
	 * Gets the tree node in bfs order.
	 *
	 * @return the tree node in bfs order
	 */
	public ArrayList<TreeNode<T1, T2>> getTreeNodeInBFSOrder() {
		ArrayList<TreeNode<T1, T2>> tempList = new ArrayList<TreeNode<T1, T2>>();
		ArrayList<TreeNode<T1, T2>> nodeList = new ArrayList<TreeNode<T1, T2>>();
		tempList.add(this.root);
		while (!tempList.isEmpty()) {
			TreeNode<T1, T2> currentNode = tempList.remove(0);
			nodeList.add(currentNode);
			for (TreeNode<T1, T2> child : currentNode.getChildren()) {
				tempList.add(child);
				nodeList.add(child);
			}
		}
		return nodeList;
	}

	/**
	 * Gets the tree all leaf nodes.
	 *
	 * @return the tree all leaf nodes
	 */
	public ArrayList<TreeNode<T1, T2>> getTreeAllLeafNodes() {
		ArrayList<TreeNode<T1, T2>> tempList = new ArrayList<TreeNode<T1, T2>>();
		ArrayList<TreeNode<T1, T2>> nodeList = new ArrayList<TreeNode<T1, T2>>();

		tempList.add(this.root);
		while (!tempList.isEmpty()) {
			TreeNode<T1, T2> currentNode = tempList.remove(0);
			if (currentNode.isLeaf()) {
				nodeList.add(currentNode);
			}
			for (TreeNode<T1, T2> child : currentNode.getChildren()) {
				tempList.add(child);
			}
		}
		return nodeList;
	}

	/**
	 * Prints the out.
	 */
	public void printOut() {
		System.out.println("Print out the content of a tree");
		ArrayList<TreeNode<T1, T2>> nodeList = this.getTreeNodeInDFSOrder();
		for (TreeNode<T1, T2> treeNode : nodeList) {
			System.out.println(treeNode.toString());
		}
	}

}
