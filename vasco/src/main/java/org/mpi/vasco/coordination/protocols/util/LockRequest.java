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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class LockRequest.
 */
public class LockRequest{
	
	/** The op name. */
	String opName;
	
	/** The key list. */
	List<String> keyList;
	
	byte[] arr;
	
	/**
	 * Instantiates a new lock request.
	 *
	 * @param _opName the _op name
	 * @param _keyList the _key list
	 */
	public LockRequest(String _opName, List<String> _keyList){
		this.setOpName(_opName);
		this.setKeyList(_keyList);
		this.setArr(null);
	}
	
	/**
	 * Instantiates a new lock request.
	 *
	 * @param _opName the _op name
	 */
	public LockRequest(String _opName){
		this.setOpName(_opName);
		this.keyList = new ArrayList<String>();
		this.setArr(null);
	}

	public LockRequest(byte[] b, int offset) {
		// TODO Auto-generated constructor stub
		this.setArr(new byte[b.length - offset]);
		System.arraycopy(b, offset, this.getArr(), 0, b.length - offset);
		try {
			this.decode();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Gets the op name.
	 *
	 * @return the op name
	 */
	public String getOpName() {
		return opName;
	}

	/**
	 * Sets the op name.
	 *
	 * @param opName the new op name
	 */
	public void setOpName(String opName) {
		this.opName = opName;
	}

	/**
	 * Gets the key list.
	 *
	 * @return the key list
	 */
	public List<String> getKeyList() {
		return keyList;
	}

	/**
	 * Sets the key list.
	 *
	 * @param keyList the new key list
	 */
	public void setKeyList(List<String> keyList) {
		this.keyList = keyList;
	}
	
	/**
	 * Adds the key.
	 *
	 * @param _key the _key
	 */
	public void addKey(String _key){
		this.keyList.add(_key);
	}

	@Override
    public String toString(){
		StringBuilder strBuild = new StringBuilder();
		strBuild.append("OpName: " + this.getOpName()+"\t");
		for(int i = 0; i < this.getKeyList().size();i++){
			strBuild.append("key " + i + " " + this.getKeyList().get(i) + "\t");
		}
		return strBuild.toString();
    }
	
	private void decode() throws IOException{
		ByteArrayInputStream bais = new ByteArrayInputStream(this.getArr());
		DataInputStream dis = new DataInputStream(bais);
		this.setOpName(dis.readUTF());
		int numOfKeys = dis.readInt();
		this.setKeyList(new ArrayList<String>());
		while(numOfKeys > 0){
			this.addKey(dis.readUTF());
			numOfKeys--;
		}
	}
	
	private void encode() throws IOException{
		if(this.getArr() == null){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeUTF(this.getOpName());
			dos.writeInt(this.getKeyList().size());
			for(String key : this.getKeyList()){
				dos.writeUTF(key);
			}
			this.setArr(baos.toByteArray());
		}
	}
	
	public byte[] getBytes(){
		try {
			this.encode();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		return this.getArr();
	}
	
	public int getByteSize(){
		return this.getBytes().length;
	}
	
	private byte[] getArr(){
		return this.arr;
	}

	private void setArr(byte[] arr) {
		this.arr = arr;
	}
}
