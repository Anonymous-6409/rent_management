package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.model.Payment;
import com.moneytree.rentmanagement.model.PaymentStatus;
import com.moneytree.rentmanagement.model.Property;
import com.moneytree.rentmanagement.model.Tenant;
import com.moneytree.rentmanagement.security.CurrentUserService;
import com.moneytree.rentmanagement.service.PaymentService;
import com.moneytree.rentmanagement.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final TenantService tenantService;
    private final CurrentUserService currentUserService;

    public PaymentController(PaymentService paymentService, TenantService tenantService,
                             CurrentUserService currentUserService) {
        this.paymentService = paymentService;
        this.tenantService = tenantService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String list(Model model) {
        if (currentUserService.isAdmin()) {
            model.addAttribute("payments", paymentService.findAll());
        } else {
            model.addAttribute("payments", currentUserService.currentOwnerId()
                    .map(paymentService::findByOwner).orElse(List.of()));
        }
        return "payments/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("payment", new Payment());
        model.addAttribute("tenants", visibleTenants());
        model.addAttribute("statuses", PaymentStatus.values());
        return "payments/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Payment payment = paymentService.findById(id);
        assertCanModify(payment);
        model.addAttribute("payment", payment);
        model.addAttribute("tenants", visibleTenants());
        model.addAttribute("statuses", PaymentStatus.values());
        return "payments/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("payment") Payment payment,
                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("tenants", visibleTenants());
            model.addAttribute("statuses", PaymentStatus.values());
            return "payments/form";
        }
        if (!currentUserService.isAdmin()) {
            assertOwnsTenant(payment.getTenant());
            if (payment.getId() != null) {
                assertCanModify(paymentService.findById(payment.getId()));
            }
        }
        paymentService.save(payment);
        return "redirect:/payments";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        if (!currentUserService.isAdmin()) {
            assertCanModify(paymentService.findById(id));
        }
        paymentService.deleteById(id);
        return "redirect:/payments";
    }

    private List<Tenant> visibleTenants() {
        if (currentUserService.isAdmin()) {
            return tenantService.findAll();
        }
        return currentUserService.currentOwnerId().map(tenantService::findByOwner).orElse(List.of());
    }

    private void assertCanModify(Payment payment) {
        if (currentUserService.isAdmin()) {
            return;
        }
        assertOwnsTenant(payment.getTenant());
    }

    private void assertOwnsTenant(Tenant tenant) {
        if (currentUserService.isAdmin()) {
            return;
        }
        Property property = tenant != null ? tenant.getProperty() : null;
        if (property == null || !currentUserService.ownsOwner(property.getOwner())) {
            throw new AccessDeniedException("You do not have access to this tenant");
        }
    }
}
