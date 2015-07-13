/*******************************************************************************
 * Copyright (c) 2013 Cheng Li.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Cheng Li - initial API and implementation
 * 
 * Contact:
 *     To distribute or use this code requires prior specific permission.
 *     In this case, please contact chengli@mpi-sws.org.
 ******************************************************************************/
package org.mpi.vasco.util.debug;

// TODO: Auto-generated Javadoc
/**
 * The Class Debug.
 */
public class Debug {

	/** The debug. */
	public static boolean debug = true;

	/** The profile. */
	public static boolean profile = false;

	/**
	 * Log.
	 *
	 * @param module the module
	 * @param level the level
	 * @param format the format
	 * @param args the args
	 */
	public static void log(boolean module, boolean level, String format,
			Object[] args) {
		if (module && level) {
			System.err.printf(format, args);
		}
	}

	/**
	 * Println.
	 *
	 * @param obj the obj
	 */
	public static void println(Object obj) {
		if (debug) {
			System.err.println(obj);
		}
	}

	/**
	 * Println.
	 *
	 * @param str the str
	 */
	public static void println(String str) {
		if (debug) {
			System.err.println(str);
		}
	}

	/**
	 * Printf.
	 *
	 * @param format the format
	 * @param args the args
	 */
	
	public static void printf(String format, Object... args){
    	if (debug){
    		System.err.printf(format, args);
    	}
    }

	/**
	 * Println.
	 *
	 * @param cond the cond
	 * @param st the st
	 */
	public static void println(boolean cond, Object st) {
		if (debug && cond) {
			System.err.println(st);
		}
	}

	/**
	 * Println.
	 */
	public static void println() {
		if (debug) {
			// System.err.println();
		}
	}

	/**
	 * Prints the.
	 *
	 * @param obj the obj
	 */
	public static void print(Object obj) {
		if (debug) {
			System.err.print(obj);
		}
	}

	/**
	 * Kill.
	 *
	 * @param e the e
	 */
	static public void kill(Exception e) {
		e.printStackTrace();
		System.exit(0);
	}

	/**
	 * Kill.
	 *
	 * @param st the st
	 */
	public static void kill(String st) {
		kill(new RuntimeException(st));
	}

	/** The baseline. */
	static protected long baseline = 0;// System.currentTimeMillis() - 1000000;

	/**
	 * Profile start.
	 *
	 * @param s the s
	 */
	public static void profileStart(String s) {
		if (!profile) {
			return;
		}
		String tmp = Thread.currentThread() + " " + s + " START "
				+ (System.currentTimeMillis() - baseline);
		System.err.println(tmp);
	}

	/**
	 * Profile finis.
	 *
	 * @param s the s
	 */
	public static void profileFinis(String s) {
		if (!profile) {
			return;
		}
		String tmp = Thread.currentThread() + " " + s + " FINIS "
				+ (System.currentTimeMillis() - baseline);
		System.err.println(tmp);
	}

}
