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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
	List<Map<String, Conflict>> conflicts;
	
	/** The Constant CONFLICT_COLLECTION_STR. */
	private final static String CONFLICT_COLLECTION_STR = "collectionOfConflicts";
	
	/** The Constant CONFLICT_NUM_STR. */
	private final static String CONFLICT_NUM_STR = "numOfConflicts";
	
	/** The Constant CONFLICT_STR. */
	private final static String CONFLICT_STR = "conflict";
	
	/** The Constant CONFLICT_TYPE_STR. */
	private final static String CONFLICT_TYPE_STR = "type";
	
	/** The Constant ASYMMETRY_CONFLICT_STR. */
	private final static String ASYMMETRY_CONFLICT_STR = "asymmetry";
	
	/** The Constant SYMMETRY_CONFLICT_STR. */
	private final static String SYMMETRY_CONFLICT_STR = "symmetry";
	
	/** The Constant LEFT_OPERANT_STR. */
	private final static String LEFT_OPERANT_STR = "leftOperand";
	
	/** The Constant RIGHT_OPERANT_STR. */
	private final static String RIGHT_OPERANT_STR = "rightOperand";
	
	/** The Constant BARRIER_STR. */
	private final static String BARRIER_STR = "barrier";
	
	/** The Constant YES_STR. */
	private final static String YES_STR = "yes";
	
	/** The Constant NO_STR. */
	private final static String NO_STR = "no";
	
	/**
	 * Instantiates a new conflict table.
	 *
	 * @param xmlFile the xml file
	 */
	public ConflictTable(String xmlFile){
		this.setIntialConflicts();
		this.readFromXml(xmlFile);
	}
	
	/**
	 * Gets the conflicts.
	 *
	 * @return the conflicts
	 */
	public List<Map<String, Conflict>> getConflicts() {
		return conflicts;
	}

	/**
	 * Sets the intial conflicts.
	 */
	public void setIntialConflicts() {
		this.conflicts = new ArrayList<Map<String, Conflict>>();
		for(int i = 0; i < Protocol.NUM_OF_PROTOCOLS; i++){
			this.conflicts.add(new HashMap<String, Conflict>());
		}
	}
	
	/**
	 * Read from xml.
	 *
	 * @param xmlFile the xml file
	 */
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
	
	/**
	 * Adds the sym conflict.
	 *
	 * @param opName1 the op name1
	 * @param opName2 the op name2
	 */
	public void addSymConflict(String opName1, String opName2){
		Conflict c1 = this.getConflictByOpName(opName1, Protocol.PROTOCOL_SYM);
		Conflict c2 = this.getConflictByOpName(opName2, Protocol.PROTOCOL_SYM);
		c1.addConflict(opName2);
		c2.addConflict(opName1);
	}
	
	/**
	 * Adds the asym conflict.
	 *
	 * @param opName1 the op name1
	 * @param opName2 the op name2
	 * @param barrier the barrier
	 */
	public void addAsymConflict(String opName1, String opName2, boolean barrier){
		Conflict c1 = this.getConflictByOpName(opName1, Protocol.PROTOCOL_ASYM);
		Conflict c2 = this.getConflictByOpName(opName2, Protocol.PROTOCOL_ASYM);
		c1.addConflict(opName2);
		c2.addConflict(opName1);
		if(barrier){
			c1.setBarrier(true);
		}
	}
	
	/**
	 * Gets the conflict by op name.
	 *
	 * @param opName the op name
	 * @param conflictType the conflict type
	 * @return the conflict by op name
	 */
	public Conflict getConflictByOpName(String opName, int conflictType){
		Conflict c = null;
		
		switch(conflictType){
		case Protocol.PROTOCOL_ASYM:
		case Protocol.PROTOCOL_SYM:
			c = this.getConflicts().get(conflictType).get(opName);
			if(c == null){
				c = new Conflict(opName);
				this.getConflicts().get(conflictType).put(opName, c);
			}
			break;
			default:
				throw new RuntimeException("No such conflict type " + conflictType);
		}

		return c;
	}
	
	public Map<String, Conflict> getConflictsByType(int conflictType){
		return this.getConflicts().get(conflictType);
	}
	
	
	public String getRandomConflictOpNameByType(int conflictType){
		Map<String, Conflict> conflictsMap = this.getConflictsByType(conflictType);
		int sizeOfMap = conflictsMap.size();
		
		if(sizeOfMap != 0){
			Random random = new Random();
			int pos = random.nextInt(sizeOfMap);
			
			for(String conflictName : conflictsMap.keySet()){
				if(pos == 0){
					return conflictName;
				}
				pos--;
			}
		}
		
		throw new RuntimeException("No such conflicts for protocol " + conflictType);
	}
	
	/**
	 * Gets the num of conflicts.
	 *
	 * @return the num of conflicts
	 */
	public int getNumOfConflicts(){
		int numOfConflicts = 0;
		for(int i = 0 ; i < Protocol.NUM_OF_PROTOCOLS; i++){
			Iterator it = this.conflicts.get(i).entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String, Conflict> e = (Map.Entry<String, Conflict>)it.next();
				numOfConflicts += e.getValue().size();
			}
		}
		return numOfConflicts;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuilder strB = new StringBuilder("");
		for(int i = 0 ; i < Protocol.NUM_OF_PROTOCOLS; i++){
			String tagString = Protocol.getProtocolTagString(i);
			strB.append(tagString + "\n");
			Iterator it = this.conflicts.get(i).entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String, Conflict> e = (Map.Entry<String, Conflict>)it.next();
				strB.append("\t");
				strB.append(e.getValue().toString());
				strB.append("\n");
			}
		}
		return strB.toString();
	}
	
	/**
	 * Prints the out.
	 */
	public void printOut(){
		Debug.println("-----------> Conflict table<---------------\n");
		Debug.println(this.toString());
		Debug.println("-----------> End of printing Conflict table <--------------");
	}
	
	/**
	 * Gets the protocol type.
	 *
	 * @param opName the op name
	 * @return the protocol type
	 */
	public int[] getProtocolType(String opName){
		int[] pTypes = new int[Protocol.NUM_OF_PROTOCOLS];
		for(int i = 0; i < Protocol.NUM_OF_PROTOCOLS; i++){
			if(this.conflicts.get(i).containsKey(opName)){
				pTypes[i] = 1;
			}else{
				pTypes[i] = 0;
			}
		}
		return pTypes;
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
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
