package com.example.oagpfirst.repository;

import com.example.oagpfirst.model.Violation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViolationRepository extends JpaRepository<Violation, Long> {
}
