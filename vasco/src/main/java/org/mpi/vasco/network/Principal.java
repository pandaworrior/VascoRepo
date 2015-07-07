/*
 * This class is doing x;
 * Created by @Creator on @Date
 */
package org.mpi.vasco.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;

// TODO: Auto-generated Javadoc
/**
 * The Class Principal.
 */
public class Principal {
	
	/** The host. */
	InetAddress host;
	
	/** The port. */
	int port;
	
	/** The count. */
	static int count = 0;
	
	/** The uniqueid. */
	int uniqueid;
	
	/** The isa. */
	InetSocketAddress isa;

	/**
	 * Reset unique identifiers.
	 */
	public static void resetUniqueIdentifiers() {
		count = 0;
	}

	/**
	 * Instantiates a new principal.
	 *
	 * @param host the host
	 * @param port the port
	 */
	public Principal(String host, int port) {
		try {
			this.host = InetAddress.getByName(host);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.port = port;
		uniqueid = count++;
		this.isa = new InetSocketAddress(host, port);
		//System.out.println("inet address is:  " + isa);

	}

	/**
	 * Instantiates a new principal.
	 *
	 * @param host the host
	 * @param port the port
	 */
	public Principal(InetAddress host, int port) {
		this.host = host;
		this.port = port;
		uniqueid = count++;
		this.isa = new InetSocketAddress(host, port);
		System.out.println("ip address is:  " + isa);
	}

	/**
	 * Gets the inet socket address.
	 *
	 * @return the inet socket address
	 */
	public InetSocketAddress getInetSocketAddress() {
		return isa;
	}

	/**
	 * Gets the host.
	 *
	 * @return the host
	 */
	public InetAddress getHost() {
		return host;
	}

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Gets the unique id.
	 *
	 * @return the unique id
	 */
	public int getUniqueId() {
		return uniqueid;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "<" + getHost() + ":" + getPort() + " ** " + getUniqueId() + ">";
	}
}