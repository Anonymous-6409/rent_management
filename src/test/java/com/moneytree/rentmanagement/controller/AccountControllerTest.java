package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.repository.OwnerRepository;
import com.moneytree.rentmanagement.repository.PropertyRepository;
import com.moneytree.rentmanagement.repository.TenantRepository;
import com.moneytree.rentmanagement.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@WithMockUser(username = "jane")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // @WebMvcTest auto-detects the Converter @Components; supply their repositories.
    @MockBean
    private PropertyRepository propertyRepository;
    @MockBean
    private TenantRepository tenantRepository;
    @MockBean
    private OwnerRepository ownerRepository;

    @Test
    void passwordForm_renders() throws Exception {
        mockMvc.perform(get("/account/password"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/password"))
                .andExpect(model().attributeExists("changePasswordForm"));
    }

    @Test
    void changePassword_valid_updatesAndRedirects() throws Exception {
        mockMvc.perform(post("/account/password")
                        .with(csrf())
                        .param("currentPassword", "oldpw")
                        .param("newPassword", "newpw123")
                        .param("confirmPassword", "newpw123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/password?changed"));

        verify(userService).changePassword("jane", "oldpw", "newpw123");
    }

    @Test
    void changePassword_mismatch_returnsFormWithError() throws Exception {
        mockMvc.perform(post("/account/password")
                        .with(csrf())
                        .param("currentPassword", "oldpw")
                        .param("newPassword", "newpw123")
                        .param("confirmPassword", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/password"))
                .andExpect(model().attributeHasFieldErrors("changePasswordForm", "confirmPassword"));

        verify(userService, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    void changePassword_wrongCurrent_returnsFormWithError() throws Exception {
        doThrow(new IllegalArgumentException("Current password is incorrect"))
                .when(userService).changePassword(eq("jane"), anyString(), anyString());

        mockMvc.perform(post("/account/password")
                        .with(csrf())
                        .param("currentPassword", "wrong")
                        .param("newPassword", "newpw123")
                        .param("confirmPassword", "newpw123"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/password"))
                .andExpect(model().attributeHasFieldErrors("changePasswordForm", "currentPassword"));
    }
}
