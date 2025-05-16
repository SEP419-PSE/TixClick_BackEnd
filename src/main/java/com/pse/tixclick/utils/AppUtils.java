package com.pse.tixclick.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.TicketQrCodeDTO;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.payload.entity.entity_enum.ERole;
import com.pse.tixclick.payload.entity.seatmap.SeatActivity;
import com.pse.tixclick.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.core.GrantedAuthority;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.nio.file.AccessDeniedException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import static javax.crypto.Cipher.SECRET_KEY;

@RequiredArgsConstructor
@Component
public class AppUtils {
    @Autowired
    private final AccountRepository accountRepository;

    @Autowired
    private final TicketPurchaseRepository ticketPurchaseRepository;

    @Autowired
    private final SeatRepository seatRepository;

    @Autowired
    private final ZoneRepository zoneRepository;

    private static final String AES_ALGORITHM = "AES";
    private static final String SECRET_KEY = "0123456789abcdef";


    public Account getAccountFromAuthentication(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return accountRepository.findAccountByUserName(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException(new Exception("UserName not found!!!")));
    }


    public static String encryptTicketQrCode(TicketQrCodeDTO dto) throws Exception {
        String json = new ObjectMapper().writeValueAsString(dto);     // Chuyển object thành JSON
        byte[] compressed = compress(json);                           // Nén JSON
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(compressed);                // Mã hóa AES
        return Base64.getUrlEncoder().encodeToString(encrypted);      // Base64 để tạo QR
    }
    public static byte[] compress(String data) throws Exception {
        Deflater deflater = new Deflater();
        deflater.setInput(data.getBytes("UTF-8"));
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }

        outputStream.close();
        return outputStream.toByteArray();
    }


//    public static String generateQRCode(String data, int width, int height){
//        StringBuilder result = new StringBuilder();
//
//        if(!data.isEmpty()){
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//            try {
//                QRCodeWriter qrCodeWriter = new QRCodeWriter();
//                BitMatrix bitMatrix = qrCodeWriter.encode(data, com.google.zxing.BarcodeFormat.QR_CODE, width, height);
//                BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
//                ImageIO.write(bufferedImage, "png", out);
//
//                result.append(new String(Base64.getEncoder().encode(out.toByteArray())));
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//
//        }
//        return result.toString();
//    }

    public static String encrypt(String data) throws Exception {
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static TicketQrCodeDTO decryptQrCode(String encryptedData) {
        try {
            byte[] decodedBytes = Base64.getMimeDecoder().decode(encryptedData.trim());
            SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(new String(decryptedBytes), TicketQrCodeDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid QR Code format");  // Tạo exception riêng
        }
    }

    public boolean isValidString(String value) {
        return value != null && !value.trim().isEmpty() &&
                !value.trim().equalsIgnoreCase("0") && !value.trim().equalsIgnoreCase("null");
    }

    public boolean isWeekend(LocalDate date) {
        if (date == null) return false;
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    public static LocalDate getStartOfWeekend(LocalDate referenceDate) {
        return referenceDate.with(java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    }

    public static LocalDate getEndOfWeekend(LocalDate referenceDate) {
        return referenceDate.with(java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    public static String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null); // hoặc throw nếu muốn
    }

    public static void checkRole(ERole... rolesAllowed)  {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Extract roleName from JWT token
        String roleName;
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            roleName = (String) jwtAuth.getTokenAttributes().get("roleName"); // Extract roleName claim
        } else {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (roleName == null) {
            throw new AppException(ErrorCode.NOT_FOUNT_ROLE);
        }

        // Convert allowed roles to a list of role names
        List<String> allowedRoleNames = Arrays.stream(rolesAllowed)
                .map(Enum::name)
                .toList();

        // Check if the roleName from the JWT matches any of the allowed roles
        boolean hasRole = allowedRoleNames.contains(roleName);

        if (!hasRole) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }
    public static String rowIndexToLetter(int rowIndex) {
        return String.valueOf((char) ('A' + rowIndex));
    }

    public static String parseSeatDisplayName(SeatActivity seatActivity) {
        if (seatActivity == null || seatActivity.getSeat() == null) return "";

        String rawSeatName = seatActivity.getSeat().getSeatName();  // ví dụ: 1747214534816-r0-c0
        String[] parts = rawSeatName.split("-");
        String rowStr = null, colStr = null;

        for (String part : parts) {
            if (part.startsWith("r")) rowStr = part.substring(1);  // "0"
            if (part.startsWith("c")) colStr = part.substring(1);  // "0"
        }

        int rowIndex = rowStr != null ? Integer.parseInt(rowStr) : -1;
        int colIndex = colStr != null ? Integer.parseInt(colStr) : -1;

        String rowLetter = rowIndex >= 0 ? rowIndexToLetter(rowIndex) : "?";
        String colNumber = colIndex >= 0 ? String.valueOf(colIndex + 1) : "?";

        // Lấy tên zone (nếu có)
        String zoneName = "";
        if (seatActivity.getZoneActivity() != null && seatActivity.getZoneActivity().getZone() != null) {
            zoneName = String.valueOf(seatActivity.getZoneActivity().getZone().getZoneId());  // từ ZoneActivity
        }

        // Rút gọn mô tả
        return "G" + colNumber + "H" + rowLetter + (zoneName.isEmpty() ? "" : " " + zoneName);
    }

    public static String convertSeatName(String rawSeatName) {
        // Ví dụ: "1747214534816-r0-c0"
        if (rawSeatName == null || !rawSeatName.contains("-r") || !rawSeatName.contains("-c")) {
            return rawSeatName;
        }

        try {
            String[] parts = rawSeatName.split("-");

            String rowPart = Arrays.stream(parts)
                    .filter(p -> p.startsWith("r"))
                    .findFirst()
                    .orElse("r0");

            String colPart = Arrays.stream(parts)
                    .filter(p -> p.startsWith("c"))
                    .findFirst()
                    .orElse("c0");

            int rowIndex = Integer.parseInt(rowPart.substring(1));
            int colIndex = Integer.parseInt(colPart.substring(1));

            // Hàng = A, B, C,... (0 → A)
            char rowChar = (char) ('A' + rowIndex);
            int seatNumber = colIndex + 1;

            return "Ghế " + seatNumber + " Hàng " + rowChar;
        } catch (Exception e) {
            return rawSeatName; // fallback nếu lỗi format
        }
    }



}
