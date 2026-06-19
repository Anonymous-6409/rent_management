package com.moneytree.rentmanagement.notification;

import com.moneytree.rentmanagement.model.Reminder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default sender used when email delivery is disabled. It simply logs the reminder,
 * which keeps the app fully runnable without any SMTP configuration.
 */
@Component
public class LoggingNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationSender.class);

    @Override
    public void send(Reminder reminder) {
        log.info("[REMINDER -> {} ({})] to <{}> | {} | {}",
                reminder.getRecipientType(),
                reminder.getTenant() != null ? reminder.getTenant().getName() : "n/a",
                reminder.getRecipientEmail(),
                subjectFor(reminder),
                reminder.getMessage());
    }
}
