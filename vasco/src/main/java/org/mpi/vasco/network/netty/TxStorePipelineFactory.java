/*
 *    CHECKED BY ALLEN 2011.04.19
 */

package org.mpi.vasco.network.netty;

import org.mpi.vasco.network.NetworkQueue;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

/**
 *
 * @author manos
 */
public class TxStorePipelineFactory implements ChannelPipelineFactory {

    NetworkQueue NWQ;
    
    public TxStorePipelineFactory(NetworkQueue nwq) {
        NWQ = nwq;
    }
    
    public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = Channels.pipeline();
	//System.out.println(pipeline.getClass());
        ServerHandler handler = new ServerHandler(NWQ);
        MessageDecoder decoder = new MessageDecoder();
        pipeline.addLast("decoder", decoder);
        pipeline.addLast("handler", handler);
        return pipeline;
    }


}
