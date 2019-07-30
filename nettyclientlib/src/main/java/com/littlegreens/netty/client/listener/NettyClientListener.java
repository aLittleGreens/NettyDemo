package com.littlegreens.netty.client.listener;


/**
 * Created by littleGreens on 2018-11-10.
 * TCP状态变化监听
 */
public interface NettyClientListener<T> {

    /**
     * 当接收到系统消息
     * @param msg 消息
     * @param index tcp 客户端的标识，因为一个应用程序可能有很多个长链接
     */
    void onMessageResponseClient(T msg, int index);

    /**
     * 当服务状态发生变化时触发
     * @param statusCode 状态变化
     * @param index tcp 客户端的标识，因为一个应用程序可能有很多个长链接
     */
    public void onClientStatusConnectChanged(int statusCode, int index);
}
