package com.example.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class LeadScheduler {

    private final LeadRepository repo;

    public LeadScheduler(LeadRepository repo) {
        this.repo = repo;
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 10_000)
    public void reportNewLeads() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        List<Lead> recent = repo.findByCreatedAtAfter(since);

        if (recent.isEmpty()) {
            System.out.println("[scheduler] Новых заявок нет");
        } else {
            System.out.println("[scheduler] За последние 5 минут: " + recent.size() + " новых заявок");
            recent.forEach(lead ->
                    System.out.println("  — " + lead.getName() + " / " + lead.getContact())
            );
        }
    }
}