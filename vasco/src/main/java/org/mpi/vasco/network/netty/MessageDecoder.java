// CHECKED BY ALLEN 2011.04.19


package org.mpi.vasco.network.netty;

import org.mpi.vasco.util.UnsignedTypes;
import org.mpi.vasco.util.Pair;
import org.mpi.vasco.util.debug.Debug;

import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channel;

public class MessageDecoder extends FrameDecoder {

    public MessageDecoder() { }
    
    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) {
        //if (buffer.readableBytes() % 100 == 0 && buffer.readableBytes() > 10)
       // System.out.println(buffer.readableBytes());
    	if(buffer.readableBytes() < 4) { // we still dont know the length
    	    return null;
    	} else {
            byte[] lenBytes = new byte[4];

            buffer.markReaderIndex();
            buffer.readBytes(lenBytes, 0, 4);

    	    int len = (int)UnsignedTypes.bytesToLong(lenBytes);
    	    if(buffer.readableBytes() < len+4) {
                buffer.resetReaderIndex();
                return null;
    	    } else {
    	    	byte[] messageBytes = new byte[len];
    	    	
    	    	buffer.readBytes(messageBytes,0,len);
                
    	    	//I can reuse the lenBytes buffer here to read the marker
    	    	buffer.readBytes(lenBytes,0,4);
    	    	int marker = (int)UnsignedTypes.bytesToLong(lenBytes);
    	    	if(marker != len) {
                    UnsignedTypes.printBytes(messageBytes);
                    System.out.println();
                    UnsignedTypes.printBytes(lenBytes);
                    System.out.println();
                    Debug.kill(new RuntimeException("invalid marker "+len + " "+marker));
                }
    	    	
    	    	Pair<Integer, byte[]> parsedMsgBytes = new Pair<Integer, byte[]>(0, messageBytes);
                
    	    	return parsedMsgBytes;
    	    }
    	}
    }
}