package com.hellFire.Real_Time_Notifications_System.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellFire.Real_Time_Notifications_System.exceptions.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private static final String DEFAULT_RESEND_FROM = "ChatApp <onboarding@resend.dev>";

    private final ObjectMapper objectMapper;
    private final JavaMailSender mailSender;
    private final String smtpFromAddress;
    private final String resendApiKey;
    private final String resendFrom;
    private final HttpClient httpClient;

    public EmailService(
            ObjectMapper objectMapper,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.from:no-reply@chatapp.local}") String smtpFromAddress,
            @Value("${app.mail.resend.api-key:}") String resendApiKey,
            @Value("${app.mail.resend.from:}") String resendFrom
    ) {
        this.objectMapper = objectMapper;
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.smtpFromAddress = smtpFromAddress;
        this.resendApiKey = resendApiKey == null ? "" : resendApiKey.trim();
        this.resendFrom = (resendFrom == null || resendFrom.isBlank())
                ? DEFAULT_RESEND_FROM
                : resendFrom.trim();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public void sendEmailVerification(String toEmail, String verifyUrl) {
        String bodyText =
                "Welcome to ChatApp!\n\n"
                        + "Verify your email by opening this link:\n"
                        + verifyUrl
                        + "\n\n"
                        + "If you did not create this account, please ignore this email.";

        if (!resendApiKey.isEmpty()) {
            sendViaResend(toEmail, bodyText);
            return;
        }

        if (mailSender != null) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(smtpFromAddress);
            message.setTo(toEmail);
            message.setSubject("Verify your ChatApp account");
            message.setText(bodyText);
            mailSender.send(message);
            return;
        }

        log.warn("No email configured (set RESEND_API_KEY for Render, or spring.mail.* for SMTP). to={}, verifyUrl={}",
                toEmail, verifyUrl);
    }

    private void sendViaResend(String toEmail, String bodyText) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("from", resendFrom);
            payload.put("to", List.of(toEmail));
            payload.put("subject", "Verify your ChatApp account");
            payload.put("text", bodyText);

            String json = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int code = response.statusCode();
            if (code < 200 || code >= 300) {
                log.error("Resend API error status={} body={}", code, response.body());
                throw new AppException("EMAIL_SEND_FAILED", "Could not send verification email");
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Resend request failed", e);
            throw new AppException("EMAIL_SEND_FAILED", "Could not send verification email");
        }
    }
}
