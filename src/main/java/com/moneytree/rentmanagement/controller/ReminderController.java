package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.model.Property;
import com.moneytree.rentmanagement.model.Reminder;
import com.moneytree.rentmanagement.security.CurrentUserService;
import com.moneytree.rentmanagement.service.ReminderService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/reminders")
public class ReminderController {

    private final ReminderService reminderService;
    private final CurrentUserService currentUserService;

    public ReminderController(ReminderService reminderService, CurrentUserService currentUserService) {
        this.reminderService = reminderService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String list(Model model) {
        if (currentUserService.isAdmin()) {
            model.addAttribute("reminders", reminderService.findAll());
        } else {
            model.addAttribute("reminders", currentUserService.currentOwnerId()
                    .map(reminderService::findByOwner).orElse(List.of()));
        }
        return "reminders/list";
    }

    @PostMapping("/generate")
    public String generate(RedirectAttributes redirectAttributes) {
        List<Reminder> created;
        if (currentUserService.isAdmin()) {
            created = reminderService.generateRemindersForCurrentPeriod();
        } else {
            Long ownerId = currentUserService.currentOwnerId()
                    .orElseThrow(() -> new AccessDeniedException("No owner profile for current user"));
            created = reminderService.generateRemindersForCurrentPeriod(ownerId);
        }
        redirectAttributes.addFlashAttribute("message",
                "Generated " + created.size() + " new reminder(s) for the current period.");
        return "redirect:/reminders";
    }

    @PostMapping("/send")
    public String send(RedirectAttributes redirectAttributes) {
        int sent;
        if (currentUserService.isAdmin()) {
            sent = reminderService.sendPendingReminders();
        } else {
            Long ownerId = currentUserService.currentOwnerId()
                    .orElseThrow(() -> new AccessDeniedException("No owner profile for current user"));
            sent = reminderService.sendPendingReminders(ownerId);
        }
        redirectAttributes.addFlashAttribute("message", "Sent " + sent + " pending reminder(s).");
        return "redirect:/reminders";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        if (!currentUserService.isAdmin()) {
            Reminder reminder = reminderService.findById(id);
            Property property = reminder.getProperty();
            if (property == null || !currentUserService.ownsOwner(property.getOwner())) {
                throw new AccessDeniedException("You do not have access to this reminder");
            }
        }
        reminderService.deleteById(id);
        return "redirect:/reminders";
    }
}
