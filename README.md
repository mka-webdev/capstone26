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

## AI-assisted remediation

OAGP is being extended to include AI-based remediation guidance.

The system will:
- Generate remediation suggestions for each accessibility issue
- Use structured scan data (rule, impact, WCAG tags, element type)
- Produce short, actionable guidance (description, impact, recommendation)

Users will be able to select an AI provider before running a scan:
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

## Export features

- Export scan results to PDF 


## Scanner module setup

After pulling the branch, run the following commands once to install the scanner dependencies locally:

```bash
cd scanner
npm install
npx playwright install
```
 
## Logging configuration

The application uses Spring Boot's logging (Logback under the hood) and is configured via `src/main/resources/application.properties`.

Key settings you can change:

- `logging.level.root` — global default logging level (TRACE, DEBUG, INFO, WARN, ERROR)
- `logging.level.<package or class>` — per-package or per-class override (for example `logging.level.com.oagp=DEBUG`)
- `logging.file.name` — (optional) write logs to a file instead of only the console

Default values in this project (see `application.properties`):

```
logging.level.root=INFO
logging.level.com.oagp=INFO
```

What the levels mean (short):

- TRACE — very detailed diagnostic information
- DEBUG — useful development-time information
- INFO — runtime events of interest (start/stop/major actions)
- WARN — something unexpected happened, but the application can continue
- ERROR — a serious failure; typically an exception that stops a request or process

## Environment Variables

This application requires several environment variables to be configured for AI provider integration. These variables are used to securely supply API keys and configuration without hardcoding sensitive data.

### Required Variables

| Variable Name | Description | Required |
|--------------|------------|----------|
| `GEMINI_API_KEY_FREE` | API key for the free-tier Gemini project | Yes (if using Gemini free tier) |
| `GEMINI_API_KEY_PAID` | API key for the paid Gemini project | Yes (if using Gemini paid tier) |
| `OPENAI_API_KEY` | API key for OpenAI | Yes (if using OpenAI) |

### Example Setup

#### macOS / Linux
```bash
export GEMINI_API_KEY_FREE=your_free_key
export GEMINI_API_KEY_PAID=your_paid_key
export OPENAI_API_KEY=your_openai_key
```
#### Windows (PowerShell)
```powershell
$env:GEMINI_API_KEY_FREE="your_free_key"
$env:GEMINI_API_KEY_PAID="your_paid_key"
$env:OPENAI_API_KEY="your_openai_key"
```

## Run with Docker

The project is packaged as a Docker image and published to Docker Hub at `turner747/capstone26`.

### Quick Start: Pull and Run from Docker Hub (Recommended)

Supply environment variables at runtime when pulling and running the image — do not rely on a repository `.env` file.

#### Using docker run (Linux / macOS)

```bash
docker run --rm -p 8080:8080 \
  -e GEMINI_API_KEY_FREE=your_free_key \
  -e GEMINI_API_KEY_PAID=your_paid_key \
  -e OPENAI_API_KEY=your_openai_key \
  turner747/capstone26:latest
```

#### Using docker run (Windows / PowerShell)

```powershell
docker run --rm -p 8080:8080 `
  -e GEMINI_API_KEY_FREE="your_free_key" `
  -e GEMINI_API_KEY_PAID="your_paid_key" `
  -e OPENAI_API_KEY="your_openai_key" `
  turner747/capstone26:latest
```

#### Required Environment Variables

| Variable | Purpose |
|----------|---------|
| `GEMINI_API_KEY_FREE` | Free-tier Gemini API key (required if using Gemini free tier) |
| `GEMINI_API_KEY_PAID` | Paid Gemini API key (optional; required if using Gemini paid tier) |
| `OPENAI_API_KEY` | OpenAI API key (optional; required if using OpenAI) |

### Local Development: Build and Run with Docker Compose

For developers contributing to this project:

1. Clone the repository and cd into it
2. Create a local `.env` file and fill in your API keys (`.env` is gitignored):

   ```
   GEMINI_API_KEY_FREE=your_free_key
   GEMINI_API_KEY_PAID=your_paid_key
   OPENAI_API_KEY=your_openai_key
   ```

3. Build and start:

   ```bash
   docker-compose up -d --build
   ```

4. Stop and clean up:

   ```bash
   docker-compose down
   ```

The `docker-compose.yml` uses a named volume `oagp-data` to persist the SQLite database between restarts.

### CI/CD: Automated Docker Hub Publishing

This repository includes a GitHub Actions workflow (`.github/workflows/docker-publish.yml`) that automatically builds and publishes the image to Docker Hub whenever you publish a GitHub Release.

**Setup:**
1. Add these repository secrets to GitHub (Settings → Secrets and variables → Actions):
   - `DOCKERHUB_USERNAME` — your Docker Hub username
   - `DOCKERHUB_TOKEN` — a Docker Hub personal access token or password

2. Create a release on GitHub:

   ```bash
   # with git and gh CLI
   git tag -a v1.0.0 -m "Release v1.0.0"
   git push origin v1.0.0
   gh release create v1.0.0
   ```

   The workflow will automatically build and push:
   - `turner747/capstone26:v1.0.0` (release-specific tag)
   - `turner747/capstone26:latest` (always updated with each release)

### Security Notes

- **Never commit real API keys or credentials to the repository.** The `.env` file in the repository is a template with placeholder values for local development only — it is gitignored and should never contain actual secrets.
- When users pull the image from Docker Hub, they must supply their own API keys at runtime via environment variables, not by editing the repository.
- Do not pass secrets in Dockerfile or build context; always inject them at runtime.
- For production deployments, consider using a secrets manager (AWS Secrets Manager, HashiCorp Vault, Kubernetes Secrets, etc.) instead of plain environment variables.

### Troubleshooting

- **Image not found**: Ensure you have pulled the latest image with `docker pull turner747/capstone26:latest`
- **Database not persisting**: Check that the volume `-v oagp-data:/data` is mounted
- **Scanner not working**: Ensure `PLAYWRIGHT_HEADLESS=true` in production. GUI rendering is not available in headless container environments.
- **Build fails locally**: Verify Docker is installed and running, and that `Dockerfile` builds successfully with `docker build -t oagp .`




