package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.repository.OwnerRepository;
import com.moneytree.rentmanagement.repository.PropertyRepository;
import com.moneytree.rentmanagement.repository.TenantRepository;
import com.moneytree.rentmanagement.security.SecurityConfig;
import com.moneytree.rentmanagement.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

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
    void loginPage_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void registerPage_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registrationForm"));
    }

    @Test
    void protectedPage_redirectsToLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void register_validForm_registersUserAndRedirectsToLogin() throws Exception {
        when(userService.usernameExists("jane")).thenReturn(false);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "Jane Doe")
                        .param("username", "jane")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(userService).register(any());
    }

    @Test
    void register_passwordMismatch_returnsFormWithError() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "Jane Doe")
                        .param("username", "jane")
                        .param("password", "secret123")
                        .param("confirmPassword", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "confirmPassword"));

        verify(userService, never()).register(any());
    }

    @Test
    void register_duplicateUsername_returnsFormWithError() throws Exception {
        when(userService.usernameExists("jane")).thenReturn(true);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "Jane Doe")
                        .param("username", "jane")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "username"));

        verify(userService, never()).register(any());
    }
}
