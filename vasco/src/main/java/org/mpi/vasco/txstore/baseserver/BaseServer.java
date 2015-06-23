/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mpi.vasco.txstore.baseserver;

  

import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.txstore.BaseNode;
import org.mpi.vasco.txstore.membership.Role;

import org.mpi.vasco.txstore.messages.MessageFactory;
import org.mpi.vasco.txstore.messages.MessageTags;
import org.mpi.vasco.txstore.messages.MessageBase;

// receiving messages
import org.mpi.vasco.txstore.messages.OperationMessage;


// sending messages
import org.mpi.vasco.txstore.messages.ResultMessage;


////Cross data center/remote transaction messages



import org.mpi.vasco.txstore.util.Result;

import java.io.IOException;


//for logging
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author aclement
 * 
 * This Class is intended to be a simple server that receives an operation 
 * message and delivers it to the storage service as a complete transaction.
 * 
 * Every object should be considered red by the scratchpad -- this server is 
 * intended to provide a baseline for network round trip latencies and should 
 * be minimally impacted by computation
 */
public class BaseServer extends BaseNode{
 


	MessageFactory mf;
	long lastTxnCount = 0;
        ServerApplication app;
        

	private static Logger sshimLogger = Logger.getLogger(BaseServer.class
			.getName());
	private static FileHandler fh;

	public BaseServer(String file, int dc, int id, ServerApplication sapp) {
		super(file, dc, Role.STORAGE, id);
		this.mf = new MessageFactory();
                app = sapp;

		String logFileName = "storageshim" + Integer.toString(id) + "-.log";
		try {
			fh = new FileHandler(logFileName, true);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		sshimLogger.setLevel(Level.INFO);
		SimpleFormatter formatter = new SimpleFormatter();
		fh.setFormatter(formatter);
		sshimLogger.addHandler(fh);
	}

	/***
	 * handle incoming messages. implements ByteHandler
	 ***/
	public void handle(byte[] b) {
		MessageBase msg = mf.fromBytes(b);
		Debug.println(msg);
		if (msg == null)
			throw new RuntimeException("Should never receive a null message");
		switch (msg.getTag()) {
		case MessageTags.OPERATION:
			process((OperationMessage) msg);
			return;
		default:
			throw new RuntimeException("invalid message tag: " + msg.getTag());
		}

	}


	public void process(OperationMessage msg) {
            Result res = app.execute(msg.getOperation());
                    
            ResultMessage rm = new ResultMessage(msg.getTxnId(), res, 
                                             msg.getOperationId());
	    
            sendToProxy(rm, msg.getTxnId().getProxyId());
	}

            
}
