package ifreecomm.nettydemo;

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


public class NettyClient {
    private static final String TAG = "NettyClient";

    private EventLoopGroup group;

    private NettyListener listener;

    private Channel channel;

    private boolean isConnect = false;

    private static int reconnectNum = Integer.MAX_VALUE;

    private boolean isNeedReconnect = true;
    private boolean isConnecting = false;

    private long reconnectIntervalTime = 5000;
    private static final Integer CONNECT_TIMEOUT_MILLIS = 5000;

    public String host;
    public int tcp_port;

//    private ScheduledExecutorService mScheduledExecutorService;

    public NettyClient(String host, int tcp_port) {
        this.host = host;
        this.tcp_port = tcp_port;
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
        synchronized (NettyClient.this) {
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
                                ch.pipeline().addLast(new NettyClientHandler(listener));
                            }
                        });

                try {
                    channelFuture = bootstrap.connect(host, tcp_port).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            if (channelFuture.isSuccess()) {
                                Log.e(TAG,"连接成功");
                                isConnect = true;
                                channel = channelFuture.channel();
                            } else {
                                Log.e(TAG,"连接失败");
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
                    listener.onServiceStatusConnectChanged(NettyListener.STATUS_CONNECT_CLOSED);
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

    public boolean sendMsgToServer(String data, ChannelFutureListener listener) {
        boolean flag = channel != null && isConnect;
        if (flag) {
//			ByteBuf buf = Unpooled.copiedBuffer(data);
//            ByteBuf byteBuf = Unpooled.copiedBuffer(data + System.getProperty("line.separator"), //2
//                    CharsetUtil.UTF_8);
            channel.writeAndFlush(data + System.getProperty("line.separator")).addListener(listener);
        }
        return flag;
    }

    public boolean sendMsgToServer(byte[] data, ChannelFutureListener listener) {
        boolean flag = channel != null && isConnect;
        if (flag) {
            ByteBuf buf = Unpooled.copiedBuffer(data);
            channel.writeAndFlush(buf).addListener(listener);
        }
        return flag;
    }

    public void setReconnectNum(int reconnectNum) {
        this.reconnectNum = reconnectNum;
    }

    public void setReconnectIntervalTime(long reconnectIntervalTime) {
        this.reconnectIntervalTime = reconnectIntervalTime;
    }

    public boolean getConnectStatus() {
        return isConnect;
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public void setConnectStatus(boolean status) {
        this.isConnect = status;
    }

    public void setListener(NettyListener listener) {
        this.listener = listener;
    }

}
