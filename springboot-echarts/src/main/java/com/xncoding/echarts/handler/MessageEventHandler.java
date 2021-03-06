package com.xncoding.echarts.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.xncoding.echarts.api.model.BaseResponse;
import com.xncoding.echarts.common.constants.MsgType;
import com.xncoding.echarts.config.properties.MyProperties;
import com.xncoding.echarts.service.ApiService;
import io.socket.client.Socket;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * 消息事件处理器
 *
 * 核心的接口处理类
 */
@Component
public class MessageEventHandler {
    @Resource
    private MyProperties p;
    private final SocketIOServer server;
    private final ApiService apiService;

    private static final Logger logger = LoggerFactory.getLogger(MessageEventHandler.class);

    @Autowired
    public MessageEventHandler(SocketIOServer server, ApiService apiService) {
        this.server = server;
        this.apiService = apiService;
    }

    /**
     * 添加connect事件，当客户端发起连接时调用
     *
     * @param client 客户端
     */
    @OnConnect
    public void onConnect(SocketIOClient client) {
        if (client != null) {
            //String imei = client.getHandshakeData().getSingleUrlParam("imei");
            //String applicationId = client.getHandshakeData().getSingleUrlParam("appid");
            final String sessionId = client.getSessionId().toString();
            logger.info("连接成功, sessionId=" + sessionId);
            // 赶紧保存这个sessionID呀
            apiService.updateSessionId(sessionId);
        } else {
            logger.error("客户端为空");
        }
    }

    /**
     * 添加@OnDisconnect事件，客户端断开连接时调用，刷新客户端信息
     *
     * @param client 客户端
     */
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        logger.info("客户端断开连接, sessionId=" + client.getSessionId().toString());
        client.disconnect();
    }

    /**
     * 保存客户端传来的图片数据
     *
     * @param client     客户端
     * @param ackRequest 回执消息
     * @param imgData    Base64的图形数据
     */
    @OnEvent(value = "savePic")
    public void onSavePic(SocketIOClient client, AckRequest ackRequest, String imgData) {
        logger.info("保存客户端传来的图片数据 start, sessionId=" + client.getSessionId().toString());
        String r = apiService.saveBase64Pic(imgData);
        logger.info("保存客户端传来的图片 = {}", r);
        ackRequest.sendAckData("图片保存结果=" + r);
    }


    // 消息接收入口
    @OnEvent(value = Socket.EVENT_MESSAGE)
    public void onEvent(SocketIOClient client, AckRequest ackRequest, Object data) {
        logger.info("接收到客户端消息");
        if (ackRequest.isAckRequested()) {
            // send ack response with data to client
            ackRequest.sendAckData("服务器回答Socket.EVENT_MESSAGE", "好的");
        }
    }

    // 广播消息接收入口
    @OnEvent(value = "broadcast")
    public void onBroadcast(SocketIOClient client, AckRequest ackRequest, Object data) {
        logger.info("接收到广播消息");
        // 房间广播消息
        String room = client.getHandshakeData().getSingleUrlParam("appid");
        server.getRoomOperations(room).sendEvent("broadcast", "广播啦啦啦啦");
    }

    /**
     * 报告地址接口
     * @param client 客户端
     * @param ackRequest 回执消息
     * @param param 报告地址参数
     */
    @OnEvent(value = "doReport")
    public void onDoReport(SocketIOClient client, AckRequest ackRequest, String param) {
        logger.info("报告地址接口 start....");
        ackRequest.sendAckData(param);
    }
}

