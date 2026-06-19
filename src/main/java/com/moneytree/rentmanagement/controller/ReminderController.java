package com.moneytree.rentmanagement.controller;

import com.moneytree.rentmanagement.model.Reminder;
import com.moneytree.rentmanagement.service.ReminderService;
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

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("reminders", reminderService.findAll());
        return "reminders/list";
    }

    @PostMapping("/generate")
    public String generate(RedirectAttributes redirectAttributes) {
        List<Reminder> created = reminderService.generateRemindersForCurrentPeriod();
        redirectAttributes.addFlashAttribute("message",
                "Generated " + created.size() + " new reminder(s) for the current period.");
        return "redirect:/reminders";
    }

    @PostMapping("/send")
    public String send(RedirectAttributes redirectAttributes) {
        int sent = reminderService.sendPendingReminders();
        redirectAttributes.addFlashAttribute("message", "Sent " + sent + " pending reminder(s).");
        return "redirect:/reminders";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        reminderService.deleteById(id);
        return "redirect:/reminders";
    }
}
