/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mpi.vasco.network.netty;

import org.mpi.vasco.network.DiscardNetworkQueue;
import org.mpi.vasco.network.NetworkSender;
import org.mpi.vasco.util.UnsignedTypes;

import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import static org.jboss.netty.buffer.ChannelBuffers.*;

/**
 * @author manos
 */
public class NettyTCPSender implements NetworkSender {
    private Hashtable<Object, NettyFutureListener> nfls;
    private Hashtable<Object, ChannelFuture> pendingConnects;
    ClientBootstrap clientBootstrap;

    public NettyTCPSender(){
	this(1);
    }

    public void setTCPNoDelay(boolean nodelay){
    	System.out.println("sender set tpcnodelay " + nodelay);
	clientBootstrap.setOption("tcpNoDelay", nodelay);
    }
    public void setKeepAlive(boolean alive){
    	System.out.println("sender set keepAlive " + alive);
	clientBootstrap.setOption("keepAlive", alive);
    }


    public NettyTCPSender(int threadCount) {
    	System.out.println("sender set up threads " + threadCount);
        clientBootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool(), threadCount));

        clientBootstrap.setOption("tcpNoDelay", true);
        clientBootstrap.setOption("keepAlive", true);
	

        DiscardNetworkQueue dnq = new DiscardNetworkQueue();
        clientBootstrap.setPipelineFactory(new TxStorePipelineFactory(dnq));

	nfls = new Hashtable<Object, NettyFutureListener>();
        pendingConnects = new Hashtable<Object, ChannelFuture>();
    }


    // public void send(byte[] m, int rcptId){
    // 	send(m, membership.getPrincipal(rcptId);
    // }

    public void send(byte[] m, InetSocketAddress recipient){
	byte[] lenBytes = UnsignedTypes.longToBytes((long) m.length);
	byte[] allBytes = new byte[m.length + 8]; // MARKER change
	System.arraycopy(lenBytes, 0, allBytes, 0, 4);
	System.arraycopy(m, 0, allBytes, 4, m.length);
	System.arraycopy(lenBytes, 0, allBytes, m.length + 4, 4);
	// sending the bytes with a header/footer corresponding to length of actual bytes


	NettyFutureListener nfl = nfls.get(recipient);
	if (nfl == null){
	    synchronized(nfls){
		nfl = nfls.get(recipient);
		if (nfl == null)
		    nfls.put(recipient, new NettyFutureListener());
		nfl = nfls.get(recipient);
	    }
	}
	Channel channel = nfl.getChannel();
	if (channel == null || !channel.isOpen()){
            synchronized (pendingConnects) {
                
                nfl.addMessage(allBytes);
                ChannelFuture future = pendingConnects.get(recipient);
                if (future == null || ( future.isDone()  &&
                                       !future.getChannel().isOpen())) {
                        pendingConnects.remove(recipient);
                        System.out.println("Attempting to connect to : "+recipient);
                        future = clientBootstrap.connect(recipient);
                        pendingConnects.put(recipient, future);
                    }                    
                
                future.addListener(nfl);
            }
	} else{
	    ChannelBuffer buf = copiedBuffer(allBytes);
	    ChannelFuture lastWriteFuture = channel.write(buf);
	}
    }

    public void send(byte[] m, InetSocketAddress[] recipients){
	System.out.println("really ought to optimize this to avoid extraneous calls to system.arraycopy.  NettyTCPSender.send(byte[], principal[]");
	for (int i = 0; i < recipients.length; i++)
	    send(m, recipients[i]);
    }

}
