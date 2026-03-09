package com.wenn.notificationservice.service;

import com.wenn.notificationservice.client.UserServiceClient;
import com.yuranium.javalabcore.TaskCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final UserServiceClient userServiceClient;
    private final EmailService emailService;

    public void dispatchTaskCreated(TaskCreatedEvent event) {

        int page = 0;
        int size = 30;

        while (true) {

            List<String> emails = userServiceClient.getEmails(page, size);

            if (emails.isEmpty()) break;

            for (String email : emails) {

                emailService.sendTaskCreatedEmail(
                        email,
                        event.title(),
                        event.difficulty(),
                        event.categories()
                );
            }

            if (emails.size() < size) break;

            page++;
        }
    }
}
