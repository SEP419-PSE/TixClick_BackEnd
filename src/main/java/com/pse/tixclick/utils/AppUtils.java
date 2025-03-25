package com.pse.tixclick.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.pse.tixclick.payload.entity.Account;
import com.pse.tixclick.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.ScheduledFuture;

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

    private final CompanyAccountRepository companyAccountRepository;



    public Account getAccountFromAuthentication(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return accountRepository.findAccountByUserName(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException(new Exception("UserName not found!!!")));
    }


    public static String transferToString(Object object){
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);


        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return "";
    }


    public static String generateQRCode(String data, int width, int height){
        StringBuilder result = new StringBuilder();

        if(!data.isEmpty()){
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try {
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrCodeWriter.encode(data, com.google.zxing.BarcodeFormat.QR_CODE, width, height);
                BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
                ImageIO.write(bufferedImage, "png", out);

                result.append(new String(Base64.getEncoder().encode(out.toByteArray())));
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return result.toString();
    }

    public String generateUniqueUsername(String baseUsername) {
        baseUsername = baseUsername.replaceAll("\\s+", "").toLowerCase(); // Loại bỏ khoảng trắng và chuẩn hóa
        String newUsername = baseUsername;
        int suffix = 1;

        // Kiểm tra username đã tồn tại chưa
        while (companyAccountRepository.findCompanyAccountByUsername(newUsername).isPresent()) {
            newUsername = baseUsername + suffix;
            suffix++;
        }
        return newUsername;
    }


}
