package com.wenn.notificationservice.service.kafka;

import com.wenn.notificationservice.service.EmailService;
import com.wenn.notificationservice.service.NotificationDispatcher;
import com.wenn.notificationservice.util.exception.NotificationException;
import com.yuranium.javalabcore.TaskCreatedEvent;
import com.yuranium.javalabcore.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@KafkaListener(
        topics = {
                "${kafka.topic-names.user-registered}",
                "${kafka.topic-names.task-created}"
        },
        containerFactory = "kafkaListenerContainerFactory"
)
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    private final EmailService emailService;

    private final NotificationDispatcher dispatcher;

    @KafkaHandler
    public void consume(UserRegisteredEvent event) {
        try {
            log.info("Received event: {}", event);

            if (event == null
                    || event.email() == null || event.email().isBlank()
                    || event.authCode() == null || event.id() == null) {
                throw new NotificationException("Invalid event payload: " + event);
            }

            emailService.sendVerificationCode(event.email(), event.username(), event.authCode());

            log.info("Email sent to {} for user id {}", event.email(), event.id());
        } catch (Exception ex) {
            log.error("Failed to process event: {}", ex.getMessage(), ex);

            throw new NotificationException("Failed to process UserRegisteredEvent: " + ex.getMessage(), ex);
        }
    }

    @KafkaHandler
    public void consume(TaskCreatedEvent event) {

        try {

            log.info("Received TaskCreatedEvent: {}", event);

            if (event == null
                    || event.title() == null || event.title().isBlank()
                    || event.difficulty() == null || event.difficulty().isBlank()
                    || event.categories() == null) {
                throw new NotificationException("Invalid TaskCreatedEvent payload: " + event);
            }

            dispatcher.dispatchTaskCreated(event);

            log.info("TaskCreated notification processed for task '{}'", event.title());
        } catch (Exception ex) {
            log.error("Failed to process TaskCreatedEvent: {}", ex.getMessage(), ex);

            throw new NotificationException("Failed to process TaskCreatedEvent: " + ex.getMessage(), ex);
        }
    }
}
