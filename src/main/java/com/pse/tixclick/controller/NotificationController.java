package com.pse.tixclick.controller;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.payload.dto.NotificationDTO;
import com.pse.tixclick.payload.response.ApiResponse;
import com.pse.tixclick.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
    NotificationService notificationService;
    SimpMessagingTemplate messagingTemplate;

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotificationsByAccount(){
        try{
            List<NotificationDTO> notifications = notificationService.getNotificationByAccountId();

            return ResponseEntity.ok(ApiResponse.<List<NotificationDTO>>builder()
                    .code(HttpStatus.OK.value())
                    .message("Get notifications successfully")
                    .result(notifications)
                    .build());
        }catch (AppException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.<List<NotificationDTO>>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.<List<NotificationDTO>>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message(e.getMessage())
                    .build());
        }

    }

    @PatchMapping ("/read-notification")
    public ResponseEntity<ApiResponse<String>> readNotification(@RequestParam int id){
        try{
            String message = notificationService.readNotification(id);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .code(HttpStatus.OK.value())
                    .message(message)
                    .build());
        }catch (AppException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.<String>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.<String>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/test")
    public void test() {
        // Gửi thông báo đến người dùng 'huy2' trên kênh '/user/queue/notifications'
        messagingTemplate.convertAndSendToUser("huy2", "/specific/messages", "Hello, this is a test message!");
    }

    @GetMapping("/count-unread-notification")
    public ResponseEntity<ApiResponse<Integer>> countUnreadNotification(){
        try{
            int count = notificationService.countUnreadNotification();
            return ResponseEntity.ok(ApiResponse.<Integer>builder()
                    .code(HttpStatus.OK.value())
                    .message("Count unread notifications successfully")
                    .result(count)
                    .build());
        }catch (AppException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.<Integer>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .build());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.<Integer>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message(e.getMessage())
                    .build());
        }
    }

}
