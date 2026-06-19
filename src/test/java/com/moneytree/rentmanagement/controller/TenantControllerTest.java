package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.model.Property;
import com.moneytree.rentmanagement.model.Tenant;
import com.moneytree.rentmanagement.repository.OwnerRepository;
import com.moneytree.rentmanagement.repository.PropertyRepository;
import com.moneytree.rentmanagement.repository.TenantRepository;
import com.moneytree.rentmanagement.service.PropertyService;
import com.moneytree.rentmanagement.service.TenantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TenantController.class)
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TenantService tenantService;
    @MockBean
    private PropertyService propertyService;

    // @WebMvcTest auto-detects the Converter @Components; supply their repositories.
    @MockBean
    private PropertyRepository propertyRepository;
    @MockBean
    private TenantRepository tenantRepository;
    @MockBean
    private OwnerRepository ownerRepository;

    @Test
    void list_rendersListView() throws Exception {
        when(tenantService.findAll()).thenReturn(List.of(new Tenant()));

        mockMvc.perform(get("/tenants"))
                .andExpect(status().isOk())
                .andExpect(view().name("tenants/list"))
                .andExpect(model().attributeExists("tenants"));
    }

    @Test
    void createForm_rendersFormWithPropertyList() throws Exception {
        when(propertyService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/tenants/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("tenants/form"))
                .andExpect(model().attributeExists("tenant"))
                .andExpect(model().attributeExists("properties"));
    }

    @Test
    void save_validTenant_bindsPropertyAndRedirects() throws Exception {
        Property property = new Property();
        property.setId(1L);
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));

        mockMvc.perform(post("/tenants/save")
                        .param("name", "Jane Doe")
                        .param("email", "jane@example.com")
                        .param("phone", "555-0100")
                        .param("property", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tenants"));

        verify(tenantService).save(any(Tenant.class));
    }

    @Test
    void save_invalidTenant_returnsFormAndDoesNotPersist() throws Exception {
        when(propertyService.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/tenants/save")
                        .param("name", "")
                        .param("email", "not-an-email")
                        .param("phone", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("tenants/form"))
                .andExpect(model().attributeHasFieldErrors("tenant", "name", "email", "phone"));

        verify(tenantService, never()).save(any());
    }

    @Test
    void delete_redirectsToList() throws Exception {
        mockMvc.perform(get("/tenants/delete/2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tenants"));

        verify(tenantService).deleteById(2L);
    }
}
