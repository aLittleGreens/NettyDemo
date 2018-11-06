package ifreecomm.nettydemo.netty;

import android.util.Log;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;


public class NettyClientHandler extends SimpleChannelInboundHandler<String> {

    private static final String TAG = "NettyClientHandler";
    private NettyClientListener listener;
    private int index;

//    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Heartbeat"+System.getProperty("line.separator"),
//            CharsetUtil.UTF_8));
//    byte[] requestBody = {(byte) 0xFE, (byte) 0xED, (byte) 0xFE, 5,4, (byte) 0xFF,0x0a};


    public NettyClientHandler(NettyClientListener listener, int index) {
        this.listener = listener;
        this.index = index;
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {   //发送心跳
                ctx.channel().writeAndFlush("Heartbeat" + System.getProperty("line.separator"));
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
//        NettyTcpClient.getInstance().setConnectStatus(true);
        listener.onClientStatusConnectChanged(NettyClientListener.STATUS_CONNECT_SUCCESS, index);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAG, "channelInactive");
//        NettyTcpClient.getInstance().setConnectStatus(false);
//        listener.onServiceStatusConnectChanged(NettyClientListener.STATUS_CONNECT_CLOSED);
        // NettyTcpClient.getInstance().reconnect();
    }

    /**
     * 客户端收到消息
     *
     * @param channelHandlerContext
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        Log.e(TAG, "channelRead0");
        listener.onMessageResponseClient(msg, index);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
//        NettyTcpClient.getInstance().setConnectStatus(false);
        Log.e(TAG, "exceptionCaught");
        listener.onClientStatusConnectChanged(NettyClientListener.STATUS_CONNECT_ERROR, index);
        cause.printStackTrace();
        ctx.close();
    }
}
