package com.moneytree.rentmanagement.service;

import com.moneytree.rentmanagement.model.Payment;
import com.moneytree.rentmanagement.model.PaymentStatus;
import com.moneytree.rentmanagement.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment(Long id, String amount, PaymentStatus status) {
        Payment p = new Payment();
        p.setId(id);
        p.setAmount(new BigDecimal(amount));
        p.setPaymentDate(LocalDate.of(2026, 6, 1));
        p.setPeriodMonth("2026-06");
        p.setStatus(status);
        return p;
    }

    @Test
    void findAll_returnsAllPayments() {
        when(paymentRepository.findAll()).thenReturn(List.of(payment(1L, "100", PaymentStatus.PAID)));

        assertThat(paymentService.findAll()).hasSize(1);
    }

    @Test
    void findById_missingId_throwsException() {
        when(paymentRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.findById(7L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("7");
    }

    @Test
    void findByTenant_delegatesToRepository() {
        when(paymentRepository.findByTenantId(2L)).thenReturn(List.of(payment(1L, "100", PaymentStatus.PAID)));

        assertThat(paymentService.findByTenant(2L)).hasSize(1);
        verify(paymentRepository).findByTenantId(2L);
    }

    @Test
    void totalCollected_sumsOnlyPaidPayments() {
        when(paymentRepository.findAll()).thenReturn(List.of(
                payment(1L, "1000.00", PaymentStatus.PAID),
                payment(2L, "500.50", PaymentStatus.PAID),
                payment(3L, "750.00", PaymentStatus.PENDING),
                payment(4L, "300.00", PaymentStatus.OVERDUE)
        ));

        BigDecimal total = paymentService.totalCollected();

        assertThat(total).isEqualByComparingTo("1500.50");
    }

    @Test
    void totalCollected_noPaidPayments_returnsZero() {
        when(paymentRepository.findAll()).thenReturn(List.of(
                payment(1L, "750.00", PaymentStatus.PENDING)
        ));

        assertThat(paymentService.totalCollected()).isEqualByComparingTo("0");
    }

    @Test
    void save_delegatesToRepository() {
        Payment p = payment(null, "100", PaymentStatus.PAID);
        when(paymentRepository.save(p)).thenReturn(payment(1L, "100", PaymentStatus.PAID));

        assertThat(paymentService.save(p).getId()).isEqualTo(1L);
    }

    @Test
    void deleteById_delegatesToRepository() {
        paymentService.deleteById(9L);

        verify(paymentRepository).deleteById(9L);
    }
}
