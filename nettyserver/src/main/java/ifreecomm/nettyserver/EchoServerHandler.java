package ifreecomm.nettyserver;

import android.util.Log;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


@ChannelHandler.Sharable                                        //1
public class EchoServerHandler extends SimpleChannelInboundHandler<String> {

    private static final String TAG = "EchoServerHandler";
    private NettyListener mListener;

    public EchoServerHandler(NettyListener listener) {
        this.mListener = listener;
    }

/*	@Override
    public void channelRead(ChannelHandlerContext ctx,
                            Object msg) {
		
        
//        ctx.write(in); 
//        ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(in.toString(Charset.forName("GBK"))+System.getProperty("line.separator"),
//                Charset.forName("GBK")));//3
//        ctx.channel().writeAndFlush(HEARTBEAT_SEQUENCE);
    }*/

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//    	System.out.println("channelReadComplete");
//        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)//4
//                .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();                //5
        ctx.close();                            //6
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        mListener.onMessageResponse(msg);
    }

    /**
     * 连接成功
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAG, "channelActive");
        mListener.onChannel(ctx.channel());
        EchoServer.getInstance().setConnectStatus(true);
        mListener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_SUCCESS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAG, "channelInactive");
        EchoServer.getInstance().setConnectStatus(false);
        mListener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_CLOSED);
        // NettyClient.getInstance().reconnect();
    }
}
