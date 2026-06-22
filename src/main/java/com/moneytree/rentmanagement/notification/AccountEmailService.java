package com.moneytree.rentmanagement.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends account-related emails (e.g. an owner's initial login credentials).
 * Uses the configured {@link JavaMailSender} when available; if mail is not configured
 * it logs a warning instead of failing, so the app still runs without SMTP.
 */
@Service
public class AccountEmailService {

    private static final Logger log = LoggerFactory.getLogger(AccountEmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;

    public AccountEmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                               @Value("${app.reminders.email.from:no-reply@rentmanager.local}") String from) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
    }

    /** Returns true if the credentials email was actually sent. */
    public boolean sendOwnerCredentials(String toEmail, String username, String temporaryPassword) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Mail is not configured; skipping owner credentials email to {}", toEmail);
            return false;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("Your RentManager login");
        message.setText("Hello,\n\n"
                + "An owner account has been created for you in RentManager.\n\n"
                + "Username: " + username + "\n"
                + "Temporary password: " + temporaryPassword + "\n\n"
                + "Please sign in and change your password from the Change Password page.\n\n"
                + "— RentManager");
        try {
            mailSender.send(message);
            log.info("Owner credentials email sent to {}", toEmail);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send owner credentials email to {}", toEmail, ex);
            return false;
        }
    }
}
