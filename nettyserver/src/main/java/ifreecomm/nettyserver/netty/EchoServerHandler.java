package ifreecomm.nettyserver.netty;

import android.util.Log;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


@ChannelHandler.Sharable
public class EchoServerHandler extends SimpleChannelInboundHandler<String> {

    private static final String TAG = "EchoServerHandler";
    private final NettyServerListener<String> mListener;

    public EchoServerHandler(NettyServerListener<String> listener) {
        this.mListener = listener;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    	System.out.println("channelReadComplete");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();                //5
        ctx.close();
        //6
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        if(msg.equals("Heartbeat")){
            Log.d(TAG,"Heartbeat");
            return; //客户端发送来的心跳数据
        }
        mListener.onMessageResponseServer(msg,ctx.channel().id().asShortText());
    }

    /**
     * 连接成功
     * @param ctx ChannelHandlerContext
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx)  {
        Log.e(TAG, "channelActive");
        mListener.onChannelConnect(ctx.channel());
//        NettyTcpServer.getInstance().setConnectStatus(true);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)  {
        Log.e(TAG, "channelInactive");
//        NettyTcpServer.getInstance().setConnectStatus(false);
        mListener.onChannelDisConnect(ctx.channel());
    }
}
