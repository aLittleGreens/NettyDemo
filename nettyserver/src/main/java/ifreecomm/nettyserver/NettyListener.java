package ifreecomm.nettyserver;


import io.netty.channel.Channel;


public interface NettyListener {

    public final static byte STATUS_CONNECT_SUCCESS = 1;

    public final static byte STATUS_CONNECT_CLOSED = 0;

    public final static byte STATUS_CONNECT_ERROR = 0;


    void onMessageResponse(Object msg);

    /**
     * 与客户端建立连接
     * @param channel
     */
    void onChannel(Channel channel);

    void onStartServer();
    void onStopServer();


    public void onServiceStatusConnectChanged(int statusCode);
}
