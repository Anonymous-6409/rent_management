package com.moneytree.rentmanagement.service;

import com.moneytree.rentmanagement.model.*;
import com.moneytree.rentmanagement.notification.NotificationSender;
import com.moneytree.rentmanagement.repository.PaymentRepository;
import com.moneytree.rentmanagement.repository.ReminderRepository;
import com.moneytree.rentmanagement.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private NotificationSender notificationSender;

    private ReminderService reminderService;

    private final YearMonth period = YearMonth.of(2026, 6);

    @BeforeEach
    void setUp() {
        reminderService = new ReminderService(reminderRepository, tenantRepository,
                paymentRepository, notificationSender, 5);
        // By default echo back the saved reminder so generate() can collect it.
        lenient().when(reminderRepository.save(any(Reminder.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    private Owner owner(String email) {
        Owner o = new Owner();
        o.setId(10L);
        o.setName("Olivia Owner");
        o.setEmail(email);
        return o;
    }

    private Property property(Owner owner) {
        Property p = new Property();
        p.setId(100L);
        p.setName("Maple Court");
        p.setMonthlyRent(new BigDecimal("1200.00"));
        p.setOwner(owner);
        return p;
    }

    private Tenant tenant(Property property) {
        Tenant t = new Tenant();
        t.setId(1L);
        t.setName("Jane Doe");
        t.setEmail("jane@example.com");
        t.setProperty(property);
        return t;
    }

    @Test
    void generate_unpaidTenantWithOwner_createsTenantAndOwnerReminders() {
        Tenant tenant = tenant(property(owner("olivia@example.com")));
        when(tenantRepository.findAll()).thenReturn(List.of(tenant));
        when(paymentRepository.existsByTenantIdAndPeriodMonthAndStatus(1L, "2026-06", PaymentStatus.PAID))
                .thenReturn(false);
        when(reminderRepository.existsByTenantIdAndPeriodMonthAndRecipientType(anyLong(), anyString(), any()))
                .thenReturn(false);

        List<Reminder> created = reminderService.generateReminders(period);

        assertThat(created).hasSize(2);
        assertThat(created).extracting(Reminder::getRecipientType)
                .containsExactlyInAnyOrder(RecipientType.TENANT, RecipientType.OWNER);
        assertThat(created).allMatch(r -> r.getAmountDue().compareTo(new BigDecimal("1200.00")) == 0);
        assertThat(created).allMatch(r -> r.getStatus() == ReminderStatus.PENDING);
        verify(reminderRepository, times(2)).save(any(Reminder.class));
    }

    @Test
    void generate_tenantAlreadyPaid_createsNoReminders() {
        Tenant tenant = tenant(property(owner("olivia@example.com")));
        when(tenantRepository.findAll()).thenReturn(List.of(tenant));
        when(paymentRepository.existsByTenantIdAndPeriodMonthAndStatus(1L, "2026-06", PaymentStatus.PAID))
                .thenReturn(true);

        List<Reminder> created = reminderService.generateReminders(period);

        assertThat(created).isEmpty();
        verify(reminderRepository, never()).save(any());
    }

    @Test
    void generate_tenantWithoutProperty_isSkipped() {
        Tenant tenant = tenant(null);
        when(tenantRepository.findAll()).thenReturn(List.of(tenant));

        List<Reminder> created = reminderService.generateReminders(period);

        assertThat(created).isEmpty();
        verify(paymentRepository, never()).existsByTenantIdAndPeriodMonthAndStatus(anyLong(), anyString(), any());
    }

    @Test
    void generate_propertyWithoutOwner_createsOnlyTenantReminder() {
        Tenant tenant = tenant(property(null));
        when(tenantRepository.findAll()).thenReturn(List.of(tenant));
        when(paymentRepository.existsByTenantIdAndPeriodMonthAndStatus(1L, "2026-06", PaymentStatus.PAID))
                .thenReturn(false);
        when(reminderRepository.existsByTenantIdAndPeriodMonthAndRecipientType(anyLong(), anyString(), any()))
                .thenReturn(false);

        List<Reminder> created = reminderService.generateReminders(period);

        assertThat(created).hasSize(1);
        assertThat(created.get(0).getRecipientType()).isEqualTo(RecipientType.TENANT);
        assertThat(created.get(0).getDueDate()).isEqualTo(period.atDay(5));
    }

    @Test
    void generate_isIdempotent_skipsExistingReminders() {
        Tenant tenant = tenant(property(owner("olivia@example.com")));
        when(tenantRepository.findAll()).thenReturn(List.of(tenant));
        when(paymentRepository.existsByTenantIdAndPeriodMonthAndStatus(1L, "2026-06", PaymentStatus.PAID))
                .thenReturn(false);
        // Both recipient reminders already exist.
        when(reminderRepository.existsByTenantIdAndPeriodMonthAndRecipientType(anyLong(), anyString(), any()))
                .thenReturn(true);

        List<Reminder> created = reminderService.generateReminders(period);

        assertThat(created).isEmpty();
        verify(reminderRepository, never()).save(any());
    }

    @Test
    void sendPending_marksRemindersSent() {
        Reminder r1 = new Reminder();
        r1.setId(1L);
        r1.setStatus(ReminderStatus.PENDING);
        Reminder r2 = new Reminder();
        r2.setId(2L);
        r2.setStatus(ReminderStatus.PENDING);
        when(reminderRepository.findByStatus(ReminderStatus.PENDING)).thenReturn(List.of(r1, r2));

        int sent = reminderService.sendPendingReminders();

        assertThat(sent).isEqualTo(2);
        verify(notificationSender, times(2)).send(any(Reminder.class));
        ArgumentCaptor<Reminder> captor = ArgumentCaptor.forClass(Reminder.class);
        verify(reminderRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).allMatch(r -> r.getStatus() == ReminderStatus.SENT);
        assertThat(captor.getAllValues()).allMatch(r -> r.getSentAt() != null);
    }

    @Test
    void sendPending_whenSenderFails_marksReminderFailed() {
        Reminder r1 = new Reminder();
        r1.setId(1L);
        r1.setStatus(ReminderStatus.PENDING);
        when(reminderRepository.findByStatus(ReminderStatus.PENDING)).thenReturn(List.of(r1));
        doThrow(new RuntimeException("smtp down")).when(notificationSender).send(r1);

        int sent = reminderService.sendPendingReminders();

        assertThat(sent).isZero();
        assertThat(r1.getStatus()).isEqualTo(ReminderStatus.FAILED);
        verify(reminderRepository).save(r1);
    }
}
