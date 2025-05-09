//package com.pse.tixclick.redis;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@Service
//public class TicketQueueService {
//    private final RedisTemplate<String, String> redisTemplate;
//    private final ExecutorService executor = Executors.newFixedThreadPool(5); // 5 luồng xử lý song song
//
//    public TicketQueueService(RedisTemplate<String, String> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }
//
////    public void processQueue(int eventId) {
////        String queueKey = "queue_event_" + eventId;
////
////        while (true) {
////            String json = redisTemplate.opsForList().leftPop(queueKey);
////            if (json == null) break;
////
////            executor.submit(() -> {
////                try {
////                    ObjectMapper objectMapper = new ObjectMapper();
////                    Map<String, String> requestData = objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
////
////                    String userId = requestData.get("userId");
////                    String ticketId = requestData.get("ticketId");
////
////                    processPayment(userId, ticketId);
////                } catch (Exception e) {
////                    e.printStackTrace();
////                }
////            });
////        }
////    }
////
////
////    private void processPayment(String userId, String eventId) {
////        // Gọi API thanh toán
////        boolean paymentSuccess = true;
////        if (paymentSuccess) {
////            System.out.println("✅ User " + userId + " thanh toán thành công!");
////        } else {
////            System.out.println("❌ User " + userId + " thanh toán thất bại!");
////        }
////    }
//
//}
