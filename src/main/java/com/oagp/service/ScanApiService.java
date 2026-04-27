package com.oagp.service;

import com.oagp.dto.ScanRequestDto;
import com.oagp.dto.ScanResponseDto;
import com.oagp.dto.ViolationNodeResponseDto;
import com.oagp.dto.ViolationResponseDto;
import com.oagp.model.Scan;
import com.oagp.model.Violation;
import com.oagp.model.ViolationNode;
import com.oagp.repository.ScanReportRepository;
import com.oagp.repository.ScanRepository;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * API service for full scan endpoints.
 *
 * This service:
 * - retrieves full scan data
 * - creates a real scan using the scanner process
 * - updates scan metadata
 * - deletes scans
 * - converts entities into DTOs
 */
@Service
public class ScanApiService {

    private final ScanRepository scanRepository;
    private final ScanReportRepository scanReportRepository;
    private final ScanService scanService;
    private final ScannerProcessService scannerProcessService;

    private static final DateTimeFormatter TIME_FILTER_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ScanApiService(ScanRepository scanRepository,
            ScanReportRepository scanReportRepository,
            ScanService scanService,
            ScannerProcessService scannerProcessService) {
        this.scanRepository = scanRepository;
        this.scanReportRepository = scanReportRepository;
        this.scanService = scanService;
        this.scannerProcessService = scannerProcessService;
    }

