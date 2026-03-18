package com.example.oagpfirst.repository;

import com.example.oagpfirst.model.Scan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanRepository extends JpaRepository<Scan, Long> {

    Optional<Scan> findTopByOrderByIdDesc();
}
