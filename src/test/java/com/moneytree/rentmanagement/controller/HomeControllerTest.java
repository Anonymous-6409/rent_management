package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.repository.OwnerRepository;
import com.moneytree.rentmanagement.repository.PropertyRepository;
import com.moneytree.rentmanagement.repository.TenantRepository;
import com.moneytree.rentmanagement.service.OwnerService;
import com.moneytree.rentmanagement.service.PaymentService;
import com.moneytree.rentmanagement.service.PropertyService;
import com.moneytree.rentmanagement.service.ReminderService;
import com.moneytree.rentmanagement.service.TenantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PropertyService propertyService;
    @MockBean
    private TenantService tenantService;
    @MockBean
    private PaymentService paymentService;
    @MockBean
    private OwnerService ownerService;
    @MockBean
    private ReminderService reminderService;

    // Required because @WebMvcTest auto-detects the Converter @Components,
    // which depend on these repositories.
    @MockBean
    private PropertyRepository propertyRepository;
    @MockBean
    private TenantRepository tenantRepository;
    @MockBean
    private OwnerRepository ownerRepository;

    @Test
    void dashboard_rendersWithSummaryAttributes() throws Exception {
        when(propertyService.count()).thenReturn(3L);
        when(tenantService.count()).thenReturn(5L);
        when(paymentService.totalCollected()).thenReturn(new BigDecimal("2500.00"));
        when(paymentService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("propertyCount", 3L))
                .andExpect(model().attribute("tenantCount", 5L))
                .andExpect(model().attributeExists("totalCollected"))
                .andExpect(model().attributeExists("recentPayments"));
    }
}
