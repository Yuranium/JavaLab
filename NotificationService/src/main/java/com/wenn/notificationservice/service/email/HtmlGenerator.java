package com.wenn.notificationservice.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.OffsetDateTime;
import java.time.Year;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class HtmlGenerator {

    private final SpringTemplateEngine templateEngine;

    public String generateVerificationHtml(String username, String email, Integer code) {

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("email", email);
        context.setVariable("code", code);
        context.setVariable("year", Year.now().getValue());

        return templateEngine.process("email/email-verification", context);
    }

    public String generateTaskCreatedHtml(String title,
                                          String difficulty,
                                          Collection<String> categories) {

        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("difficulty", difficulty);
        context.setVariable("categories", categories);
        context.setVariable("year", Year.now().getValue());

        return templateEngine.process("email/task-created", context);
    }

    public String generateUserLockedHtml(String username,
                                         OffsetDateTime startLock,
                                         OffsetDateTime endLock,
                                         boolean isLock,
                                         String message) {

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("startLock", startLock);
        context.setVariable("endLock", endLock);
        context.setVariable("isLock", isLock);
        context.setVariable("message", message);
        context.setVariable("year", Year.now().getValue());

        return templateEngine.process("email/user-locked", context);
    }
}