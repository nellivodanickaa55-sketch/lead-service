package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.to}")
    private String to;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void notifyNewLead(Lead lead) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject("Новая заявка: " + lead.getName());
            msg.setText(
                "Имя: " + lead.getName() + "\n" +
                "Контакт: " + lead.getContact() + "\n" +
                "Сообщение: " + lead.getMessage() + "\n" +
                "externalId: " + lead.getExternalId() + "\n" +
                "Время: " + lead.getCreatedAt()
            );
            mailSender.send(msg);
            System.out.println(">>> Email: отправлено на " + to);
        } catch (Exception e) {
            System.out.println("!!! Email error: " + e.getClass().getSimpleName() + " — " + e.getMessage());
        }
    }
}