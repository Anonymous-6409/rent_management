package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.model.Payment;
import com.moneytree.rentmanagement.model.PaymentStatus;
import com.moneytree.rentmanagement.model.Tenant;
import com.moneytree.rentmanagement.repository.OwnerRepository;
import com.moneytree.rentmanagement.repository.PropertyRepository;
import com.moneytree.rentmanagement.repository.TenantRepository;
import com.moneytree.rentmanagement.security.CurrentUserService;
import com.moneytree.rentmanagement.service.PaymentService;
import com.moneytree.rentmanagement.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@WithMockUser
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;
    @MockBean
    private TenantService tenantService;

    // @WebMvcTest auto-detects the Converter @Components; supply their repositories.
    @MockBean
    private CurrentUserService currentUserService;
    @MockBean
    private PropertyRepository propertyRepository;
    @MockBean
    private TenantRepository tenantRepository;
    @MockBean
    private OwnerRepository ownerRepository;

    @BeforeEach
    void defaultAdmin() {
        lenient().when(currentUserService.isAdmin()).thenReturn(true);
    }

    @Test
    void list_rendersListView() throws Exception {
        when(paymentService.findAll()).thenReturn(List.of(new Payment()));

        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(view().name("payments/list"))
                .andExpect(model().attributeExists("payments"));
    }

    @Test
    void createForm_rendersFormWithTenantsAndStatuses() throws Exception {
        when(tenantService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/payments/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("payments/form"))
                .andExpect(model().attributeExists("payment"))
                .andExpect(model().attributeExists("tenants"))
                .andExpect(model().attributeExists("statuses"));
    }

    @Test
    void save_validPayment_bindsTenantAndRedirects() throws Exception {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));

        mockMvc.perform(post("/payments/save")
                        .with(csrf())
                        .param("tenant", "1")
                        .param("periodMonth", "2026-06")
                        .param("amount", "1200.00")
                        .param("paymentDate", "2026-06-01")
                        .param("status", PaymentStatus.PAID.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments"));

        verify(paymentService).save(any(Payment.class));
    }

    @Test
    void save_invalidPayment_returnsFormAndDoesNotPersist() throws Exception {
        when(tenantService.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/payments/save")
                        .with(csrf())
                        .param("status", PaymentStatus.PAID.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("payments/form"))
                .andExpect(model().attributeHasFieldErrors("payment", "amount", "paymentDate", "periodMonth"));

        verify(paymentService, never()).save(any());
    }

    @Test
    void delete_redirectsToList() throws Exception {
        mockMvc.perform(get("/payments/delete/3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments"));

        verify(paymentService).deleteById(3L);
    }
}
