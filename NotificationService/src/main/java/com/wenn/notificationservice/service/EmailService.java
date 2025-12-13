package com.wenn.notificationservice.service;

import com.wenn.notificationservice.service.email.HtmlGenerator;
import com.wenn.notificationservice.util.exception.EmailSendException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;
    private final JavaMailSender mailSender;
    private final HtmlGenerator htmlGenerator;


    public void sendVerificationCode(String toEmail, String username, Integer code) {
        try {
            String html = htmlGenerator.generateVerificationHtml(username, toEmail, code);
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "utf-8");
            helper.setText(html, true);
            helper.setTo(toEmail);
            helper.setSubject("Код подтверждения — JavaLab");
            helper.setFrom(fromEmail);
            mailSender.send(msg);
        } catch (Exception ex) {
            throw new EmailSendException("Failed to send email to " + toEmail + ": " + ex.getMessage(), ex);
        }
    }
}
