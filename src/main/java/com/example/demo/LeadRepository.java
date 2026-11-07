package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    Optional<Lead> findByExternalId(String externalId);

    List<Lead> findByCreatedAtAfter(LocalDateTime since);
}