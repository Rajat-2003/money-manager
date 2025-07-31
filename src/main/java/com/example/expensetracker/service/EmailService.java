package com.example.expensetracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
        private final JavaMailSender mailSender;

        @Value("${spring.mail.properties.mail.smtp.from}")
        private String fromEmail;

        public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            System.out.println("Attempting to send email to: " + to);
            mailSender.send(message);
            System.out.println("Email sent successfully!");

        } catch (Exception emailException) {
            System.err.println("Failed to send activation email: " + emailException.getMessage());
            emailException.printStackTrace();
            // Don't throw exception - let the calling method handle it
            throw new RuntimeException("Email sending failed: " + emailException.getMessage());
        }
    }

    
}
