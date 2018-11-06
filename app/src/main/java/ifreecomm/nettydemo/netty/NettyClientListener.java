package ifreecomm.nettydemo.netty;



public interface NettyClientListener<T> {

    public final static byte STATUS_CONNECT_SUCCESS = 1;

    public final static byte STATUS_CONNECT_CLOSED = 0;

    public final static byte STATUS_CONNECT_ERROR = 0;


    /**
     * 当接收到系统消息
     */
    void onMessageResponseClient(T msg,int index);

    /**
     * 当服务状态发生变化时触发
     */
    public void onClientStatusConnectChanged(int statusCode,int index);
}
