package com.pse.tixclick.config.testnotification;

import com.nimbusds.jwt.SignedJWT;
import com.pse.tixclick.jwt.Jwt;
import com.pse.tixclick.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.text.ParseException;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketAuthenticationInterceptor implements HandshakeInterceptor {
    @Autowired
    private Jwt jwt;

    @Autowired
    private AccountRepository accountRepository;

    public WebSocketAuthenticationInterceptor() {
        System.out.println("✅ WebSocketAuthenticationInterceptor bean has been created!");
    }
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Lấy URI của request
        String uri = request.getURI().toString();
        System.out.println("🔍 WebSocket Request URI: " + uri);

        // Lấy phần query (sau dấu "?")
        String query = request.getURI().getQuery(); // Ví dụ: "token=eyJhbGciOiJIUzUxMiJ9..."

        // Kiểm tra query có chứa token không
        String token = null;
        if (query != null && query.startsWith("token=")) {
            token = query.substring(6); // Cắt bỏ "token="
        }

        // Debug token sdgsadvdsv
        System.out.println("🔑 Token received in WebSocket: " + token);

        if (token == null || token.isEmpty()) {
            System.out.println("⚠️ Không có token trong WebSocket request!");
//            return false; // Chặn kết nối WebSocket nếu không có token
            return true;
        }

        try {
            if (token == null) {
                System.out.println("❌ Token không hợp lệ!");
                return true;
            }
            // Giải mã token trực tiếp
            SignedJWT signedJWT = SignedJWT.parse(token);
            String userName = signedJWT.getJWTClaimsSet().getSubject();
            attributes.put("principal", new UserPrincipal(userName)); // 👈 Cần thêm dòng này!

            attributes.put("username", userName);

            System.out.println("✅ Xác thực thành công! Username: " + userName);
            return true;
        } catch (ParseException e) {
            System.out.println("❌ Token không hợp lệ!");
            return true;
        }

    }



    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
