package com.localconnect.service;

import com.localconnect.domain.enums.NotificationChannel;

public interface NotificationService {
    void sendNotification(String recipientPhone, String messageType, String content);

    void sendUrgentAlert(String recipientPhone, String messageType, String content);

    NotificationChannel getChannel();
}
