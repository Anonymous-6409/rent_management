package com.moneytree.rentmanagement.notification;

import com.moneytree.rentmanagement.model.Reminder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Sends reminders as real emails over SMTP. Only registered (and made primary, so it
 * wins over the logging sender) when {@code app.reminders.email.enabled=true}, in which
 * case a configured {@code spring.mail.*} mail server is required.
 */
@Component
@Primary
@ConditionalOnProperty(name = "app.reminders.email.enabled", havingValue = "true")
public class EmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final JavaMailSender mailSender;
    private final String from;

    public EmailNotificationSender(JavaMailSender mailSender,
                                   @Value("${app.reminders.email.from:no-reply@rentmanager.local}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(Reminder reminder) {
        if (reminder.getRecipientEmail() == null || reminder.getRecipientEmail().isBlank()) {
            throw new IllegalArgumentException("Reminder has no recipient email");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(reminder.getRecipientEmail());
        message.setSubject(subjectFor(reminder));
        message.setText(reminder.getMessage());
        mailSender.send(message);
        log.info("Reminder email sent to {}", reminder.getRecipientEmail());
    }
}
