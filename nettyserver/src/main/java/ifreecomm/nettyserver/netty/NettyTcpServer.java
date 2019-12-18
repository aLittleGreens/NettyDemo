package ifreecomm.nettyserver.netty;

import android.text.TextUtils;
import android.util.Log;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;


/**
 * TCP 服务端
 * 目前服务端支持连接多个客户端
 */
public class NettyTcpServer {

    private static final String TAG = "NettyTcpServer";
    private final int port = 1088;
    private Channel channel;

    private static NettyTcpServer instance = null;
    private NettyServerListener listener;
    //    private boolean connectStatus;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private boolean isServerStart;

    private String packetSeparator;
    private int maxPacketLong = 1024;

    public void setPacketSeparator(String separator) {
        this.packetSeparator = separator;
    }

    public void setMaxPacketLong(int maxPacketLong) {
        this.maxPacketLong = maxPacketLong;
    }


    public static NettyTcpServer getInstance() {
        if (instance == null) {
            synchronized (NettyTcpServer.class) {
                if (instance == null) {
                    instance = new NettyTcpServer();
                }
            }
        }
        return instance;
    }

    private NettyTcpServer() {
    }

    public void start() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                bossGroup = new NioEventLoopGroup(1);
                workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class) // 5
                            .localAddress(new InetSocketAddress(port)) // 6
                            .childOption(ChannelOption.SO_KEEPALIVE, true)
                            .childOption(ChannelOption.SO_REUSEADDR, true)
                            .childOption(ChannelOption.TCP_NODELAY, true)
                            .childHandler(new ChannelInitializer<SocketChannel>() { // 7

                                @Override
                                public void initChannel(SocketChannel ch) throws Exception {
                                    ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                                    if (!TextUtils.isEmpty(packetSeparator)) {
//                                        ByteBuf delimiter = Unpooled.copiedBuffer(packetSeparator.getBytes());
                                        ByteBuf delimiter= Unpooled.buffer();
                                        delimiter.writeBytes(packetSeparator.getBytes());
                                        ch.pipeline().addLast(new DelimiterBasedFrameDecoder(maxPacketLong, delimiter));
                                    } else {
                                        ch.pipeline().addLast(new LineBasedFrameDecoder(maxPacketLong));
                                    }
                                    ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                                    ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                                    ch.pipeline().addLast(new EchoServerHandler(listener));
                                }
                            });

                    // Bind and start to accept incoming connections.
                    ChannelFuture f = b.bind().sync(); // 8
                    Log.e(TAG, NettyTcpServer.class.getName() + " started and listen on " + f.channel().localAddress());
                    isServerStart = true;
                    listener.onStartServer();
                    // Wait until the server socket is closed.
                    // In this example, this does not happen, but you can do that to gracefully
                    // shut down your server.
                    f.channel().closeFuture().sync(); // 9
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                } finally {
                    isServerStart = false;
                    listener.onStopServer();
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                }
            }
        }.start();

    }

    public void disconnect() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public void setListener(NettyServerListener listener) {
        this.listener = listener;
    }

//    public void setConnectStatus(boolean connectStatus) {
//        this.connectStatus = connectStatus;
//    }
//
//    public boolean getConnectStatus() {
//        return connectStatus;
//    }

    public boolean isServerStart() {
        return isServerStart;
    }


    // 异步发送消息
    public boolean sendMsgToServer(String data, ChannelFutureListener listener) {
        boolean flag = channel != null && channel.isActive();
        String separator = TextUtils.isEmpty(packetSeparator) ? System.getProperty("line.separator") : packetSeparator;
        if (flag) {
            channel.writeAndFlush(data + separator).addListener(listener);
        }
        return flag;
    }

    // 同步发送消息
    public boolean sendMsgToServer(String data) {
        boolean flag = channel != null && channel.isActive();
        if (flag) {
//			ByteBuf buf = Unpooled.copiedBuffer(data);
//            ByteBuf byteBuf = Unpooled.copiedBuffer(data + System.getProperty("line.separator"), //2
//                    CharsetUtil.UTF_8);
            String separator = TextUtils.isEmpty(packetSeparator) ? System.getProperty("line.separator") : packetSeparator;
            ChannelFuture channelFuture = channel.writeAndFlush(data + separator).awaitUninterruptibly();
            return channelFuture.isSuccess();
        }
        return false;
    }

//    public boolean sendMsgToServer(byte[] data, ChannelFutureListener listener) {
//        boolean flag = channel != null && channel.isActive();
//        if (flag) {
//            ByteBuf buf = Unpooled.copiedBuffer(data);
//            channel.writeAndFlush(buf).addListener(listener);
//        }
//        return flag;
//    }

    /**
     * 切换通道
     * 设置服务端，与哪个客户端通信
     *
     * @param channel
     */
    public void selectorChannel(Channel channel) {
        this.channel = channel;
    }

}
