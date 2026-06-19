package com.moneytree.rentmanagement.repository;

import com.moneytree.rentmanagement.model.Payment;
import com.moneytree.rentmanagement.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByTenantId(Long tenantId);

    boolean existsByTenantIdAndPeriodMonthAndStatus(Long tenantId,
                                                    String periodMonth,
                                                    PaymentStatus status);
}
