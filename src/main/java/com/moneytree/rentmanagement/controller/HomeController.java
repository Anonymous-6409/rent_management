package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.service.OwnerService;
import com.moneytree.rentmanagement.service.PaymentService;
import com.moneytree.rentmanagement.service.PropertyService;
import com.moneytree.rentmanagement.service.ReminderService;
import com.moneytree.rentmanagement.service.TenantService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final PropertyService propertyService;
    private final TenantService tenantService;
    private final PaymentService paymentService;
    private final OwnerService ownerService;
    private final ReminderService reminderService;

    public HomeController(PropertyService propertyService,
                          TenantService tenantService,
                          PaymentService paymentService,
                          OwnerService ownerService,
                          ReminderService reminderService) {
        this.propertyService = propertyService;
        this.tenantService = tenantService;
        this.paymentService = paymentService;
        this.ownerService = ownerService;
        this.reminderService = reminderService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("ownerCount", ownerService.count());
        model.addAttribute("propertyCount", propertyService.count());
        model.addAttribute("tenantCount", tenantService.count());
        model.addAttribute("totalCollected", paymentService.totalCollected());
        model.addAttribute("reminderCount", reminderService.count());
        model.addAttribute("recentPayments", paymentService.findAll());
        return "dashboard";
    }
}
