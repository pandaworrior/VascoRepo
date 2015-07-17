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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.mpi.vasco.util.debug.Debug;

/**
 * The Class ConflictTable stores all conflict pairs <opName, List<opName>>.
 */
public class ConflictTable {

	/** The all conflicts. Not thread-safe, but it is fine since it is not modified at runtime*/
	HashMap<String, Conflict> allConflicts;
	
	private final static String CONFLICT_COLLECTION_STR = "collectionOfConflicts";
	private final static String CONFLICT_NUM_STR = "numOfConflicts";
	private final static String CONFLICT_STR = "conflict";
	private final static String LEFT_OPERANT_STR = "leftOperand";
	private final static String RIGHT_OPERANT_STR = "rightOperand";
	
	
	public ConflictTable(String xmlFile){
		this.setAllConflicts(new HashMap<String, Conflict>());
		this.readFromXml(xmlFile);
	}
	
	public void readFromXml(String xmlFile){
		int numOfConflicts = 0;
		try{
		    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		    InputStream in  = new FileInputStream(xmlFile);
		    XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
		   
		    
		    while (eventReader.hasNext()){
				XMLEvent event = eventReader.nextEvent();
	
				if (event.isStartElement()){
				    StartElement startElement = event.asStartElement();
				    
				    if (startElement.getName().getLocalPart() == CONFLICT_COLLECTION_STR){
					    Debug.println(startElement);
						Iterator<Attribute> attributes = startElement.getAttributes();
						while (attributes.hasNext()) {
						    Attribute attribute = attributes.next();
						    if (attribute.getName().toString().equals(CONFLICT_NUM_STR)){
						    	numOfConflicts = Integer.parseInt(attribute.getValue());
						    }
						}
				    }
				    
				    if (startElement.getName().getLocalPart() == CONFLICT_STR){
				    	//Debug.println("\t"+startElement);
				    	Iterator<Attribute> attributes = startElement.getAttributes();
				    	String leftOpName = null;
				    	String rightOpName = null;
				    	while (attributes.hasNext()) {
				    		Attribute attribute = attributes.next();
							if (attribute.getName().toString().equals(LEFT_OPERANT_STR)){
				    			leftOpName = attribute.getValue();
							}else if (attribute.getName().toString().equals(RIGHT_OPERANT_STR)){
								rightOpName = attribute.getValue();
							}else
								throw new RuntimeException("invalid attribute");
				    	}
				    	if(leftOpName == null || 
				    			rightOpName == null){
				    		throw new RuntimeException("Either left op name or right op name is not set");
				    	}
				    	this.addConflict(leftOpName, rightOpName);
				    }
				}
			}
		    
		    if(numOfConflicts != this.getNumOfConflicts()){
		    	throw new RuntimeException("The num of conflicts "+this.getNumOfConflicts()+" " +
		    			"is not equal to the specified number " + numOfConflicts);
		    }
		    
		    in.close();
		} catch(Exception e){
		    throw new RuntimeException(e);
		}
	}
	
	public void addConflict(String opName1, String opName2){
		Conflict c1 = this.getConflictByOpName(opName1);
		Conflict c2 = this.getConflictByOpName(opName2);
		c1.addConflict(opName2);
		c2.addConflict(opName1);
	}
	
	public Conflict getConflictByOpName(String opName){
		Conflict c = this.getAllConflicts().get(opName);
		if(c == null){
			c = new Conflict(opName);
			this.allConflicts.put(opName, c);
		}
		return c;
	}

	public HashMap<String, Conflict> getAllConflicts() {
		return allConflicts;
	}

	public void setAllConflicts(HashMap<String, Conflict> allConflicts) {
		this.allConflicts = allConflicts;
	}
	
	public int getNumOfConflicts(){
		int numOfConflicts = 0;
		Iterator it = this.getAllConflicts().entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Conflict> e = (Map.Entry<String, Conflict>)it.next();
			numOfConflicts += e.getValue().size();
		}
		return numOfConflicts;
	}
	
	public String toString(){
		StringBuilder strB = new StringBuilder();
		Iterator it = this.getAllConflicts().entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Conflict> e = (Map.Entry<String, Conflict>)it.next();
			strB.append("\t");
			strB.append(e.getValue().toString());
			strB.append("\n");
		}
		return strB.toString();
	}
	
	public void printOut(){
		Debug.println("-----------> Conflict table<---------------\n");
		Debug.println(this.toString());
		Debug.println("-----------> End of printing Conflict table <--------------");
	}
	
	public static void main(String[] args){
		if(args.length != 1){
			System.err.println("ConflictTable [configXmlFilePath]");	
			System.exit(-1);
		}
		
		String xmlFilePath = args[0];
		
		ConflictTable confTable = new ConflictTable(xmlFilePath);
		confTable.printOut();
	}
}
