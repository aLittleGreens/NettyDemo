package ifreecomm.nettyserver.netty;

import android.util.Log;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;


@ChannelHandler.Sharable
public class EchoByteServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final String TAG = "EchoServerHandler";
    private NettyServerListener mListener;

    public EchoByteServerHandler(NettyServerListener listener) {
        this.mListener = listener;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//    	System.out.println("channelReadComplete");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();                //5
        ctx.close();
        //6
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ByteBuf buf = (ByteBuf)msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            String result = new String(bytes, "utf-8");
            Log.e(TAG, "server: " + result);
            mListener.onMessageResponseServer(bytes,ctx.channel().id().asShortText());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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
        mListener.onChannelConnect(ctx.channel());
//        NettyTcpServer.getInstance().setConnectStatus(true);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.e(TAG, "channelInactive");
//        NettyTcpServer.getInstance().setConnectStatus(false);
        mListener.onChannelDisConnect(ctx.channel());
    }
}
