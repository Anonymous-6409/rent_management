package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.model.Property;
import com.moneytree.rentmanagement.model.PropertyStatus;
import com.moneytree.rentmanagement.service.OwnerService;
import com.moneytree.rentmanagement.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final OwnerService ownerService;

    public PropertyController(PropertyService propertyService, OwnerService ownerService) {
        this.propertyService = propertyService;
        this.ownerService = ownerService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("properties", propertyService.findAll());
        return "properties/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("property", new Property());
        model.addAttribute("statuses", PropertyStatus.values());
        model.addAttribute("owners", ownerService.findAll());
        return "properties/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("property", propertyService.findById(id));
        model.addAttribute("statuses", PropertyStatus.values());
        model.addAttribute("owners", ownerService.findAll());
        return "properties/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("property") Property property,
                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", PropertyStatus.values());
            model.addAttribute("owners", ownerService.findAll());
            return "properties/form";
        }
        propertyService.save(property);
        return "redirect:/properties";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        propertyService.deleteById(id);
        return "redirect:/properties";
    }
}
