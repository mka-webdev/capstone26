package com.oagp.controller;

import com.oagp.dto.ApiResponse;
import com.oagp.dto.ScanRequestDto;
import com.oagp.dto.ScanResponseDto;
import com.oagp.service.ScanApiService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 * REST API controller for full scan endpoints.
 *
 * Supports:
 * - create scan (runs a real scan)
 * - read scans
 * - update scan metadata
 * - delete scan
 */
@RestController
@RequestMapping("/api/scans")
public class ScanApiController {

    private final ScanApiService scanApiService;

    public ScanApiController(ScanApiService scanApiService) {
        this.scanApiService = scanApiService;
    }

    /*
     * ========================================================================
     * GET ALL SCANS (WITH OPTIONAL FILTERING)
     * ========================================================================
     *
     * Endpoint:
     *      GET /api/scans
     *
     * Optional query parameters:
     *      ?url=...
     *      ?auditName=...
     *      ?time=...
     *
     * Purpose:
     * - Retrieve all scans from the system
     * - Optionally filter results by:
     *      - page URL
     *      - audit name
     *      - timestamp (partial match)
     *
     * Example requests:
     *      GET /api/scans
     *      GET /api/scans?url=example.com
     *      GET /api/scans?auditName=Homepage
     *
     * Returns:
     * - List of ScanResponseDto objects wrapped in ApiResponse
     * - HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ScanResponseDto>>> getAllScans(
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String auditName,
            @RequestParam(required = false) String time) {

        List<ScanResponseDto> scans = scanApiService.getAllScans(url, auditName, time);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Scans retrieved successfully.", scans)
        );
    }

    /*
     * ========================================================================
     * GET LATEST SCAN
     * ========================================================================
     *
     * Endpoint:
     *      GET /api/scans/latest
     *
     * Purpose:
     * - Retrieve the most recently created scan
     *
     * Returns:
     * - ScanResponseDto if found
     * - 404 if no scans exist
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<ScanResponseDto>> getLatestScan() {

        ScanResponseDto latestScan = scanApiService.getLatestScan();

        if (latestScan == null) {
            return ResponseEntity.status(404).body(
                    new ApiResponse<>(false, "No scans found.", null)
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Latest scan retrieved successfully.", latestScan)
        );
    }

    /*
     * ========================================================================
     * GET SCAN BY ID
     * ========================================================================
     *
     * Endpoint:
     *      GET /api/scans/{id}
     *
     * Purpose:
     * - Retrieve a single scan using its unique ID
     *
     * Path Variable:
     *      id -> the scan's primary key
     *
     * Returns:
     * - ScanResponseDto if found
     * - 404 if scan does not exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScanResponseDto>> getScanById(
            @PathVariable("id") Long id) {

        ScanResponseDto scan = scanApiService.getScanById(id);

        if (scan == null) {
            return ResponseEntity.status(404).body(
                    new ApiResponse<>(false, "Scan not found.", null)
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Scan retrieved successfully.", scan)
        );
    }

    /*
     * ========================================================================
     * CREATE SCAN
     * ========================================================================
     *
     * Endpoint:
     *      POST /api/scans
     *
     * Purpose:
     * - Create a new scan by running the external scanner process
     *
     * Input:
     * - ScanRequestDto containing:
     *      - auditName
     *      - url
     *
     * Process:
     * - Validate input
     * - Normalize URL
     * - Run scanner 
     * - Process JSON results
     * - Save full scan to database
     *
     * Returns:
     * - Full ScanResponseDto on success
     * - 400 for invalid input
     * - 500 for system errors
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ScanResponseDto>> createScan(
            @RequestBody ScanRequestDto requestDto) {

        try {
            ScanResponseDto createdScan = scanApiService.createScan(requestDto);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Scan created successfully.", createdScan)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null)
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ApiResponse<>(false, "Failed to create scan.", null)
            );
        }
    }

    /*
     * ========================================================================
     * UPDATE SCAN (METADATA ONLY)
     * ========================================================================
     *
     * Endpoint:
     *      PUT /api/scans/{id}
     *
     * Purpose:
     * - Update editable scan fields:
     *      - auditName
     *      - pageUrl
     *
     * Important:
     * - Does NOT re-run scan
     * - Does NOT modify violations or nodes
     *
     * Returns:
     * - Updated ScanResponseDto
     * - 404 if scan not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ScanResponseDto>> updateScan(
            @PathVariable("id") Long id,
            @RequestBody ScanRequestDto requestDto) {

        try {
            ScanResponseDto updatedScan = scanApiService.updateScan(id, requestDto);

            if (updatedScan == null) {
                return ResponseEntity.status(404).body(
                        new ApiResponse<>(false, "Scan not found.", null)
                );
            }

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Scan updated successfully.", updatedScan)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null)
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ApiResponse<>(false, "Failed to update scan.", null)
            );
        }
    }

    /*
     * ========================================================================
     * DELETE SCAN
     * ========================================================================
     *
     * Endpoint:
     *      DELETE /api/scans/{id}
     *
     * Purpose:
     * - Remove a scan and all associated data from the system
     *
     * Process:
     * - Delete linked report (if exists)
     * - Delete scan entity
     * - Cascade removes violations and nodes
     *
     * Returns:
     * - success message if deleted
     * - 404 if scan not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteScan(
            @PathVariable("id") Long id) {

        try {
            boolean deleted = scanApiService.deleteScan(id);

            if (!deleted) {
                return ResponseEntity.status(404).body(
                        new ApiResponse<>(false, "Scan not found.", null)
                );
            }

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Scan deleted successfully.", null)
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ApiResponse<>(false, "Failed to delete scan.", null)
            );
        }
    }
}