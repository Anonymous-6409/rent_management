package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.model.Owner;
import com.moneytree.rentmanagement.service.OwnerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/owners")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
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
        return "owners/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("owner") Owner owner, BindingResult result) {
        if (result.hasErrors()) {
            return "owners/form";
        }
        ownerService.save(owner);
        return "redirect:/owners";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        ownerService.deleteById(id);
        return "redirect:/owners";
    }
}
