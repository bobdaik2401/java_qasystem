package com.quyen.qasystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetPasswordEmail(String toEmail, String link) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reset your password");
        message.setText("""
                Hello,

                You requested to reset your password.
                Click the link below to reset it:

                %s

                This link will expire in 15 minutes.
                If you didn't request this, please ignore.

                QASystem Team
                """.formatted(link));

        mailSender.send(message);
    }
}
