package com.oagp.repository;

import com.oagp.model.Scan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScanRepository extends JpaRepository<Scan, Long> {

    Optional<Scan> findTopByOrderByIdDesc();
}
