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

	/** The all symmetry conflicts. Not thread-safe, but it is fine since it is not modified at runtime*/
	HashMap<String, Conflict> symConflicts;
	HashMap<String, Conflict> asymConflicts;
	
	public final static byte CONFLICT_INDEX_SYM = 0;
	public final static byte CONFLICT_INDEX_ASYM = 1;
	
	private final static String CONFLICT_COLLECTION_STR = "collectionOfConflicts";
	private final static String CONFLICT_NUM_STR = "numOfConflicts";
	private final static String CONFLICT_STR = "conflict";
	private final static String CONFLICT_TYPE_STR = "type";
	private final static String ASYMMETRY_CONFLICT_STR = "asymmetry";
	private final static String SYMMETRY_CONFLICT_STR = "symmetry";
	
	private final static String LEFT_OPERANT_STR = "leftOperand";
	private final static String RIGHT_OPERANT_STR = "rightOperand";
	private final static String BARRIER_STR = "barrier";
	private final static String YES_STR = "yes";
	private final static String NO_STR = "no";
	
	public ConflictTable(String xmlFile){
		this.setSymConflicts(new HashMap<String, Conflict>());
		this.setAsymConflicts(new HashMap<String, Conflict>());
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
				    	String type = null;
				    	String barrierStr = null;
				    	while (attributes.hasNext()) {
				    		Attribute attribute = attributes.next();
				    		if (attribute.getName().toString().equals(CONFLICT_TYPE_STR)){
				    			type = attribute.getValue();
				    		}else if(attribute.getName().toString().equals(LEFT_OPERANT_STR)){
				    			leftOpName = attribute.getValue();
							}else if (attribute.getName().toString().equals(RIGHT_OPERANT_STR)){
								rightOpName = attribute.getValue();
							}else if(attribute.getName().toString().equals(BARRIER_STR)){
								barrierStr = attribute.getValue();
							}else
								throw new RuntimeException("invalid attribute");
				    	}
				    	if(leftOpName == null || 
				    			rightOpName == null){
				    		throw new RuntimeException("Either left op name or right op name is not set");
				    	}
				    	
				    	if(type.equals(ASYMMETRY_CONFLICT_STR)){
				    		Debug.println("here we check conflict str type");
				    		if(barrierStr.equals(YES_STR)){
				    			this.addAsymConflict(leftOpName, rightOpName, true);
				    		}else{
				    			this.addAsymConflict(leftOpName, rightOpName, false);
				    		}
				    	}else if(type.equals(SYMMETRY_CONFLICT_STR)){
				    		this.addSymConflict(leftOpName, rightOpName);
				    	}else{
				    		throw new RuntimeException("No such conflict type " + type + "\n" );
				    	}
				    }
				}
			}
		    
		    if(numOfConflicts != this.getNumOfConflicts()){
		    	throw new RuntimeException("The num of conflicts "+this.getNumOfConflicts()+" " +
		    			"is not equal to the specified number " + numOfConflicts);
		    }
		    
		    in.close();
		} catch(Exception e){
		    e.printStackTrace();
		    System.exit(-1);
		}
	}
	
	public void addSymConflict(String opName1, String opName2){
		Conflict c1 = this.getConflictByOpName(opName1, CONFLICT_INDEX_SYM);
		Conflict c2 = this.getConflictByOpName(opName2, CONFLICT_INDEX_SYM);
		c1.addConflict(opName2);
		c2.addConflict(opName1);
	}
	
	public void addAsymConflict(String opName1, String opName2, boolean barrier){
		Conflict c1 = this.getConflictByOpName(opName1, CONFLICT_INDEX_ASYM);
		Conflict c2 = this.getConflictByOpName(opName2, CONFLICT_INDEX_ASYM);
		c1.addConflict(opName2);
		c2.addConflict(opName1);
		if(barrier){
			c1.setBarrier(true);
		}
	}
	
	public Conflict getConflictByOpName(String opName, byte conflictType){
		Conflict c = null;
		
		switch(conflictType){
		case CONFLICT_INDEX_ASYM:
			c= this.getAsymConflicts().get(opName);
			if(c == null){
				c = new Conflict(opName);
				this.asymConflicts.put(opName, c);
			}
			break;
		case CONFLICT_INDEX_SYM:
			c = this.getSymConflicts().get(opName);
			if(c == null){
				c = new Conflict(opName);
				this.symConflicts.put(opName, c);
			}
			break;
			default:
				throw new RuntimeException("No such conflict type " + conflictType);
		}

		return c;
	}
	
	public int getNumOfConflicts(){
		int numOfConflicts = 0;
		Iterator it1 = this.getSymConflicts().entrySet().iterator();
		while(it1.hasNext()){
			Map.Entry<String, Conflict> e = (Map.Entry<String, Conflict>)it1.next();
			numOfConflicts += e.getValue().size();
		}
		
		Iterator it2 = this.getAsymConflicts().entrySet().iterator();
		while(it2.hasNext()){
			Map.Entry<String, Conflict> e = (Map.Entry<String, Conflict>)it2.next();
			numOfConflicts += e.getValue().size();
		}
		return numOfConflicts;
	}
	
	public String toString(){
		StringBuilder strB = new StringBuilder("Sym conflicts \n");
		Iterator it1 = this.getSymConflicts().entrySet().iterator();
		while(it1.hasNext()){
			Map.Entry<String, Conflict> e = (Map.Entry<String, Conflict>)it1.next();
			strB.append("\t");
			strB.append(e.getValue().toString());
			strB.append("\n");
		}
		
		strB.append("Asym conflicts \n");
		Iterator it2 = this.getAsymConflicts().entrySet().iterator();
		while(it2.hasNext()){
			Map.Entry<String, Conflict> e = (Map.Entry<String, Conflict>)it2.next();
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
	
	public HashMap<String, Conflict> getSymConflicts() {
		return symConflicts;
	}

	public void setSymConflicts(HashMap<String, Conflict> symConflicts) {
		this.symConflicts = symConflicts;
	}

	public HashMap<String, Conflict> getAsymConflicts() {
		return asymConflicts;
	}

	public void setAsymConflicts(HashMap<String, Conflict> asymConflicts) {
		this.asymConflicts = asymConflicts;
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
