# NettyDemo
 该分支，支持发送和接收byte[]格式。但是没有做黏包处理，如果有黏包，可以根据自己定义的包头信息，自行解决。
 ## 如何导入
 
直接依赖 lib：nettyclientlib
 ## 一、先看演示效果，后面有详细的用法教程
 <img src="https://github.com/cai784921129/NettyDemo/blob/master/screenshot/clent.gif" width="280px"/> <img src="https://github.com/cai784921129/NettyDemo/blob/master/screenshot/server.gif" height="280px"/>

如果是作为TCP客户端使用的话，可以直接依赖

## 二、HOW TO USE?

1. **创建TCP客户端**
```Java
      NettyTcpClient  mNettyTcpClient = new NettyTcpClient.Builder()
                .setHost(Const.HOST)    //设置服务端地址
                .setTcpPort(Const.TCP_PORT) //设置服务端端口号
                .setMaxReconnectTimes(5)    //设置最大重连次数
                .setReconnectIntervalTime(5)    //设置重连间隔时间。单位：秒
                .setSendheartBeat(true) //设置是否发送心跳
                .setHeartBeatInterval(5)    //设置心跳间隔时间。单位：秒
                .setHeartBeatData(new byte[]{0x55, 0x55, 0x55, 0x55}) //设置心跳数据，可以是String类型，也可以是byte[]，以后设置的为准
                .setIndex(0)    //设置客户端标识.(因为可能存在多个tcp连接)
                .build();
```

2. **设置监听**
```Java
        mNettyTcpClient.setListener(new NettyClientListener<String>() {
            @Override
            public void onMessageResponseClient(String msg, int index) {
                //服务端过来的消息回调
            }

            @Override
            public void onClientStatusConnectChanged(int statusCode, int index) {
               //连接状态回调
            }
        });
```
3. **连接、断开连接**
- 判断是否已经连接
```Java
mNettyTcpClient.getConnectStatus()
```
- 连接
```Java
mNettyTcpClient.connect();
```
- 断开连接
```Java
mNettyTcpClient.disconnect();
```
4. **发送信息到服务端**
```Java
                    mNettyTcpClient.sendMsgToServer(msg, new MessageStateListener() {
                        @Override
                        public void isSendSuccss(boolean isSuccess) {
                            if (isSuccess) {
                                Log.d(TAG, "send successful");
                            } else {
                                Log.d(TAG, "send error");
                            }
                        }
                    });
```

## 二、小伙伴遇到的问题
1、服务端反馈的消息被截断
答：由于socket会粘包，sdk中默认的采用的是特殊符号作为分割符，来解决粘包问题，默认采用的分隔符是分割符，也可以通过setPacketSeparator，设置自定义的换行符，这样客户端发送信息的时候，sdk会在末尾，添加分隔符，这里需要注意服务端，返回信息的时候，也要添加对应的分隔符。


