/*******************************************************************************
 * Copyright (c) 2015 Dependable Cloud Group and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dependable Cloud Group - initial API and implementation
 *
 * Creator:
 *     Cheng Li
 *
 * Contact:
 *     chengli@mpi-sws.org    
 *******************************************************************************/
package org.mpi.vasco.coordination.protocols.util.asym;

// TODO: Auto-generated Javadoc
/**
 * The Class AsymCounter.
 */
public abstract class AsymCounter {
	
	/** The counter name. */
	String counterName;
	
	/**
	 * Instantiates a new asym counter.
	 *
	 * @param _counterName the _counter name
	 */
	public AsymCounter(String _counterName){
		this.setCounterName(_counterName);
	}

	public String getCounterName() {
		return counterName;
	}

	public void setCounterName(String counterName) {
		this.counterName = counterName;
	}
	
	public abstract boolean isBarrier();
	
	public boolean equals(AsymCounter asymC){
		if(this.getCounterName().equals(asymC.getCounterName())){
			return true;
		}
		return false;
	}
	
	public int hashCode(){
		return this.getCounterName().hashCode();
	}
	
	public String toString(){
		return "AsymCounter " + this.getCounterName();
	}

}
