package com.pse.tixclick.service;

import com.pse.tixclick.payload.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> getNotificationByAccountId(String username);
}
