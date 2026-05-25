package com.studentplansystem.studyplangym.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${resend.from-email}")
    private String fromEmail;

    public void sendTemporaryPasswordEmail(String toEmail, String username, String temporaryPassword) {
        Resend resend = new Resend(resendApiKey);

        String htmlContent =
                "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #1f2937;'>" +
                        "<h2>Study Plan System Password Reset</h2>" +
                        "<p>Hello,</p>" +
                        "<p>Your Study Plan System password has been reset by an administrator.</p>" +
                        "<p><strong>Username:</strong> " + escapeHtml(username) + "</p>" +
                        "<p><strong>Temporary password:</strong> " + escapeHtml(temporaryPassword) + "</p>" +
                        "<p>Please log in using this temporary password. After logging in, you will be asked to create a new password.</p>" +
                        "<p>If you did not request this password reset, please contact the school administrator.</p>" +
                        "<p>Thank you.</p>" +
                        "</div>";

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject("Study Plan System Password Reset")
                .html(htmlContent)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            System.out.println("Resend email sent. ID: " + response.getId());
        } catch (ResendException e) {
            throw new RuntimeException("Could not send password reset email using Resend.", e);
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}