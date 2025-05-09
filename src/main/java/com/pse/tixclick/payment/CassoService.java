package com.pse.tixclick.payment;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CassoService {
    private static final String BASE_URL = "https://oauth.casso.vn/v2/transactions";
    // Nếu dùng API key, chỉ cần cung cấp API key của bạn
    private static final String API_KEY = "AK_CS.25261c502cb611f095d88b1a1023c43c.JJtEKJiQkkOIanhxhykYn2vs516guMiUxRlBkZtRroXqZo51SpqTlixTVyGnt25bDtccTEU4";

    private final OkHttpClient client = new OkHttpClient();

    public String getTransactions(String fromDate, int page, int pageSize, String sort) {
        // Xây dựng URL với tham số truy vấn
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL).newBuilder();
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            urlBuilder.addQueryParameter("fromDate", fromDate);
        }
        urlBuilder.addQueryParameter("page", String.valueOf(page));
        urlBuilder.addQueryParameter("pageSize", String.valueOf(pageSize));
        urlBuilder.addQueryParameter("sort", sort);

        String url = urlBuilder.build().toString();

        // Tạo Request, dùng "Apikey" thay cho "Bearer"
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Apikey " + API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "{\"error\": \"Failed to fetch transactions\", \"status\": " + response.code() + "}";
            }
            return response.body() != null ? response.body().string() : "{}";
        } catch (IOException e) {
            return "{\"error\": \"Exception occurred\", \"message\": \"" + e.getMessage() + "\"}";
        }
    }
}
