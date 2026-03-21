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

## Scanner module setup

After pulling the branch, run the following commands once to install the scanner dependencies locally:

```bash
cd scanner
npm install
npx playwright install
