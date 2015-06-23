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
package org.mpi.vasco.sieve.staticanalysis.templatecreator.template;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.sieve.staticanalysis.templatecreator.StaticFingerPrintGenerator;

/**
 * The Class ShadowOperationTemplate.
 */
public class ShadowOperationTemplate {
	
	/** The method identifier. */
	private String methodIdentifier = "template";
	
	/** The template id count. */
	private static AtomicInteger templateIdCount = new AtomicInteger();
	
	/** The crdt op list. */
	private List<Operation> crdtOpList;
	
	/** The finger print. */
	private String fingerPrint;
	
	private int shdOpTemplateId;
	
	/**
	 * Instantiates a new shadow operation template.
	 */
	public ShadowOperationTemplate(){
		this.crdtOpList = new ArrayList<Operation>();
		this.setShdOpTemplateId(this.getNextId());
	}
	
	/**
	 * Instantiates a new shadow operation template.
	 *
	 * @param opList the op list
	 */
	public ShadowOperationTemplate(List<Operation> opList){
		this.setCrdtOpList(opList);
		this.setShdOpTemplateId(this.getNextId());
	}
	
	/**
	 * Instantiates a new shadow operation template.
	 *
	 * @param opList the op list
	 * @param methodId the method id
	 */
	public ShadowOperationTemplate(List<Operation> opList, String methodId){
		this.setCrdtOpList(opList);
		this.setMethodIdentifier(methodId);
		this.setShdOpTemplateId(this.getNextId());
	}

	/**
	 * Gets the crdt op list.
	 *
	 * @return the crdt op list
	 */
	public List<Operation> getCrdtOpList() {
		return crdtOpList;
	}

	/**
	 * Sets the crdt op list.
	 *
	 * @param crdtOpList the new crdt op list
	 */
	public void setCrdtOpList(List<Operation> crdtOpList) {
		this.crdtOpList = crdtOpList;
	}
	
	/**
	 * Adds the operation.
	 *
	 * @param op the op
	 */
	public void addOperation(Operation op){
		this.crdtOpList.add(op);
	}

	/**
	 * Gets the method identifier.
	 *
	 * @return the method identifier
	 */
	public String getMethodIdentifier() {
		return methodIdentifier;
	}

	/**
	 * Sets the method identifier.
	 *
	 * @param methodIdentifier the new method identifier
	 */
	public void setMethodIdentifier(String methodIdentifier) {
		this.methodIdentifier = methodIdentifier;
	}
	
	/**
	 * Gets the next id.
	 *
	 * @return the next id
	 */
	private int getNextId(){
		return templateIdCount.getAndIncrement();
	}
	
	/**
	 * Gets the unique template id.
	 *
	 * @return the unique template id
	 */
	public String getUniqueTemplateId(){
		return this.getMethodIdentifier() + this.getShdOpTemplateId();
	}

	/**
	 * Gets the finger print.
	 *
	 * @return the fingerPrint
	 */
	public String getFingerPrint() {
		return fingerPrint;
	}

	/**
	 * Sets the finger print.
	 *
	 * @param fingerPrint the fingerPrint to set
	 */
	public void setFingerPrint(String fingerPrint) {
		this.fingerPrint = fingerPrint;
	}
	
	/**
	 * Sets the signature.
	 */
	public void setSignature() {
		this.setFingerPrint(StaticFingerPrintGenerator.computeFingerPrint(this.getCrdtOpList()));
		Debug.println("my signature is: " + this.getFingerPrint());
	}

	/**
	 * @param shdOpTemplateId the shdOpTemplateId to set
	 */
	public void setShdOpTemplateId(int shdOpTemplateId) {
		this.shdOpTemplateId = shdOpTemplateId;
	}

	/**
	 * @return the shdOpTemplateId
	 */
	public int getShdOpTemplateId() {
		return shdOpTemplateId;
	}
}
