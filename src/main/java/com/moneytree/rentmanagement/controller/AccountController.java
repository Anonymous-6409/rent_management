package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.service.UserService;
import com.moneytree.rentmanagement.web.ChangePasswordForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/password")
    public String passwordForm(Model model) {
        if (!model.containsAttribute("changePasswordForm")) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
        }
        return "account/password";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute("changePasswordForm") ChangePasswordForm form,
                                 BindingResult result, Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (form.getNewPassword() != null && !form.getNewPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }
        if (result.hasErrors()) {
            return "account/password";
        }
        try {
            userService.changePassword(principal.getName(), form.getCurrentPassword(), form.getNewPassword());
        } catch (IllegalArgumentException ex) {
            result.rejectValue("currentPassword", "password.invalid", ex.getMessage());
            return "account/password";
        }
        redirectAttributes.addFlashAttribute("passwordChanged", true);
        return "redirect:/account/password?changed";
    }
}
