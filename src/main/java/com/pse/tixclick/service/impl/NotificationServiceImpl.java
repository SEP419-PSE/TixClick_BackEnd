package com.pse.tixclick.service.impl;

import com.pse.tixclick.payload.dto.NotificationDTO;
import com.pse.tixclick.payload.entity.Notification;
import com.pse.tixclick.repository.NotificationRepository;
import com.pse.tixclick.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class NotificationServiceImpl implements NotificationService {
    NotificationRepository notificationRepository;
    ModelMapper modelMapper;
    @Override
    public List<NotificationDTO> getNotificationByAccountId(String username) {
        List<Notification> notifications = notificationRepository.findNotificationsByAccount_UserName(username);

        return notifications.stream()
                .map(notification -> modelMapper.map(notification, NotificationDTO.class))
                .collect(Collectors.toList());
    }

}
