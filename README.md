# OAGP - Open Accessibility Governance Platform 

OAGP is a local-first accessibility platform for scanning, storing, and reviewing web accessibility results.
It helps turn technical scan output into clear, usable information for analysis and reporting. 

The platform combines automated accessibility scanning (via axe-core) with optional AI-assisted remediation guidance,
allowing users to interpret issues and understand how to fix them.

Built with Spring Boot and a Node-based scanning module, OAGP uses the open-source [axe-core](https://www.npmjs.com/package/axe-core) engine developed by Deque Systems.

## What this version does

- Accepts a page URL through the web interface
- Triggers a local Playwright + axe-core scan
- Writes structured scan output to `results.json`
- Archives previous scan files before each new scan (stored in `scan-archive/`)
- Stores each completed scan as a NEW scan record in SQLite
- Keeps all previous scans in the database
- Shows only the most recently imported scan at `http://localhost:8080/`
- Prepares structured scan data for AI-based remediation (in progress)

## Important behavior

- The application no longer depends on manually replacing `results.json` before startup
- A scan is now triggered from the app workflow after a URL is submitted
- The backend runs the scanner module locally using Node.js
- The generated JSON is processed and stored as a new scan record
- The page only displays the latest scan
- The database keeps the full history of all earlier scans

## Scan file handling

- The latest scan is always written to `results.json` in the project root
- Before each new scan, the previous `results.json` is automatically archived
- Archived files are stored in `scan-archive/` with timestamped filenames

Example:
scan-archive/results_2026-04-09_12-34-56.json

This allows simple scan history tracking without affecting the main workflow.

## AI-assisted remediation (in progress)

OAGP is being extended to include AI-based remediation guidance.

The system will:
- Generate remediation suggestions for each accessibility issue
- Use structured scan data (rule, impact, WCAG tags, element type)
- Produce short, actionable guidance (description, impact, recommendation)

Users will be able to select an AI provider before running a scan.

Planned behavior:
- Default provider: Google Gemini (free-tier friendly)
- Optional provider: OpenAI (user-supplied API key stored locally on user's machine)
- The system selects the provider dynamically at runtime

AI generation is system-triggered after scan processing and does not require manual user input.

## Accepted input examples

The scanner currently accepts inputs such as:

- `example.com`
- `www.example.com`
- `https://example.com`
- `http://example.com`
- `localhost:8080`
- `127.0.0.1:8080`

The application normalises supported inputs before scanning. For example:

- `example.com` becomes `https://example.com`
- `www.example.com` becomes `https://www.example.com`
- `localhost:8080` becomes `http://localhost:8080`

## Basic input validation

Before the scanner runs, the application performs basic validation on the submitted address **and** checks if it is reachable.

It rejects clearly incomplete or invalid inputs such as:

- `cnn`
- `test`
- blank input

This helps prevent unnecessary scanner execution and avoids exposing raw processing errors for simple input mistakes.

## Planned features

- AI-generated remediation guidance per issue
- Export scan results to PDF reports
- Improved dashboard for scan history and comparison
- Optional AI provider configuration via UI

## Scanner module setup

After pulling the branch, run the following commands once to install the scanner dependencies locally:

```bash
cd scanner
npm install
npx playwright install
