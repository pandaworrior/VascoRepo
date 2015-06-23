/***************************************************************
Project name: georeplication
Class file name: InvariantParser.java
Created at 7:40:33 PM by chengli

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
****************************************************************/

package org.mpi.vasco.util.annotationparser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chengli
 *
 */
public class InvariantParser {
	
	String invariantFilePath;
	List<Invariant> invariants;
	
	public InvariantParser(String path) {
		this.setInvariantFilePath(path);
		this.invariants = new ArrayList<Invariant>();
		this.loadInvariantsFromFile();
	}

	/**
	 * @return the invariantFilePath
	 */
	public String getInvariantFilePath() {
		return invariantFilePath;
	}

	/**
	 * @param invariantFilePath the invariantFilePath to set
	 */
	public void setInvariantFilePath(String invariantFilePath) {
		this.invariantFilePath = invariantFilePath;
	}

	/**
	 * @return the invariants
	 */
	public List<Invariant> getInvariants() {
		return invariants;
	}

	/**
	 * @param invariants the invariants to set
	 */
	public void setInvariants(List<Invariant> invariants) {
		this.invariants = invariants;
	}
	
	public void addInvariant(Invariant inv) {
		this.invariants.add(inv);
	}
	
	public void loadInvariantsFromFile() {
		BufferedReader br;
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(this.getInvariantFilePath())));
			while ((line = br.readLine()) != null) {
				Invariant inv = new Invariant(line);
				this.addInvariant(inv);
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
	
	public List<String> getAllTables(){
		List<String> tableList = new ArrayList<String>();
		for(Invariant inv : this.getInvariants()) {
			tableList.addAll(inv.getTableList());
		}
		return tableList;
	}

}
