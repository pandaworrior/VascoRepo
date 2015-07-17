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
package org.mpi.vasco.util;

import java.util.concurrent.ConcurrentLinkedQueue;

// TODO: Auto-generated Javadoc
/**
 * The Class ObjectPool.
 *
 * @param <T> the generic type
 */
public class ObjectPool<T> {
	
	/** The object list. */
	public ConcurrentLinkedQueue<T> objectList;
	
	/**
	 * Instantiates a new object pool.
	 */
	public ObjectPool(){
		objectList = new ConcurrentLinkedQueue<T>();
	}
	
	/**
	 * Adds the object.
	 *
	 * @param obj the obj
	 */
	public void addObject(T obj){
		objectList.add(obj);
	}
	
	/**
	 * Borrow object.
	 *
	 * @return the t
	 */
	public T borrowObject(){
		return objectList.poll();
	}
	
	/**
	 * Return object.
	 *
	 * @param obj the obj
	 */
	public void returnObject(T obj){
		objectList.add(obj);
	}
	
	public int numOfObject(){
		return this.objectList.size();
	}

}
