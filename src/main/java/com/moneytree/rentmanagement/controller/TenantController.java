package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.model.Property;
import com.moneytree.rentmanagement.model.Tenant;
import com.moneytree.rentmanagement.security.CurrentUserService;
import com.moneytree.rentmanagement.service.PropertyService;
import com.moneytree.rentmanagement.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/tenants")
public class TenantController {

    private final TenantService tenantService;
    private final PropertyService propertyService;
    private final CurrentUserService currentUserService;

    public TenantController(TenantService tenantService, PropertyService propertyService,
                            CurrentUserService currentUserService) {
        this.tenantService = tenantService;
        this.propertyService = propertyService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String list(Model model) {
        if (currentUserService.isAdmin()) {
            model.addAttribute("tenants", tenantService.findAll());
        } else {
            model.addAttribute("tenants", currentUserService.currentOwnerId()
                    .map(tenantService::findByOwner).orElse(List.of()));
        }
        return "tenants/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("tenant", new Tenant());
        model.addAttribute("properties", visibleProperties());
        return "tenants/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Tenant tenant = tenantService.findById(id);
        assertCanModify(tenant);
        model.addAttribute("tenant", tenant);
        model.addAttribute("properties", visibleProperties());
        return "tenants/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("tenant") Tenant tenant,
                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("properties", visibleProperties());
            return "tenants/form";
        }
        if (!currentUserService.isAdmin()) {
            // The chosen property must belong to the owner, and an existing tenant must already be theirs.
            assertOwnsProperty(tenant.getProperty());
            if (tenant.getId() != null) {
                assertCanModify(tenantService.findById(tenant.getId()));
            }
        }
        tenantService.save(tenant);
        return "redirect:/tenants";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        if (!currentUserService.isAdmin()) {
            assertCanModify(tenantService.findById(id));
        }
        tenantService.deleteById(id);
        return "redirect:/tenants";
    }

    private List<Property> visibleProperties() {
        if (currentUserService.isAdmin()) {
            return propertyService.findAll();
        }
        return currentUserService.currentOwnerId().map(propertyService::findByOwner).orElse(List.of());
    }

    private void assertCanModify(Tenant tenant) {
        if (currentUserService.isAdmin()) {
            return;
        }
        assertOwnsProperty(tenant.getProperty());
    }

    private void assertOwnsProperty(Property property) {
        if (currentUserService.isAdmin()) {
            return;
        }
        if (property == null || !currentUserService.ownsOwner(property.getOwner())) {
            throw new AccessDeniedException("You do not have access to this property");
        }
    }
}
