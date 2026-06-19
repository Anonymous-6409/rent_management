package com.moneytree.rentmanagement.notification;

import com.moneytree.rentmanagement.model.Reminder;

/**
 * Delivers a rent {@link Reminder} to its recipient.
 * The active implementation is selected by configuration:
 * {@link LoggingNotificationSender} by default, or {@link EmailNotificationSender}
 * when {@code app.reminders.email.enabled=true}.
 */
public interface NotificationSender {

    void send(Reminder reminder);

    default String subjectFor(Reminder reminder) {
        return "Rent reminder for " + reminder.getPeriodMonth();
    }
}
