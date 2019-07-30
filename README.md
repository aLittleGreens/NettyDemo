# NettyDemo

 > Netty是基于Java NIO client-server的网络应用框架，使用Netty可以快速开发网络应用
 
   >更多用法请请跳转到[https://github.com/netty/netty](https://github.com/netty/netty)  
 
 **本项目基于Netty在Android平台所建项目，只是提供Netty的使用方式，大家可根据自己的需求，做相应的定制。**
 
 **演示时，客户端在Const.java中请修改TCP服务端ip地址就行了，服务端通过切换通道，可以与多个客户端通信。**
 
 最后，不足之处请海涵，多多提issue，大家一起解决。
 ## 添加依赖
 ```
 1. build.gradle 根目录
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://dl.bintray.com/littlegreens/maven' }
    }
}
2.Module的build.gradle加入依赖:
 dependencies {
  implementation 'com.littlegreens.netty.client:nettyclientlib:1.0.0'
 }


 
 ```
 ## 一、先看演示效果，后面有详细的用法教程

![image](https://github.com/cai784921129/NettyDemo/blob/master/screenshot/clent.gif?raw=true)
![image](https://github.com/cai784921129/NettyDemo/blob/master/screenshot/server.gif?raw=true)

如果是作为TCP客户端使用的话，可以直接依赖

## 二、HOW TO USE?

1. **创建TCP客户端**
```Java
      NettyTcpClient  mNettyTcpClient = new NettyTcpClient.Builder()
                .setHost("192.168.66.34")    //设置服务端地址
                .setTcpPort(8881) //设置服务端端口号
                .setMaxReconnectTimes(5)    //设置最大重连次数
                .setReconnectIntervalTime(5)    //设置重连间隔时间。单位：秒
                .setSendheartBeat(true) //设置发送心跳
                .setHeartBeatInterval(5)    //设置心跳间隔时间。单位：秒
                .setHeartBeatData(new byte[]{0x03, 0x0F, (byte) 0xFE, 0x05, 0x04, 0x0a}) //设置心跳数据，可以是String类型，也可以是byte[]
          //    .setHeartBeatData("I'm is HeartBeatData") //设置心跳数据，可以是String类型，也可以是byte[]，以后设置的为准
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
3. **建立连接**
```Java
mNettyTcpClient.connect();//连接服务器
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