    /*
 * ========================================================================
 * GET ALL SCANS (WITH OPTIONAL FILTERING)
 * ========================================================================
 *
 * This method retrieves all scan records from the database and optionally
 * filters them based on the provided parameters.
 *
 * Parameters:
 * - url: filters scans whose page URL contains this value
 * - auditName: filters scans whose audit name contains this value
 * - time: filters scans based on a formatted timestamp match
 *
 * Process:
 * 1. Normalize all input parameters (convert to lowercase and trim)
 * 2. Retrieve all scans from the repository
 * 3. Apply filtering using helper methods:
 *      - matchesUrl()
 *      - matchesAuditName()
 *      - matchesTime()
 * 4. Sort results by scan timestamp (latest first)
 * 5. Convert each Scan entity into a ScanResponseDto
 *
 * Returns:
 * - A list of ScanResponseDto objects representing filtered scan results
     */
    public List<ScanResponseDto> getAllScans(String url, String auditName, String time) {

        String normalizedUrl = normalize(url);
        String normalizedAuditName = normalize(auditName);
        String normalizedTime = normalize(time);

        return scanRepository.findAll().stream()
                .filter(scan -> matchesUrl(scan, normalizedUrl))
                .filter(scan -> matchesAuditName(scan, normalizedAuditName))
                .filter(scan -> matchesTime(scan, normalizedTime))
                .sorted(Comparator.comparing(
                        Scan::getScanTimestamp,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .map(this::mapToScanResponseDto)
                .collect(Collectors.toList());
    }

    /*
 * ========================================================================
 * GET LATEST SCAN
 * ========================================================================
 *
 * This method retrieves the most recently created scan from the database.
 *
 * Process:
 * 1. Calls repository method findTopByOrderByIdDesc()
 *    which returns the scan with the highest ID (latest)
 * 2. Converts the Scan entity into a ScanResponseDto
 *
 * Returns:
 * - ScanResponseDto representing the latest scan
 * - null if no scans exist in the database
     */
    public ScanResponseDto getLatestScan() {
        return scanRepository.findTopByOrderByIdDesc()
                .map(this::mapToScanResponseDto)
                .orElse(null);
    }

    /*
 * ========================================================================
 * GET SCAN BY ID
 * ========================================================================
 *
 * This method retrieves a single scan based on its unique ID.
 *
 * Parameters:
 * - id: the primary key of the scan
 *
 * Process:
 * 1. Query the repository using findById()
 * 2. If the scan exists:
 *      - map it to ScanResponseDto
 * 3. If not found:
 *      - return null
 *
 * Returns:
 * - ScanResponseDto if found
 * - null if no scan exists with the given ID
     */
    public ScanResponseDto getScanById(Long id) {
        return scanRepository.findById(id)
                .map(this::mapToScanResponseDto)
                .orElse(null);
    }

    /*
 * ========================================================================
 * CREATE SCAN 
 * ========================================================================
 *
 * This method creates a new scan by executing the external scanning process.
 *
 * Parameters:
 * - requestDto: contains user input (auditName and URL)
 *
 * Process:
 * 1. Validate the request input (ensure fields are present)
 * 2. Normalize the URL (ensure it includes http/https)
 * 3. Validate the URL structure
 * 4. Run the scanner process
 *      -> generates JSON results
 * 5. Pass JSON results to ScanService for processing
 *      -> converts JSON into Scan, Violation, and Node entities
 * 6. Save the full scan to the database
 * 7. Convert saved entity into ScanResponseDto
 *
 * Returns:
 * - ScanResponseDto containing full scan results
 *
 * Throws:
 * - IllegalArgumentException for invalid input
 * - IOException / InterruptedException for scanner issues
     */
    public ScanResponseDto createScan(ScanRequestDto requestDto)
            throws IOException, InterruptedException {

        validateRequest(requestDto);

        String normalizedUrl = normalizeUrl(requestDto.getUrl());
        validateUrl(normalizedUrl);

        Path jsonPath = scannerProcessService.runScan(normalizedUrl);
        Scan savedScan = scanService.processScannedJson(jsonPath, requestDto.getAuditName().trim());

        return mapToScanResponseDto(savedScan);
    }

    /*
 * ========================================================================
 * UPDATE SCAN (METADATA ONLY)
 * ========================================================================
 *
 * This method updates only user-editable fields of a scan.
 *
 * Parameters:
 * - id: scan ID to update
 * - requestDto: contains new auditName and URL
 *
 * Important:
 * - Does NOT re-run the scan
 * - Does NOT modify violations or nodes
 *
 * Process:
 * 1. Validate request input
 * 2. Retrieve scan by ID
 * 3. If not found -> return null
 * 4. Update:
 *      - auditName
 *      - pageUrl
 * 5. Save updated scan to database
 * 6. Convert to ScanResponseDto
 *
 * Returns:
 * - Updated ScanResponseDto
 * - null if scan not found
     */
    public ScanResponseDto updateScan(Long id, ScanRequestDto requestDto) {

        validateRequest(requestDto);

        Scan scan = scanRepository.findById(id).orElse(null);

        if (scan == null) {
            return null;
        }

        scan.setAuditName(requestDto.getAuditName().trim());
        scan.setPageUrl(requestDto.getUrl().trim());

        Scan updatedScan = scanRepository.save(scan);

        return mapToScanResponseDto(updatedScan);
    }

    /*
 * ========================================================================
 * DELETE SCAN
 * ========================================================================
 *
 * This method deletes a scan and its associated data.
 *
 * Parameters:
 * - id: ID of the scan to delete
 *
 * Process:
 * 1. Check if scan exists
 * 2. If not -> return false
 * 3. Delete associated scan report
 * 4. Delete scan entity
 * 5. Cascade removes related violations and nodes
 *
 * Transactional:
 * - Ensures all delete operations succeed or fail together
 *
 * Returns:
 * - true if deleted successfully
 * - false if scan not found
     */
    @Transactional
    public boolean deleteScan(Long id) {

        if (!scanRepository.existsById(id)) {
            return false;
        }

        scanReportRepository.deleteByScanId(id);
        scanRepository.deleteById(id);
        return true;
    }

    /*
 * ========================================================================
 * VALIDATE REQUEST
 * ========================================================================
 *
 * This method checks that the incoming request object contains the minimum
 * required data needed to create or update a scan.
 *
 * Parameters:
 * - requestDto: the incoming API request object
 *
 * Validation rules:
 * 1. The request body itself must not be null
 * 2. The audit name must be provided
 * 3. The URL must be provided
 *
 * If any validation rule fails, an IllegalArgumentException is thrown.
 *
 * Purpose:
 * - Prevent invalid or incomplete data from reaching the business logic
 * - Ensure the API only processes valid requests
     */
    private void validateRequest(ScanRequestDto requestDto) {

        if (requestDto == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        if (requestDto.getAuditName() == null || requestDto.getAuditName().isBlank()) {
            throw new IllegalArgumentException("Audit name is required.");
        }

        if (requestDto.getUrl() == null || requestDto.getUrl().isBlank()) {
            throw new IllegalArgumentException("URL is required.");
        }
    }

    /*
 * ========================================================================
 * VALIDATE URL FORMAT
 * ========================================================================
 *
 * This method checks whether a URL is structurally valid and usable for scanning.
 *
 * Parameters:
 * - url: the normalized URL string to validate
 *
 * Validation rules:
 * 1. URL must have a scheme of http or https
 * 2. URL must contain a non-empty host
 * 3. Host must be valid:
 *      - standard domain name (contains a dot)
 *      - localhost
 *      - IPv4 address
 *
 * Process:
 * - The method attempts to parse the URL using java.net.URI
 * - If parsing fails or required parts are missing, an exception is thrown
 *
 * Purpose:
 * - Prevent scanner failures caused by malformed URLs
 * - Ensure only reachable and correctly structured addresses are accepted
     */
    private void validateUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme == null
                    || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("URL must start with http or https.");
            }

            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("Invalid URL.");
            }

            if (!isValidHost(host)) {
                throw new IllegalArgumentException(
                        "Enter a valid website address, e.g. example.com, www.example.com, or localhost:8080."
                );
            }

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL.");
        }
    }

    /*
 * ========================================================================
 * NORMALIZE URL
 * ========================================================================
 *
 * This method converts user-entered URL text into a consistent format
 * before validation and scanning.
 *
 * Parameters:
 * - url: raw URL input from the user
 *
 * Process:
 * 1. Remove leading and trailing spaces
 * 2. Check for blank input
 * 3. If URL already starts with http:// or https://, leave it unchanged
 * 4. If URL starts with localhost or 127.0.0.1, add http://
 * 5. If URL starts with www., add https://
 * 6. Otherwise, default to https://
 *
 * Examples:
 * - "example.com"      -> "https://example.com"
 * - "www.test.com"     -> "https://www.test.com"
 * - "localhost:8080"   -> "http://localhost:8080"
 *
 * Purpose:
 * - Make user input flexible
 * - Ensure URLs are in a valid format before validation
     */
    private String normalizeUrl(String url) {
        String trimmedUrl = url.trim();

        if (trimmedUrl.isBlank()) {
            throw new IllegalArgumentException("URL is required.");
        }

        if (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")) {
            return trimmedUrl;
        }

        if (trimmedUrl.startsWith("localhost") || trimmedUrl.startsWith("127.0.0.1")) {
            return "http://" + trimmedUrl;
        }

        if (trimmedUrl.startsWith("www.")) {
            return "https://" + trimmedUrl;
        }

        return "https://" + trimmedUrl;
    }

    /*
 * ========================================================================
 * CHECK IF HOST IS VALID
 * ========================================================================
 *
 * This method checks whether the host part of a URL looks acceptable.
 *
 * Parameters:
 * - host: the host name extracted from the URI
 *
 * Accepted host formats:
 * - localhost
 * - IPv4 address
 * - domain name containing a dot
 *
 * Returns:
 * - true if host looks valid
 * - false otherwise
 *
 * Purpose:
 * - Provide an extra validation layer after URI parsing
 * - Help reject incomplete or invalid host names
     */
    private boolean isValidHost(String host) {
        if (host.equalsIgnoreCase("localhost")) {
            return true;
        }
        if (host.matches("^\\d{1,3}(\\.\\d{1,3}){3}$")) {
            return true;
        }
        return host.contains(".");
    }

    /*
 * ========================================================================
 * NORMALIZE FILTER TEXT
 * ========================================================================
 *
 * This method prepares filter values for case-insensitive comparison.
 *
 * Parameters:
 * - value: raw filter text entered by the user
 *
 * Process:
 * 1. If null or blank, return null
 * 2. Otherwise trim spaces and convert to lowercase
 *
 * Returns:
 * - normalized string for comparison
 * - null if no usable value was supplied
 *
 * Purpose:
 * - Make filtering logic simpler and consistent
 * - Allow case-insensitive matching across URL, audit name, and time
     */
    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    /*
 * ========================================================================
 * CHECK URL FILTER MATCH
 * ========================================================================
 *
 * This method checks whether a scan matches the supplied URL filter.
 *
 * Parameters:
 * - scan: scan being tested
 * - url: normalized URL filter text
 *
 * Logic:
 * - If the filter is null, return true (no filtering applied)
 * - Otherwise, return true only if the scan's pageUrl contains the filter text
 *
 * Purpose:
 * - Support optional filtering of scans by page URL
     */
    private boolean matchesUrl(Scan scan, String url) {
        if (url == null) {
            return true;
        }
        return scan.getPageUrl() != null
                && scan.getPageUrl().toLowerCase(Locale.ROOT).contains(url);
    }

    /*
 * ========================================================================
 * CHECK AUDIT NAME FILTER MATCH
 * ========================================================================
 *
 * This method checks whether a scan matches the supplied audit name filter.
 *
 * Parameters:
 * - scan: scan being tested
 * - auditName: normalized audit name filter text
 *
 * Logic:
 * - If the filter is null, return true
 * - Otherwise, return true only if the scan's audit name contains the filter text
 *
 * Purpose:
 * - Support optional filtering of scans by audit name
     */
    private boolean matchesAuditName(Scan scan, String auditName) {
        if (auditName == null) {
            return true;
        }
        return scan.getAuditName() != null
                && scan.getAuditName().toLowerCase(Locale.ROOT).contains(auditName);
    }

    /*
 * ========================================================================
 * CHECK TIME FILTER MATCH
 * ========================================================================
 *
 * This method checks whether a scan matches the supplied time filter.
 *
 * Parameters:
 * - scan: scan being tested
 * - time: normalized time filter text
 *
 * Process:
 * 1. If filter is null, return true
 * 2. Format the scan timestamp using TIME_FILTER_FORMATTER
 * 3. Convert formatted text to lowercase
 * 4. Check whether it contains the filter text
 *
 * Example:
 * - stored timestamp: 2026-04-14 15:30
 * - filter text: 2026-04-14
 * - result: true
 *
 * Purpose:
 * - Support flexible text-based filtering by date/time
     */
    private boolean matchesTime(Scan scan, String time) {
        if (time == null) {
            return true;
        }
        return scan.getScanTimestamp() != null
                && scan.getScanTimestamp()
                        .format(TIME_FILTER_FORMATTER)
                        .toLowerCase(Locale.ROOT)
                        .contains(time);
    }

    /*
 * ========================================================================
 * MAP SCAN ENTITY TO SCAN RESPONSE DTO
 * ========================================================================
 *
 * This method converts a Scan entity into a ScanResponseDto.
 *
 * Parameters:
 * - scan: the database entity to convert
 *
 * Process:
 * 1. Copy basic scan fields:
 *      - id
 *      - auditName
 *      - pageUrl
 *      - scanTimestamp
 *      - timeZone
 * 2. Convert the list of Violation entities into ViolationResponseDto objects
 * 3. If violations are null, return an empty list instead
 *
 * Returns:
 * - fully populated ScanResponseDto
 *
 * Purpose:
 * - Separate internal database structure from API output structure
 * - Ensure frontend receives clean, controlled data
     */
    private ScanResponseDto mapToScanResponseDto(Scan scan) {
        return new ScanResponseDto(
                scan.getId(),
                scan.getAuditName(),
                scan.getPageUrl(),
                scan.getScanTimestamp(),
                scan.getTimeZone(),
                scan.getViolations() == null
                ? List.of()
                : scan.getViolations().stream()
                        .filter(Objects::nonNull)
                        .map(this::mapToViolationResponseDto)
                        .collect(Collectors.toList())
        );
    }

    /*
 * ========================================================================
 * MAP VIOLATION ENTITY TO VIOLATION RESPONSE DTO
 * ========================================================================
 *
 * This method converts a Violation entity into a ViolationResponseDto.
 *
 * Parameters:
 * - violation: the Violation entity to convert
 *
 * Process:
 * 1. Copy all basic violation fields:
 *      - id
 *      - ruleId
 *      - impact
 *      - description
 *      - help
 *      - tags
 *      - helpUrl
 *      - instanceCount
 *      - remediation
 * 2. Convert each ViolationNode entity into a ViolationNodeResponseDto
 * 3. If node list is null, return an empty list
 *
 * Returns:
 * - ViolationResponseDto representing one violation
 *
 * Purpose:
 * - Prepare violation data for API output
 * - Prevent exposing entity objects directly to the frontend
     */
    private ViolationResponseDto mapToViolationResponseDto(Violation violation) {
        return new ViolationResponseDto(
                violation.getId(),
                violation.getRuleId(),
                violation.getImpact(),
                violation.getDescription(),
                violation.getHelp(),
                violation.getTags(),
                violation.getHelpUrl(),
                violation.getInstanceCount(),
                violation.getRemediation(),
                violation.getNodes() == null
                ? List.of()
                : violation.getNodes().stream()
                        .filter(Objects::nonNull)
                        .map(this::mapToViolationNodeResponseDto)
                        .collect(Collectors.toList())
        );
    }

    /*
 * ========================================================================
 * MAP VIOLATION NODE ENTITY TO VIOLATION NODE RESPONSE DTO
 * ========================================================================
 *
 * This method converts a ViolationNode entity into a ViolationNodeResponseDto.
 *
 * Parameters:
 * - node: the ViolationNode entity to convert
 *
 * Process:
 * - Copy the node fields directly:
 *      - id
 *      - message
 *      - html
 *      - elementType
 *
 * Returns:
 * - ViolationNodeResponseDto representing one affected element
 *
 * Purpose:
 * - Provide clean frontend-ready node data
 * - Keep API output independent from persistence entities
     */
    private ViolationNodeResponseDto mapToViolationNodeResponseDto(ViolationNode node) {
        return new ViolationNodeResponseDto(
                node.getId(),
                node.getMessage(),
                node.getHtml(),
                node.getElementType()
        );
    }
}
