package com.pse.tixclick.config.testnotification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class MessageController {

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    private final List<Message> messageHistory = new ArrayList<>(); // Lưu lịch sử tin nhắn


    // Mapped as /app/application
    @SendTo("/all/messages")
    @MessageMapping("/application")
    public Message send(final Message message) throws Exception {
        log.info("Message sent to all subscribers: to={}, text={}", message.getTo(), message.getText());
        messageHistory.add(message); // Lưu tin nhắn vào danh sách
        getAllMessages();
    return message;
    }

    // Mapped as /app/private
    @MessageMapping("/private")
    public void sendToSpecificUser(@Payload Message message, Principal principal) {



        simpMessagingTemplate.convertAndSendToUser(
                message.getTo(), // Username của người nhận
                "/specific/messages",
                message.getText() // Gửi cả sender và nội dung
        );
    }

    @GetMapping("/messages")
    public List<Message> getAllMessages() {
        log.info("📜 Lịch sử tin nhắn:");
        for (Message msg : messageHistory) {
            log.info("➡ To: {}, Text: {}", msg.getTo(), msg.getText());
        }
        return messageHistory;
    }

}