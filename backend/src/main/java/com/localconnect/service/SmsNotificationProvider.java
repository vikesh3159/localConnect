package com.localconnect.service;

import com.localconnect.domain.NotificationLog;
import com.localconnect.domain.enums.DeliveryStatus;
import com.localconnect.domain.enums.NotificationChannel;
import com.localconnect.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Primary
public class SmsNotificationProvider implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationProvider.class);
    private final NotificationLogRepository notificationLogRepository;

    public SmsNotificationProvider(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @Override
    public void sendNotification(String recipientPhone, String messageType, String content) {
        deliver(recipientPhone, messageType, content, false);
    }

    @Override
    public void sendUrgentAlert(String recipientPhone, String messageType, String content) {
        deliver(recipientPhone, messageType, "[URGENT 15-MIN] " + content, true);
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    private void deliver(String recipientPhone, String messageType, String content, boolean urgent) {
        NotificationLog notificationLog = new NotificationLog(
                recipientPhone,
                NotificationChannel.SMS,
                messageType,
                content);

        try {
            // Simulated SMS Gateway Dispatch (e.g. DLT Gateway in India)
            log.info("[SMS GATEWAY DISPATCH] To: {} | Type: {} | Urgent: {} | Message: {}",
                    recipientPhone, messageType, urgent, content);

            // In production, HTTP call to SMS gateway occurs here.
            notificationLog.setDeliveryStatus(DeliveryStatus.SENT);
            notificationLog.setDeliveredAt(LocalDateTime.now());
        } catch (Exception ex) {
            log.error("Failed to send SMS to {}: {}", recipientPhone, ex.getMessage());
            notificationLog.setDeliveryStatus(DeliveryStatus.FAILED);
            notificationLog.setErrorMessage(ex.getMessage());
        } finally {
            notificationLogRepository.save(notificationLog);
        }
    }
}
