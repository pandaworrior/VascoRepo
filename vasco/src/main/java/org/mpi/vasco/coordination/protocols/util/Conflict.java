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
package org.mpi.vasco.coordination.protocols.util;

import java.util.HashSet;
import java.util.Set;

/**
 * The Class Conflict for a restriction between an operation and the list of operations.
 */
public class Conflict {
	String opName;
	Set<String> confList;
	boolean isBarrier;// true => in this restriction, the other part is the barrier
	
	public Conflict(String _opName){
		this.setOpName(_opName);
		this.setConfList(new HashSet<String>());
		this.setBarrier(false);
	}

	public String getOpName() {
		return opName;
	}

	public void setOpName(String opName) {
		this.opName = opName;
	}

	public Set<String> getConfList() {
		return confList;
	}

	public void setConfList(Set<String> confList) {
		this.confList = confList;
	}
	
	public void addConflict(String _opName){
		this.getConfList().add(_opName);
	}
	
	public int size(){
		return this.getConfList().size();
	}
	
	public String toString(){
		StringBuilder strBuild = new StringBuilder("Conflict <");
		strBuild.append(this.getOpName());
		strBuild.append( ", {");
		for(String confName : this.getConfList()){
			strBuild.append(confName);
			strBuild.append(",");
		}
		//strBuild.deleteCharAt(strBuild.length() - 1);
		strBuild.append(isBarrier);
		strBuild.append("}>");
		return strBuild.toString();
	}

	public boolean isBarrier() {
		return isBarrier;
	}

	public void setBarrier(boolean isBarrier) {
		this.isBarrier = isBarrier;
	}
}
