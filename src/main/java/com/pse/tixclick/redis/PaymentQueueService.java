package com.pse.tixclick.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pse.tixclick.payload.request.create.CreateOrderRequest;
import com.pse.tixclick.service.OrderService;
import com.pse.tixclick.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PaymentQueueService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    private static final int MAX_CONCURRENT_TRANSACTIONS = 10;
    private final AtomicInteger currentTransactions = new AtomicInteger(0);
    private final String PAYMENT_QUEUE = "payment_queue";

    private final ExecutorService executor = Executors.newFixedThreadPool(10); // Xử lý đa luồng

    @Scheduled(fixedRate = 2000) // Worker chạy mỗi 2 giây
    public void processQueue() {
        while (currentTransactions.get() < MAX_CONCURRENT_TRANSACTIONS) {
            String json = redisTemplate.opsForList().leftPop(PAYMENT_QUEUE);
            if (json == null) break; // Không còn yêu cầu trong hàng chờ

            executor.submit(() -> {
                try {
                    CreateOrderRequest request = new ObjectMapper().readValue(json, CreateOrderRequest.class);
                    processPayment(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void processPayment(CreateOrderRequest request) {
        try {
            currentTransactions.incrementAndGet(); // Đánh dấu đang xử lý

            // Gọi service thanh toán
            orderService.createOrder(request);

            System.out.println("✅ Thanh toán thành công cho order");
        } catch (Exception e) {
            System.out.println("❌ Thanh toán thất bại cho order");
            e.printStackTrace();
        } finally {
            currentTransactions.decrementAndGet(); // Giảm số lượng sau khi hoàn tất
        }
    }
}
