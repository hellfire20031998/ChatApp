package com.hellFire.Real_Time_Notifications_System.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.from:no-reply@chatapp.local}") String fromAddress
    ) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.fromAddress = fromAddress;
    }

    public void sendEmailVerification(String toEmail, String verifyUrl) {
        if (mailSender == null) {
            log.warn("JavaMailSender bean missing; verification email not sent. to={}, verifyUrl={}", toEmail, verifyUrl);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Verify your ChatApp account");
        message.setText(
                "Welcome to ChatApp!\n\n"
                        + "Verify your email by opening this link:\n"
                        + verifyUrl
                        + "\n\n"
                        + "If you did not create this account, please ignore this email."
        );
        mailSender.send(message);
    }
}
