package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.model.Owner;
import com.moneytree.rentmanagement.notification.AccountEmailService;
import com.moneytree.rentmanagement.service.OwnerService;
import com.moneytree.rentmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/owners")
public class OwnerController {

    private final OwnerService ownerService;
    private final UserService userService;
    private final AccountEmailService accountEmailService;

    public OwnerController(OwnerService ownerService, UserService userService,
                           AccountEmailService accountEmailService) {
        this.ownerService = ownerService;
        this.userService = userService;
        this.accountEmailService = accountEmailService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("owners", ownerService.findAll());
        return "owners/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("owner", new Owner());
        return "owners/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("owner", ownerService.findById(id));
        model.addAttribute("ownerAccount", userService.findOwnerAccount(id).orElse(null));
        model.addAttribute("linkableUsers", userService.linkableUsers());
        return "owners/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("owner") Owner owner, BindingResult result,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "owners/form";
        }
        boolean isNew = owner.getId() == null;
        ownerService.save(owner);

        // On creation, auto-provision a login for the owner (username = email).
        if (isNew && owner.getEmail() != null && !owner.getEmail().isBlank()) {
            if (userService.usernameExists(owner.getEmail())) {
                redirectAttributes.addFlashAttribute("ownerLoginWarning",
                        "A login for " + owner.getEmail() + " already exists, so no new account was created. "
                                + "You can link an existing account from the owner's edit page.");
            } else {
                String tempPassword = userService.createOwnerAccount(owner);
                boolean emailed = accountEmailService.sendOwnerCredentials(
                        owner.getEmail(), owner.getEmail(), tempPassword);
                redirectAttributes.addFlashAttribute("ownerLogin",
                        Map.of("username", owner.getEmail(), "password", tempPassword, "emailed", emailed));
            }
        }
        return "redirect:/owners";
    }

    @PostMapping("/{id}/link")
    public String linkAccount(@PathVariable Long id, @RequestParam Long userId,
                              RedirectAttributes redirectAttributes) {
        Owner owner = ownerService.findById(id);
        userService.linkUserToOwner(userId, owner);
        redirectAttributes.addFlashAttribute("message", "Login account linked to " + owner.getName() + ".");
        return "redirect:/owners/edit/" + id;
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        ownerService.deleteById(id);
        return "redirect:/owners";
    }
}
