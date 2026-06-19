package com.moneytree.rentmanagement.repository;

import com.moneytree.rentmanagement.model.RecipientType;
import com.moneytree.rentmanagement.model.Reminder;
import com.moneytree.rentmanagement.model.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByStatus(ReminderStatus status);

    List<Reminder> findAllByOrderByCreatedAtDesc();

    boolean existsByTenantIdAndPeriodMonthAndRecipientType(Long tenantId,
                                                           String periodMonth,
                                                           RecipientType recipientType);
}
