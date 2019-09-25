package com.xcloude.pushservice.controller;

import com.alibaba.fastjson.JSON;
import com.xcloude.pushservice.entity.PushRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint(value = "/login")
@Component
public class PushWebsocket {
  private static final Logger logger = LoggerFactory.getLogger(PushWebsocket.class);
  private static AtomicInteger onlineCount = new AtomicInteger(0);

  private static ConcurrentHashMap<String, PushWebsocket>
      webSocketSet = new ConcurrentHashMap<>();

  // 客户端会话
  private Session session;

  @OnOpen
  public void onOpen(Session session) {
    this.session = session;
    logger.info("客户端加入，当前会话数" + onlineCount.incrementAndGet());
  }

  @OnClose
  public void onClose() {
    for (Map.Entry entry : webSocketSet.entrySet()) {
      if (entry.getValue().equals(this)) {
        webSocketSet.remove(entry.getKey());
      }
    }
    logger.info("客户端退出，当前会话数" + onlineCount.decrementAndGet());
  }


  @OnMessage
  public void onMessage(String message) {
    logger.info("接收 " + message);
    PushRequest request = JSON.parseObject(message, PushRequest.class);
    if ("login".equals(request.getType())) {
      webSocketSet.put(request.getName(), this);
      sendMessage("登录成功");
    }

    if ("push".equals(request.getType())) {
      if (StringUtils.isEmpty(request.getTo())) {
        for (PushWebsocket item : webSocketSet.values()) {
          item.sendMessage(request.getData());
        }
      } else {
        webSocketSet.get(request.getTo()).sendMessage(request.getName() + "发送给你：" + request.getData());
      }
    }
  }

  @OnError
  public void onError(Throwable e) {
    logger.error(e.getMessage());
  }

  private void sendMessage(String message) {
    try {
      this.session.getBasicRemote().sendText(message);
    } catch (IOException e) {
      logger.error("IO异常 : {}", e.getMessage());
    }
  }
}
