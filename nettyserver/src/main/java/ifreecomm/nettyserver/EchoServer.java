package ifreecomm.nettyserver;

import android.util.Log;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;


public class EchoServer {

    private static final String TAG = "EchoServer";
    private final int port = 1088;
    private Channel channel;

    private static EchoServer instance = null;
    private NettyListener listener;
    private boolean connectStatus;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private boolean isServerStart;

    public static EchoServer getInstance() {
        if (instance == null) {
            synchronized (EchoServer.class) {
                if (instance == null) {
                    instance = new EchoServer();
                }
            }
        }
        return instance;
    }

    private EchoServer() {
    }

	/*public static void main(String[] args) throws Exception {
        int port = 1000;// Integer.parseInt(args[0]); //1

		new Thread() {
			public void run() {
				while (true) {
					InputStreamReader is = new InputStreamReader(System.in); // new����InputStreamReader����
					BufferedReader br = new BufferedReader(is); // �ù���ķ�������BufferedReader��
					try { // �÷������и�IOExcepiton��Ҫ����
						String name = br.readLine();
						if (channel != null && channel.isActive()) {
//							ByteBuf byteBuf = Unpooled.copiedBuffer(name+System.getProperty("line.separator"), // 2
//									CharsetUtil.UTF_8);
							channel.writeAndFlush(name+System.getProperty("line.separator")).addListener(new ChannelFutureListener() {
								@Override
								public void operationComplete(ChannelFuture channelFuture) throws Exception {
									if (channelFuture.isSuccess()) { // 4
										System.out.println("Write auth successful");
									} else {
										System.out.println("Write auth error");
									}
								}
							});
						}else {
							System.out.println("channel:"+channel);
						}

					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			};
		}.start();
		new EchoServer(port).start();
		// 2

	}*/

    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 5
                    .localAddress(new InetSocketAddress(port)) // 6
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_REUSEADDR,true)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 7

                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("initChannel ch:" + ch);
                            ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
                            ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                            ch.pipeline().addLast(new EchoServerHandler(listener));
                        }
                    });

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind().sync(); // 8

            System.out.println(EchoServer.class.getName() + " started and listen on " + f.channel().localAddress());
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

    public void disconnect() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public void setListener(NettyListener listener) {
        this.listener = listener;
    }

    public void setConnectStatus(boolean connectStatus) {
        this.connectStatus = connectStatus;
    }

    public boolean getConnectStatus() {
        return connectStatus;
    }

    public boolean isServerStart(){
        return isServerStart;
    }


    public boolean sendMsgToServer(String data, ChannelFutureListener listener) {
        boolean flag = channel != null && connectStatus && channel.isActive();
        if (flag) {
//			ByteBuf buf = Unpooled.copiedBuffer(data);
//            ByteBuf byteBuf = Unpooled.copiedBuffer(data + System.getProperty("line.separator"), //2
//                    CharsetUtil.UTF_8);
            channel.writeAndFlush(data + System.getProperty("line.separator")).addListener(listener);
        }
        return flag;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }


//	@Override
//	public void onMessageResponse(Channel channel,Object msg) {
//		// TODO Auto-generated method stub
//		this.channel = channel;
////		ByteBuf in = (ByteBuf) msg;
//        System.out.println("Server received: " + msg);        //2
//	}
//
//	@Override
//	public void onServiceStatusConnectChanged(int statusCode) {
//		// TODO Auto-generated method stub
//
//	}
}
