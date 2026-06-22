package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.model.Reminder;
import com.moneytree.rentmanagement.repository.OwnerRepository;
import com.moneytree.rentmanagement.repository.PropertyRepository;
import com.moneytree.rentmanagement.repository.TenantRepository;
import com.moneytree.rentmanagement.security.CurrentUserService;
import com.moneytree.rentmanagement.service.ReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReminderController.class)
@WithMockUser
class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReminderService reminderService;
    @MockBean
    private CurrentUserService currentUserService;

    // @WebMvcTest auto-detects the Converter @Components; supply their repositories.
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
        when(reminderService.findAll()).thenReturn(List.of(new Reminder()));

        mockMvc.perform(get("/reminders"))
                .andExpect(status().isOk())
                .andExpect(view().name("reminders/list"))
                .andExpect(model().attributeExists("reminders"));
    }

    @Test
    void generate_invokesServiceAndRedirects() throws Exception {
        when(reminderService.generateRemindersForCurrentPeriod()).thenReturn(List.of(new Reminder(), new Reminder()));

        mockMvc.perform(post("/reminders/generate").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reminders"))
                .andExpect(flash().attributeExists("message"));

        verify(reminderService).generateRemindersForCurrentPeriod();
    }

    @Test
    void send_invokesServiceAndRedirects() throws Exception {
        when(reminderService.sendPendingReminders()).thenReturn(3);

        mockMvc.perform(post("/reminders/send").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reminders"))
                .andExpect(flash().attributeExists("message"));

        verify(reminderService).sendPendingReminders();
    }

    @Test
    void delete_invokesServiceAndRedirects() throws Exception {
        mockMvc.perform(get("/reminders/delete/7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reminders"));

        verify(reminderService).deleteById(7L);
    }
}
