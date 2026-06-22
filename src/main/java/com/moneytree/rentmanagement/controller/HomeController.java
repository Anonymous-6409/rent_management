package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.security.CurrentUserService;
import com.moneytree.rentmanagement.service.OwnerService;
import com.moneytree.rentmanagement.service.PaymentService;
import com.moneytree.rentmanagement.service.PropertyService;
import com.moneytree.rentmanagement.service.ReminderService;
import com.moneytree.rentmanagement.service.TenantService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final PropertyService propertyService;
    private final TenantService tenantService;
    private final PaymentService paymentService;
    private final OwnerService ownerService;
    private final ReminderService reminderService;
    private final CurrentUserService currentUserService;

    public HomeController(PropertyService propertyService,
                          TenantService tenantService,
                          PaymentService paymentService,
                          OwnerService ownerService,
                          ReminderService reminderService,
                          CurrentUserService currentUserService) {
        this.propertyService = propertyService;
        this.tenantService = tenantService;
        this.paymentService = paymentService;
        this.ownerService = ownerService;
        this.reminderService = reminderService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        boolean admin = currentUserService.isAdmin();
        model.addAttribute("isAdmin", admin);

        if (admin) {
            model.addAttribute("ownerCount", ownerService.count());
            model.addAttribute("propertyCount", propertyService.count());
            model.addAttribute("tenantCount", tenantService.count());
            model.addAttribute("totalCollected", paymentService.totalCollected());
            model.addAttribute("reminderCount", reminderService.count());
            model.addAttribute("recentPayments", paymentService.findAll());
        } else {
            Optional<Long> ownerId = currentUserService.currentOwnerId();
            model.addAttribute("ownerCount", 0L);
            model.addAttribute("propertyCount", ownerId.map(propertyService::countByOwner).orElse(0L));
            model.addAttribute("tenantCount", ownerId.map(tenantService::countByOwner).orElse(0L));
            model.addAttribute("totalCollected",
                    ownerId.map(paymentService::totalCollectedByOwner).orElse(BigDecimal.ZERO));
            model.addAttribute("reminderCount",
                    ownerId.map(id -> (long) reminderService.findByOwner(id).size()).orElse(0L));
            model.addAttribute("recentPayments",
                    ownerId.map(paymentService::findByOwner).orElse(List.of()));
        }
        return "dashboard";
    }
}
