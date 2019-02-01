package com.xcloude.pushservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PushController {
  @Autowired
  private PushWebsocket pushWebsocket;

  @RequestMapping("/push")
  @ResponseBody
  public String push(String message) {
    pushWebsocket.onMessage(message);
    return "OK";
  }

  @RequestMapping("/index")
  public String index() {
    return "login";
  }
}
