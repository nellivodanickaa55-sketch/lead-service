package com.example.demo;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.List;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadRepository repo;
    private final MailService mail;

    public LeadController(LeadRepository repo, MailService mail) {
        this.repo = repo;
        this.mail = mail;
    }

    @PostMapping
    public Lead create(@RequestBody Map<String, String> body) {
        String externalId = body.get("externalId");

        return repo.findByExternalId(externalId).orElseGet(() -> {
            Lead lead = new Lead();
            lead.setExternalId(externalId);
            lead.setName(body.get("name"));
            lead.setContact(body.get("contact"));
            lead.setMessage(body.get("message"));
            lead.setCreatedAt(LocalDateTime.now());
            Lead saved = repo.save(lead);
            new Thread(() -> mail.notifyNewLead(saved)).start();
            return saved;
        });
    }

  @GetMapping
    public List<Lead> all(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime since) {

        // Параметр не передан: отдаём все заявки
        if (since == null) {
            return repo.findAll();
        }
        // Параметр передан: только заявки после указанного времени
        return repo.findByCreatedAtAfter(since);
    }

    @GetMapping("/test")
    public Lead test() {
        String externalId = "test-" + System.currentTimeMillis();
        Lead lead = new Lead();
        lead.setExternalId(externalId);
        lead.setName("Ivan");
        lead.setContact("@ivan");
        lead.setMessage("Need site");
        lead.setCreatedAt(LocalDateTime.now());
        Lead saved = repo.save(lead);
        new Thread(() -> mail.notifyNewLead(saved)).start();
        return saved;
    }
}