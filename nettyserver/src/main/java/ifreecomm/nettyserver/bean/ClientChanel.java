package ifreecomm.nettyserver.bean;

import io.netty.channel.Channel;

/**
 * Created by  IT小蔡 on 2018-11-10.
 * 客户端信息
 */

public class ClientChanel {

    private String clientIp;    //客户端ip

    private Channel channel;    //与客户端建立的通道

    private String shortId;     //通道的唯一标示


    public ClientChanel(String clientIp, Channel channel, String shortId) {
        this.clientIp = clientIp;
        this.channel = channel;
        this.shortId = shortId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getShortId() {
        return shortId;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }
}
