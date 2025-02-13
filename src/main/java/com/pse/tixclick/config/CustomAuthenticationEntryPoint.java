package com.pse.tixclick.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pse.tixclick.payload.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper(); // Dùng để convert object thành JSON

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        // Tạo ApiResponse với lỗi 401 Unauthorized
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .code(HttpServletResponse.SC_UNAUTHORIZED)
                .message("Token hết hạn hoặc không hợp lệ")
                .build();

        // Cấu hình phản hồi
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Gửi phản hồi dưới dạng JSON
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.getWriter().flush();
    }
}
