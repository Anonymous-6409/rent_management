package com.moneytree.rentmanagement.repository;

import com.moneytree.rentmanagement.model.Payment;
import com.moneytree.rentmanagement.model.PaymentStatus;
import com.moneytree.rentmanagement.model.Tenant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant persistTenant(String name) {
        Tenant t = new Tenant();
        t.setName(name);
        t.setEmail(name.toLowerCase() + "@example.com");
        t.setPhone("555-0000");
        return tenantRepository.save(t);
    }

    private Payment payment(Tenant tenant, String amount) {
        Payment p = new Payment();
        p.setTenant(tenant);
        p.setAmount(new BigDecimal(amount));
        p.setPaymentDate(LocalDate.of(2026, 6, 1));
        p.setPeriodMonth("2026-06");
        p.setStatus(PaymentStatus.PAID);
        return p;
    }

    @Test
    void findByTenantId_returnsOnlyMatchingTenantsPayments() {
        Tenant alice = persistTenant("Alice");
        Tenant bob = persistTenant("Bob");

        paymentRepository.save(payment(alice, "100"));
        paymentRepository.save(payment(alice, "200"));
        paymentRepository.save(payment(bob, "300"));

        List<Payment> alicePayments = paymentRepository.findByTenantId(alice.getId());

        assertThat(alicePayments).hasSize(2);
        assertThat(alicePayments).allMatch(p -> p.getTenant().getId().equals(alice.getId()));
    }

    @Test
    void findByTenantId_noPayments_returnsEmptyList() {
        Tenant carol = persistTenant("Carol");

        assertThat(paymentRepository.findByTenantId(carol.getId())).isEmpty();
    }

    @Test
    void save_persistsAndAssignsId() {
        Tenant dave = persistTenant("Dave");

        Payment saved = paymentRepository.save(payment(dave, "450.75"));

        assertThat(saved.getId()).isNotNull();
        assertThat(paymentRepository.findById(saved.getId())).isPresent();
    }
}
