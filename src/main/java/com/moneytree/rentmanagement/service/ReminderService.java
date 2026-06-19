package com.moneytree.rentmanagement.service;

import com.moneytree.rentmanagement.model.*;
import com.moneytree.rentmanagement.notification.NotificationSender;
import com.moneytree.rentmanagement.repository.PaymentRepository;
import com.moneytree.rentmanagement.repository.ReminderRepository;
import com.moneytree.rentmanagement.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates and dispatches rent payment reminders to tenants and property owners.
 *
 * A reminder is generated for a tenant (and the property's owner) when, for a given
 * billing period, the tenant has no {@link PaymentStatus#PAID} payment recorded.
 * Generation is idempotent per (tenant, period, recipient): re-running will not create
 * duplicates.
 */
@Service
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);

    private final ReminderRepository reminderRepository;
    private final TenantRepository tenantRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationSender notificationSender;
    private final int dueDay;

    public ReminderService(ReminderRepository reminderRepository,
                           TenantRepository tenantRepository,
                           PaymentRepository paymentRepository,
                           NotificationSender notificationSender,
                           @Value("${app.reminders.due-day:5}") int dueDay) {
        this.reminderRepository = reminderRepository;
        this.tenantRepository = tenantRepository;
        this.paymentRepository = paymentRepository;
        this.notificationSender = notificationSender;
        this.dueDay = dueDay;
    }

    public List<Reminder> findAll() {
        return reminderRepository.findAllByOrderByCreatedAtDesc();
    }

    public void deleteById(Long id) {
        reminderRepository.deleteById(id);
    }

    public long count() {
        return reminderRepository.count();
    }

    /** Generate reminders for the current calendar month. */
    public List<Reminder> generateRemindersForCurrentPeriod() {
        return generateReminders(YearMonth.now());
    }

    /** Generate reminders for the given billing period, skipping tenants already paid. */
    public List<Reminder> generateReminders(YearMonth period) {
        String periodMonth = period.toString(); // yyyy-MM
        LocalDate dueDate = period.atDay(Math.min(dueDay, period.lengthOfMonth()));
        List<Reminder> created = new ArrayList<>();

        for (Tenant tenant : tenantRepository.findAll()) {
            Property property = tenant.getProperty();
            if (property == null) {
                continue; // no property -> nothing to bill
            }
            boolean alreadyPaid = paymentRepository.existsByTenantIdAndPeriodMonthAndStatus(
                    tenant.getId(), periodMonth, PaymentStatus.PAID);
            if (alreadyPaid) {
                continue;
            }

            BigDecimal amount = property.getMonthlyRent();

            // Reminder to the tenant.
            addIfAbsent(created, tenant, property, periodMonth, amount, dueDate,
                    RecipientType.TENANT, tenant.getEmail());

            // Reminder to the property owner, if one is on file.
            Owner owner = property.getOwner();
            if (owner != null && owner.getEmail() != null && !owner.getEmail().isBlank()) {
                addIfAbsent(created, tenant, property, periodMonth, amount, dueDate,
                        RecipientType.OWNER, owner.getEmail());
            }
        }

        log.info("Generated {} reminder(s) for period {}", created.size(), periodMonth);
        return created;
    }

    private void addIfAbsent(List<Reminder> created, Tenant tenant, Property property,
                             String periodMonth, BigDecimal amount, LocalDate dueDate,
                             RecipientType type, String email) {
        boolean exists = reminderRepository
                .existsByTenantIdAndPeriodMonthAndRecipientType(tenant.getId(), periodMonth, type);
        if (exists) {
            return;
        }
        Reminder reminder = new Reminder();
        reminder.setTenant(tenant);
        reminder.setProperty(property);
        reminder.setPeriodMonth(periodMonth);
        reminder.setAmountDue(amount);
        reminder.setDueDate(dueDate);
        reminder.setRecipientType(type);
        reminder.setRecipientEmail(email);
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setMessage(buildMessage(type, tenant, property, periodMonth, amount, dueDate));
        created.add(reminderRepository.save(reminder));
    }

    private String buildMessage(RecipientType type, Tenant tenant, Property property,
                                String periodMonth, BigDecimal amount, LocalDate dueDate) {
        String propertyName = property.getName();
        if (type == RecipientType.TENANT) {
            return String.format(
                    "Dear %s, this is a reminder that your rent of $%s for %s (%s) is due by %s. "
                            + "Please make your payment to avoid late fees.",
                    tenant.getName(), amount, propertyName, periodMonth, dueDate);
        }
        return String.format(
                "Hello, rent of $%s from tenant %s for %s (%s) is still outstanding (due %s).",
                amount, tenant.getName(), propertyName, periodMonth, dueDate);
    }

    /** Dispatch every PENDING reminder via the active {@link NotificationSender}. */
    public int sendPendingReminders() {
        List<Reminder> pending = reminderRepository.findByStatus(ReminderStatus.PENDING);
        int sent = 0;
        for (Reminder reminder : pending) {
            try {
                notificationSender.send(reminder);
                reminder.setStatus(ReminderStatus.SENT);
                reminder.setSentAt(LocalDateTime.now());
                sent++;
            } catch (Exception ex) {
                log.error("Failed to send reminder {} to {}", reminder.getId(),
                        reminder.getRecipientEmail(), ex);
                reminder.setStatus(ReminderStatus.FAILED);
            }
            reminderRepository.save(reminder);
        }
        log.info("Sent {} of {} pending reminder(s)", sent, pending.size());
        return sent;
    }
}
