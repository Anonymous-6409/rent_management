package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.model.Tenant;
import com.moneytree.rentmanagement.service.PropertyService;
import com.moneytree.rentmanagement.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tenants")
public class TenantController {

    private final TenantService tenantService;
    private final PropertyService propertyService;

    public TenantController(TenantService tenantService, PropertyService propertyService) {
        this.tenantService = tenantService;
        this.propertyService = propertyService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tenants", tenantService.findAll());
        return "tenants/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("tenant", new Tenant());
        model.addAttribute("properties", propertyService.findAll());
        return "tenants/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("tenant", tenantService.findById(id));
        model.addAttribute("properties", propertyService.findAll());
        return "tenants/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("tenant") Tenant tenant,
                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("properties", propertyService.findAll());
            return "tenants/form";
        }
        tenantService.save(tenant);
        return "redirect:/tenants";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        tenantService.deleteById(id);
        return "redirect:/tenants";
    }
}
