package com.zengjinhao.jwt.controller;


import com.zengjinhao.jwt.model.RequestMessage;
import com.zengjinhao.jwt.model.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * WsController
 *
 */
@Controller
public class WsController {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WsController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * @MessageMapping注解和我们之前使用的@RequestMapping类似。
     * @SendTo注解表示当服务器有消息需要推送的时候， 会对订阅了@SendTo中路径的浏览器发送消息。
     */



    @MessageMapping("/welcome")
    @SendTo("/topic/say")
    public ResponseMessage say(RequestMessage message) {
        System.out.println(message.getName());
        return new ResponseMessage("welcome," + message.getName() + " !");
    }

    /**
     * 定时推送消息
     * 定义了一个定时推送消息方法，这个方法每隔1秒会主动给订阅了主题/topic/callback的客户端推送消息。
     */
    @Scheduled(fixedRate = 1000)
    public void callback() {
        // 发现消息
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        messagingTemplate.convertAndSend("/topic/callback", "定时推送消息时间: " + df.format(new Date()));
    }
}
