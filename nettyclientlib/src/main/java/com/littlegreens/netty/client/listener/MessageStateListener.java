package com.littlegreens.netty.client.listener;

import android.util.Log;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * @author Created by LittleGreens on 2019/7/30
 */
public interface MessageStateListener {
     void isSendSuccss(boolean isSuccess);
}
