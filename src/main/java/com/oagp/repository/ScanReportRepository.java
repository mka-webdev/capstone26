package com.oagp.repository;

import com.oagp.model.ScanReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanReportRepository extends JpaRepository<ScanReport, Long> {

    Optional<ScanReport> findByScanId(Long scanId);
}