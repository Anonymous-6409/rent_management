package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.model.Property;
import com.moneytree.rentmanagement.model.PropertyStatus;
import com.moneytree.rentmanagement.repository.OwnerRepository;
import com.moneytree.rentmanagement.repository.PropertyRepository;
import com.moneytree.rentmanagement.repository.TenantRepository;
import com.moneytree.rentmanagement.security.CurrentUserService;
import com.moneytree.rentmanagement.service.OwnerService;
import com.moneytree.rentmanagement.service.PropertyService;
import com.moneytree.rentmanagement.model.Owner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PropertyController.class)
@WithMockUser
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PropertyService propertyService;
    @MockBean
    private OwnerService ownerService;
    @MockBean
    private CurrentUserService currentUserService;

    // Required because @WebMvcTest auto-detects the Converter @Components,
    // which depend on these repositories.
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
    void list_asAdmin_showsAllProperties() throws Exception {
        when(propertyService.findAll()).thenReturn(List.of(new Property()));

        mockMvc.perform(get("/properties"))
                .andExpect(status().isOk())
                .andExpect(view().name("properties/list"))
                .andExpect(model().attributeExists("properties"));

        verify(propertyService).findAll();
        verify(propertyService, never()).findByOwner(anyLong());
    }

    @Test
    void list_asOwner_showsOnlyOwnedProperties() throws Exception {
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.currentOwnerId()).thenReturn(java.util.Optional.of(5L));
        when(propertyService.findByOwner(5L)).thenReturn(List.of(new Property()));

        mockMvc.perform(get("/properties"))
                .andExpect(status().isOk())
                .andExpect(view().name("properties/list"))
                .andExpect(model().attributeExists("properties"));

        verify(propertyService).findByOwner(5L);
        verify(propertyService, never()).findAll();
    }

    @Test
    void createForm_rendersFormWithBlankProperty() throws Exception {
        mockMvc.perform(get("/properties/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("properties/form"))
                .andExpect(model().attributeExists("property"))
                .andExpect(model().attributeExists("statuses"));
    }

    @Test
    void editForm_loadsExistingProperty() throws Exception {
        Property p = new Property();
        p.setId(1L);
        p.setName("Maple Court");
        when(propertyService.findById(1L)).thenReturn(p);

        mockMvc.perform(get("/properties/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("properties/form"))
                .andExpect(model().attribute("property", p));
    }

    @Test
    void save_validProperty_redirectsAndPersists() throws Exception {
        mockMvc.perform(post("/properties/save")
                        .with(csrf())
                        .param("name", "Maple Court")
                        .param("address", "12 Maple St")
                        .param("type", "Apartment")
                        .param("monthlyRent", "1200.00")
                        .param("status", PropertyStatus.VACANT.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/properties"));

        verify(propertyService).save(any(Property.class));
    }

    @Test
    void save_asOwner_forcesOwnershipAndPersists() throws Exception {
        Owner me = new Owner();
        me.setId(9L);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.currentOwner()).thenReturn(java.util.Optional.of(me));

        mockMvc.perform(post("/properties/save")
                        .with(csrf())
                        .param("name", "Maple Court")
                        .param("address", "12 Maple St")
                        .param("type", "Apartment")
                        .param("monthlyRent", "1200.00")
                        .param("status", PropertyStatus.VACANT.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/properties"));

        ArgumentCaptor<Property> captor = ArgumentCaptor.forClass(Property.class);
        verify(propertyService).save(captor.capture());
        assertThat(captor.getValue().getOwner()).isSameAs(me);
    }

    @Test
    void save_invalidProperty_returnsFormAndDoesNotPersist() throws Exception {
        mockMvc.perform(post("/properties/save")
                        .with(csrf())
                        .param("name", "")
                        .param("address", "")
                        .param("type", "")
                        .param("status", PropertyStatus.VACANT.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("properties/form"))
                .andExpect(model().attributeHasFieldErrors("property", "name", "address", "type", "monthlyRent"));

        verify(propertyService, never()).save(any());
    }

    @Test
    void delete_redirectsToList() throws Exception {
        mockMvc.perform(get("/properties/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/properties"));

        verify(propertyService).deleteById(1L);
    }
}
