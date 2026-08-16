package com.payverse.notificationservice.service;

import com.payverse.notificationservice.event.PaymentEvent;
import com.payverse.notificationservice.model.Notification;
import com.payverse.notificationservice.repository.NotificationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventConsumer {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public PaymentEventConsumer(
            NotificationRepository notificationRepository,
            SimpMessagingTemplate messagingTemplate) {

        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(
            topics = "payment-events",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(PaymentEvent event) {

        System.out.println(
                "Received payment event: "
                        + event.getTransactionId()
                        + " | type="
                        + event.getEventType()
                        + " | amount="
                        + event.getAmount()
        );

        Notification notification = new Notification();

        /*
         * IMPORTANT:
         * PaymentEvent currently contains wallet IDs,
         * not user IDs.
         *
         * Temporarily using senderWalletId as userId
         * for our end-to-end test.
         */
        notification.setUserId(event.getSenderWalletId());

        notification.setMessage(
                "Payment "
                        + event.getEventType()
                        + " of "
                        + event.getAmount()
        );

        notification.setType(event.getEventType());

        notificationRepository.save(notification);

        System.out.println(
                "Notification saved for userId="
                        + notification.getUserId()
        );

        /*
         * Push notification to the user's WebSocket destination.
         *
         * Example:
         * userId = 1
         *
         * destination:
         * /user/1/notifications
         */
        String destination =
                "/user/"
                        + notification.getUserId()
                        + "/notifications";

        messagingTemplate.convertAndSend(
                destination,
                notification
        );

        System.out.println(
                "WebSocket notification pushed to "
                        + destination
        );
    }
}
