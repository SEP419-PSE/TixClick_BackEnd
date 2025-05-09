//package com.pse.tixclick.redis;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//@Service
//public class TicketProcessorService {
//    @Autowired
//    StringRedisTemplate redisTemplate;
//    private static final String QUEUE_KEY = "ticket_queue:event:";
//    private static final String TICKET_KEY = "available_tickets:event:";
//
//
//    @Scheduled(fixedRate = 2000) // Worker chạy mỗi 2 giây
//    public void processQueue(int evnetid, int eventActivityId, int ticketId) {
//
//    }
//    private void handleTicketBooking(String eventId, String userId) {
//        String ticketKey = TICKET_KEY + eventId;
//        Long availableTickets = redisTemplate.opsForValue().decrement(ticketKey);
//
//        if (availableTickets != null && availableTickets >= 0) {
//            System.out.println("✅ Vé đã được đặt cho user: " + userId);
//        } else {
//            System.out.println("❌ Hết vé, không thể đặt cho user: " + userId);
//            redisTemplate.opsForValue().increment(ticketKey); // Hoàn lại số vé
//        }
//    }
//}