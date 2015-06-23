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

import java.util.concurrent.atomic.AtomicInteger;

// TODO: Auto-generated Javadoc
/**
 * The Class CFGEdge.
 *
 * @param <T1> the generic type
 * @param <T2> the generic type
 */
public class CFGEdge<T1, T2> {

	/** The edge counter. */
	private static AtomicInteger EDGE_COUNTER = new AtomicInteger();

	/** The edge id. */
	private int edgeId;

	/** The source. */
	private CFGNode<T1, T2> source;

	/** The destination. */
	private CFGNode<T1, T2> destination;

	/**
	 * Instantiates a new cFG edge.
	 *
	 * @param s the s
	 * @param d the d
	 */
	public CFGEdge(CFGNode<T1, T2> s, CFGNode<T1, T2> d) {
		this.setEdgeId();
		this.setSource(s);
		this.setDestination(d);
	}

	/**
	 * Gets the edge id.
	 *
	 * @return the edge id
	 */
	public int getEdgeId() {
		return edgeId;
	}

	/**
	 * Sets the edge id.
	 */
	public void setEdgeId() {
		this.edgeId = CFGEdge.EDGE_COUNTER.getAndIncrement();
	}

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public CFGNode<T1, T2> getSource() {
		return source;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the source
	 */
	public void setSource(CFGNode<T1, T2> source) {
		this.source = source;
	}

	/**
	 * Gets the destination.
	 *
	 * @return the destination
	 */
	public CFGNode<T1, T2> getDestination() {
		return destination;
	}

	/**
	 * Sets the destination.
	 *
	 * @param destination the destination
	 */
	public void setDestination(CFGNode<T1, T2> destination) {
		this.destination = destination;
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
		String str = "Edge " + this.edgeId + " from Node "
				+ this.source.toString() + " to Node "
				+ this.destination.toString();
		return str;
	}

}
