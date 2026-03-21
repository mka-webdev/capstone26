# OAGP_FIRST

NetBeans-ready Spring Boot project with an integrated Node-based accessibility scanner.

## What this version does

- Accepts a page URL through the web interface
- Triggers a local Playwright + axe-core scan
- Writes structured scan output to `results.json`
- Stores each completed scan as a NEW scan record in SQLite
- Keeps all previous scans in the database
- Shows only the most recently imported scan at `http://localhost:8080/`

## Important behavior

- The application no longer depends on manually replacing `results.json` before startup
- A scan is now triggered from the app workflow after a URL is submitted
- The backend runs the scanner module locally using Node.js
- The generated JSON is processed and stored as a new scan record
- The page only displays the latest scan
- The database keeps the full history of all earlier scans

## Accepted input examples

The scanner currently supports inputs such as:

- `example.com`
- `www.example.com`
- `https://example.com`
- `localhost:8080`
- `127.0.0.1:8080`

This allows scanning of both hosted pages and local development pages.

## Scanner module setup

After pulling the branch, run the following commands once to install the scanner dependencies locally:

```bash
cd scanner
npm install
npx playwright install