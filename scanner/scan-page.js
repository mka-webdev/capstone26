const fs = require("fs");
const path = require("path");
const { chromium } = require("playwright");
const AxeBuilder = require("@axe-core/playwright").default;

async function run() {
  const url = process.argv[2];
  const outputPath =
    process.argv[3] || path.join(__dirname, "..", "results.json");

  if (!url) {
    console.error("Missing URL argument.");
    process.exit(1);
  }

  let browser;
  let context;

  try {
    browser = await chromium.launch({ headless: false });
    context = await browser.newContext();
    const page = await context.newPage();

    await page.goto(url, {
      waitUntil: "domcontentloaded",
      timeout: 30000
    });

    const axeResults = await new AxeBuilder({ page }).analyze();

    const result = {
      url: page.url(),
      timestamp: new Date().toISOString(),
      testEngine: {
        name: "axe-core",
        version: axeResults.testEngine?.version || ""
      },
      testRunner: {
        name: "playwright"
      },
      violations: axeResults.violations || []
    };

    fs.writeFileSync(outputPath, JSON.stringify(result, null, 2), "utf-8");
    console.log(`Scan completed. Output written to ${outputPath}`);
    process.exit(0);
  } catch (error) {
    console.error("Scanner failed:", error.message);
    process.exit(2);
  } finally {
    if (context) {
      await context.close();
    }
    if (browser) {
      await browser.close();
    }
  }
}

run();
