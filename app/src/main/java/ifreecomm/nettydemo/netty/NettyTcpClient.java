package ifreecomm.nettydemo.netty;

import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
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
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;


/**
 * TCP 客户端
 */
public class NettyTcpClient {
    private static final String TAG = "NettyTcpClient";

    private EventLoopGroup group;

    private NettyClientListener listener;

    private Channel channel;

    private boolean isConnect = false;

    private static int reconnectNum = Integer.MAX_VALUE;

    private boolean isNeedReconnect = true;
    private boolean isConnecting = false;

    private long reconnectIntervalTime = 5000;
    private static final Integer CONNECT_TIMEOUT_MILLIS = 5000;

    private String host;
    private int tcp_port;
    private int mIndex;


    public NettyTcpClient(String host, int tcp_port, int index) {
        this.host = host;
        this.tcp_port = tcp_port;
        this.mIndex = index;
    }

    public void connect() {
        if (isConnecting) {
            return;
        }
        Thread clientThread = new Thread("client-Netty") {
            @Override
            public void run() {
                super.run();
                isNeedReconnect = true;
                reconnectNum = Integer.MAX_VALUE;
                connectServer();
            }
        };
        clientThread.start();
    }


    private void connectServer() {
        synchronized (NettyTcpClient.this) {
            ChannelFuture channelFuture = null;
            if (!isConnect) {
                isConnecting = true;
                group = new NioEventLoopGroup();
                Bootstrap bootstrap = new Bootstrap().group(group)
                        .option(ChannelOption.TCP_NODELAY, true)//屏蔽Nagle算法试图
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() { // 5
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast("ping", new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));//5s未发送数据，回调userEventTriggered
                                ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                                ch.pipeline().addLast(new LineBasedFrameDecoder(1024));//黏包处理
                                ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                                ch.pipeline().addLast(new NettyClientHandler(listener, mIndex));
                            }
                        });

                try {
                    channelFuture = bootstrap.connect(host, tcp_port).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            if (channelFuture.isSuccess()) {
                                Log.e(TAG, "连接成功");
                                isConnect = true;
                                channel = channelFuture.channel();
                            } else {
                                Log.e(TAG, "连接失败");
                                isConnect = false;
                            }
                            isConnecting = false;
                        }
                    }).sync();

                    // Wait until the connection is closed.
                    channelFuture.channel().closeFuture().sync();
                    Log.e(TAG, " 断开连接");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isConnect = false;
                    listener.onClientStatusConnectChanged(NettyClientListener.STATUS_CONNECT_CLOSED, mIndex);
                    if (null != channelFuture) {
                        if (channelFuture.channel() != null && channelFuture.channel().isOpen()) {
                            channelFuture.channel().close();
                        }
                    }
                    group.shutdownGracefully();
                    reconnect();
                }
            }
        }
    }


    public void disconnect() {
        Log.e(TAG, "disconnect");
        isNeedReconnect = false;
        group.shutdownGracefully();
    }

    public void reconnect() {
        Log.e(TAG, "reconnect");
        if (isNeedReconnect && reconnectNum > 0 && !isConnect) {
            reconnectNum--;
            SystemClock.sleep(reconnectIntervalTime);
            if (isNeedReconnect && reconnectNum > 0 && !isConnect) {
                Log.e(TAG, "重新连接");
                connectServer();
            }
        }
    }

    /**
     * 异步发送
     *
     * @param data
     * @param listener
     * @return
     */
    public boolean sendMsgToServer(String data, ChannelFutureListener listener) {
        boolean flag = channel != null && isConnect;
        if (flag) {
            ChannelFuture channelFuture = channel.writeAndFlush(data + System.getProperty("line.separator")).addListener(listener);
            if (channelFuture.isSuccess()) {                //4
                Log.d(TAG, "@Write auth successful");
            } else {
                Log.d(TAG, "@Write auth error");
            }

        }
        return flag;
    }

    /**
     * 同步发送
     * @param data
     * @return
     */
    public boolean sendMsgToServer(String data) {
        boolean flag = channel != null && isConnect;
        if (flag) {
            ChannelFuture channelFuture = channel.writeAndFlush(data + System.getProperty("line.separator")).awaitUninterruptibly();
            return channelFuture.isSuccess();
        }
        return false;
    }


    public boolean sendMsgToServer(byte[] data, ChannelFutureListener listener) {
        boolean flag = channel != null && isConnect;
        if (flag) {
            ByteBuf buf = Unpooled.copiedBuffer(data);
            channel.writeAndFlush(buf).addListener(listener);
        }
        return flag;
    }

    /**
     * 设置重连次数
     * @param reconnectNum
     */
    public void setReconnectNum(int reconnectNum) {
        this.reconnectNum = reconnectNum;
    }

    /**
     * 设置重连间隔
     * @param reconnectIntervalTime
     */
    public void setReconnectIntervalTime(long reconnectIntervalTime) {
        this.reconnectIntervalTime = reconnectIntervalTime;
    }

    /**
     * 获取TCP连接状态
     * @return
     */
    public boolean getConnectStatus() {
        return isConnect;
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public void setConnectStatus(boolean status) {
        this.isConnect = status;
    }

    public void setListener(NettyClientListener listener) {
        this.listener = listener;
    }

}
