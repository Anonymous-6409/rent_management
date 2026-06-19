package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.model.Payment;
import com.moneytree.rentmanagement.model.PaymentStatus;
import com.moneytree.rentmanagement.service.PaymentService;
import com.moneytree.rentmanagement.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final TenantService tenantService;

    public PaymentController(PaymentService paymentService, TenantService tenantService) {
        this.paymentService = paymentService;
        this.tenantService = tenantService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("payments", paymentService.findAll());
        return "payments/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("payment", new Payment());
        model.addAttribute("tenants", tenantService.findAll());
        model.addAttribute("statuses", PaymentStatus.values());
        return "payments/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("payment", paymentService.findById(id));
        model.addAttribute("tenants", tenantService.findAll());
        model.addAttribute("statuses", PaymentStatus.values());
        return "payments/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("payment") Payment payment,
                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("tenants", tenantService.findAll());
            model.addAttribute("statuses", PaymentStatus.values());
            return "payments/form";
        }
        paymentService.save(payment);
        return "redirect:/payments";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        paymentService.deleteById(id);
        return "redirect:/payments";
    }
}
