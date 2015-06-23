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
package org.mpi.vasco.sieve.staticanalysis.templatecreator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTParser;

import org.mpi.vasco.util.annotationparser.SchemaParser;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.READONLY_Table;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DataField;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class DatastructureCreator.
 */
public class DatastructureCreator{
	
	/** The schema parser. */
	private SchemaParser schemaParser;
	
	/** The project name. */
	private String projectName;
	
	/**
	 * Instantiates a new datastructure creator.
	 *
	 * @param sP the s p
	 * @param projectName the project name
	 */
	public DatastructureCreator(SchemaParser sP, String projectName){
		this.setSchemaParser(sP);
		this.setProjectName(projectName);
	}
	

	/**
	 * Gets the schema parser.
	 *
	 * @return the schema parser
	 */
	public SchemaParser getSchemaParser() {
		return schemaParser;
	}

	/**
	 * Sets the schema parser.
	 *
	 * @param schemaParser the new schema parser
	 */
	public void setSchemaParser(SchemaParser schemaParser) {
		this.schemaParser = schemaParser;
	}


	/**
	 * Gets the project name.
	 *
	 * @return the project name
	 */
	public String getProjectName() {
		return projectName;
	}


	/**
	 * Sets the project name.
	 *
	 * @param projectName the new project name
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	/**
	 * Generate code.
	 */
	public void generateCode(){
		HashMap<String, DatabaseTable> tableMapping = this.getSchemaParser().getTableCrdtFormMap();
		
		//for each table, create a class for the table itself
		Iterator<Entry<String, DatabaseTable>> tableIter = tableMapping.entrySet().iterator();
		while(tableIter.hasNext()){
			Entry<String, DatabaseTable> tableEntry = tableIter.next();
			//create a table class
			//if the table is not read only, create its code
			if(!(tableEntry.getValue() instanceof READONLY_Table)){
				DatabaseTableClassCreator dtCreator = new DatabaseTableClassCreator(this.getProjectName(), tableEntry.getKey(), tableEntry.getValue());
				DatabaseRecordClassCreator dtRCreator = new DatabaseRecordClassCreator(this.getProjectName(),
						tableEntry.getKey(), tableEntry.getValue().getDataFieldList());
				dtCreator.generateCode();
				dtRCreator.generateCode();
			}
		}
	}
}
