package com.studentplansystem.studyplangym.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTemporaryPasswordEmail(String toEmail, String username, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Study Plan System Password Reset");

        message.setText(
                "Hello,\n\n" +
                        "Your Study Plan System password has been reset by an administrator.\n\n" +
                        "Username: " + username + "\n" +
                        "Temporary password: " + temporaryPassword + "\n\n" +
                        "Please log in using this temporary password. After logging in, you will be asked to create a new password.\n\n" +
                        "If you did not request this password reset, please contact the school administrator.\n\n" +
                        "Thank you."
        );

        mailSender.send(message);
    }
}