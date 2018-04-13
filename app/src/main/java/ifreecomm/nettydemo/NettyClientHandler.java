package ifreecomm.nettydemo;

import android.util.Log;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;


public class NettyClientHandler extends SimpleChannelInboundHandler<String> {

    private static final String TAG = "NettyClientHandler";
    private NettyListener listener;

//    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Heartbeat"+System.getProperty("line.separator"),
//            CharsetUtil.UTF_8));
//    byte[] requestBody = {(byte) 0xFE, (byte) 0xED, (byte) 0xFE, 5,4, (byte) 0xFF,0x0a};


    public NettyClientHandler(NettyListener listener) {
        this.listener = listener;
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                ctx.channel().writeAndFlush("Heartbeat"+System.getProperty("line.separator"));
            }
        }
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
//        NettyClient.getInstance().setConnectStatus(true);
        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_SUCCESS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAG, "channelInactive");
//        NettyClient.getInstance().setConnectStatus(false);
//        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_CLOSED);
       // NettyClient.getInstance().reconnect();
    }

    /**
     * 客户端收到消息
     *
     * @param channelHandlerContext
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String byteBuf) throws Exception {
        Log.e(TAG, "channelRead0");
        listener.onMessageResponse(byteBuf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
//        NettyClient.getInstance().setConnectStatus(false);
        Log.e(TAG, "exceptionCaught");
        listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_ERROR);
        cause.printStackTrace();
        ctx.close();
    }
}
