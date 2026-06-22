package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.model.Owner;
import com.moneytree.rentmanagement.model.Property;
import com.moneytree.rentmanagement.model.PropertyStatus;
import com.moneytree.rentmanagement.security.CurrentUserService;
import com.moneytree.rentmanagement.service.OwnerService;
import com.moneytree.rentmanagement.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final OwnerService ownerService;
    private final CurrentUserService currentUserService;

    public PropertyController(PropertyService propertyService, OwnerService ownerService,
                              CurrentUserService currentUserService) {
        this.propertyService = propertyService;
        this.ownerService = ownerService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String list(Model model) {
        if (currentUserService.isAdmin()) {
            model.addAttribute("properties", propertyService.findAll());
        } else {
            model.addAttribute("properties", currentUserService.currentOwnerId()
                    .map(propertyService::findByOwner).orElse(List.of()));
        }
        return "properties/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("property", new Property());
        populateFormModel(model);
        return "properties/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Property property = propertyService.findById(id);
        assertCanModify(property);
        model.addAttribute("property", property);
        populateFormModel(model);
        return "properties/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("property") Property property,
                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            populateFormModel(model);
            return "properties/form";
        }
        if (currentUserService.isAdmin()) {
            propertyService.save(property);
        } else {
            Owner me = currentUserService.currentOwner()
                    .orElseThrow(() -> new AccessDeniedException("No owner profile for current user"));
            if (property.getId() != null) {
                assertCanModify(propertyService.findById(property.getId()));
            }
            property.setOwner(me); // owners can only ever own their own properties
            propertyService.save(property);
        }
        return "redirect:/properties";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        if (!currentUserService.isAdmin()) {
            assertCanModify(propertyService.findById(id));
        }
        propertyService.deleteById(id);
        return "redirect:/properties";
    }

    private void populateFormModel(Model model) {
        model.addAttribute("statuses", PropertyStatus.values());
        boolean admin = currentUserService.isAdmin();
        model.addAttribute("isAdmin", admin);
        if (admin) {
            model.addAttribute("owners", ownerService.findAll());
        }
    }

    private void assertCanModify(Property property) {
        if (currentUserService.isAdmin()) {
            return;
        }
        if (!currentUserService.ownsOwner(property.getOwner())) {
            throw new AccessDeniedException("You do not have access to this property");
        }
    }
}
