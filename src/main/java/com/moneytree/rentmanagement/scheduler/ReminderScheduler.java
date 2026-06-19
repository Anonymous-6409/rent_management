package com.moneytree.rentmanagement.scheduler;

import com.moneytree.rentmanagement.service.ReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically generates rent reminders for the current period and dispatches any
 * pending ones. The schedule is configurable via {@code app.reminders.cron}
 * (default: every day at 09:00).
 */
@Component
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final ReminderService reminderService;

    public ReminderScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(cron = "${app.reminders.cron:0 0 9 * * *}")
    public void runDailyReminders() {
        log.info("Running scheduled rent reminder job");
        reminderService.generateRemindersForCurrentPeriod();
        reminderService.sendPendingReminders();
    }
}
