package com.pse.tixclick.service.impl;

import com.pse.tixclick.exception.AppException;
import com.pse.tixclick.exception.ErrorCode;
import com.pse.tixclick.payload.dto.NotificationDTO;
import com.pse.tixclick.payload.entity.Notification;
import com.pse.tixclick.repository.AccountRepository;
import com.pse.tixclick.repository.NotificationRepository;
import com.pse.tixclick.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class NotificationServiceImpl implements NotificationService {
    NotificationRepository notificationRepository;
    ModelMapper modelMapper;
    AccountRepository accountRepository;
    @Override
    public List<NotificationDTO> getNotificationByAccountId() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        var account = accountRepository.findAccountByUserName(username)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<Notification> notifications = notificationRepository.findNotificationsByAccount_UserName(username);

        return notifications.stream()
                .map(notification -> modelMapper.map(notification, NotificationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public String readNotification(int id) {
        Notification notification = notificationRepository.findById(id).orElseThrow();
        notification.setRead(true);
        notification.setReadDate(LocalDateTime.now());
        notificationRepository.save(notification);
        return "Notification read successfully";
    }

}
