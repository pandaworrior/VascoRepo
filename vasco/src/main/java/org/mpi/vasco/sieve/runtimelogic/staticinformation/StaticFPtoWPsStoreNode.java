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
package org.mpi.vasco.sieve.runtimelogic.staticinformation;

import java.util.ArrayList;
import java.util.List;
import org.mpi.vasco.util.weakestprecondtion.WeakestPrecondition;

// TODO: Auto-generated Javadoc
/**
 * The Class StaticFPtoWPsStoreNode.
 */
public class StaticFPtoWPsStoreNode {
	
	/** The node id. */
	int nodeId;
	
	/** The op fingerprint. */
	String opFingerPrint;
	
	/** The wp. */
	WeakestPrecondition weakestPrecondition;
	
	/** The child links. */
	List<StaticFPtoWPsStoreNode> childLinks;
	
	private boolean isRoot;

	/**
	 * Instantiates a new static f pto w ps store node.
	 *
	 * @param opSign the op sign
	 */
	public StaticFPtoWPsStoreNode(int nId, String opSign){
		this.setNodeId(nId);
		this.setOpFingerPrint(opSign);
		this.setWeakestPrecondition(null);
		this.setChildLinks(null);
		this.setRoot(false);
	}
	
	/**
	 * Instantiates a new static f pto w ps store node.
	 *
	 * @param opSign the op sign
	 * @param wP the w p
	 */
	public StaticFPtoWPsStoreNode(int nId, String opSign, WeakestPrecondition wP){
		this.setNodeId(nId);
		this.setOpFingerPrint(opSign);
		this.setWeakestPrecondition(wP);
		this.setChildLinks(null);
	}
	
	/**
	 * Gets the op fingerprint.
	 *
	 * @return the op fingerprint
	 */
	public String getOpFingerPrint() {
		return opFingerPrint;
	}

	/**
	 * Sets the op fingerprint.
	 *
	 * @param opFingerPrint the new op fingerprint
	 */
	public void setOpFingerPrint(String opFingerPrint) {
		this.opFingerPrint = opFingerPrint;
	}

	/**
	 * Gets the wp.
	 *
	 * @return the wp
	 */
	public WeakestPrecondition getWeakestPrecondition() {
		return weakestPrecondition;
	}

	/**
	 * Sets the wp.
	 *
	 * @param wp the new wp
	 */
	public void setWeakestPrecondition(WeakestPrecondition wp) {
		this.weakestPrecondition = wp;
	}

	/**
	 * Gets the child links.
	 *
	 * @return the child links
	 */
	public List<StaticFPtoWPsStoreNode> getChildLinks() {
		return childLinks;
	}

	/**
	 * Sets the child links.
	 *
	 * @param childLinks the new child links
	 */
	public void setChildLinks(List<StaticFPtoWPsStoreNode> childLinks) {
		this.childLinks = childLinks;
	}
	
	/**
	 * Checks if is child contained.
	 *
	 * @param childSign the child sign
	 * @return true, if is child contained
	 */
	public boolean isChildContained(String childSign){
		for(StaticFPtoWPsStoreNode n : this.getChildLinks()){
			if(n.getOpFingerPrint().equals(childSign)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Find matching child node.
	 *
	 * @param childSign the child sign
	 * @return the static f pto w ps store node
	 */
	public StaticFPtoWPsStoreNode findMatchingChildNode(String childSign){
		if(this.getChildLinks() != null) {
			for(StaticFPtoWPsStoreNode n : this.getChildLinks()){
				if(n.getOpFingerPrint().equals(childSign)){
					return n;
				}
			}
		}
		return null;
	}
	
	/**
	 * Adds the one child node.
	 *
	 * @param sFpTWpNode the s fp t wp node
	 */
	public void addOneChildNode(StaticFPtoWPsStoreNode sFpTWpNode){
		if(this.childLinks == null){
			this.childLinks = new ArrayList<StaticFPtoWPsStoreNode>();
		}
		this.childLinks.add(sFpTWpNode);
		/*if(!this.isChildContained(sFpTWpNode.getOpFingerPrint())){
			Debug.println("Not found, don't ignore");
			this.childLinks.add(sFpTWpNode);
		}else {
			Debug.println("Found the same fingerprint, ignore");
		}*/
	}

	/**
	 * Checks if is leaf node.
	 *
	 * @return true, if is leaf node
	 */
	public boolean isLeafNode(){
		if(this.getChildLinks()==null){
			return true;
		}
		if(this.getChildLinks().size() == 0){
			return true;
		}
		return false;
	}

	/**
	 * Gets the node id.
	 *
	 * @return the nodeId
	 */
	public int getNodeId() {
		return nodeId;
	}

	/**
	 * Sets the node id.
	 *
	 * @param nodeId the nodeId to set
	 */
	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * @param isRoot the isRoot to set
	 */
	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	/**
	 * @return the isRoot
	 */
	public boolean isRoot() {
		return isRoot;
	}
	
	public boolean isLastNodeOnPath() {
		return (this.getWeakestPrecondition() != null);
	}
}
