import {expect, Page} from "@playwright/test";
import * as fs from "fs";
import * as path from "path";
import {compile} from "./compile";
import {getServerUrl} from "./global-setup";
import {DocumentType, ENTRY_POINT} from "./paths";

/** Generates a unique ID for parallel test isolation. */
function uniqueId(): string {
    return `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}

/**
 * Creates a temporary source file with a document type prepended.
 * Uses a unique ID to avoid conflicts in parallel execution.
 * @param testDir - Directory containing the main.qd file
 * @param docType - Document type to prepend
 * @param id - Unique identifier for this test run
 * @returns Path to the temporary source file
 */
function createSourceWithDocType(testDir: string, docType: DocumentType, id: string): string {
    const sourcePath = path.join(testDir, ENTRY_POINT);
    const content = fs.readFileSync(sourcePath, "utf-8");
    const withDocType = `.doctype {${docType}}\n\n${content}`;

    const tempPath = path.join(testDir, `main-${docType}-${id}.qd`);
    fs.writeFileSync(tempPath, withDocType);
    return tempPath;
}

/**
 * Runs a test by compiling a document and navigating to it via the persistent server.
 * Waits for the document to be fully rendered before running the test function.
 * @param testDir - Directory containing the test's main.qd file
 * @param page - Playwright page instance
 * @param fn - Test function to execute
 * @param docType - Optional document type to prepend to the source
 */
export async function runTest(
    testDir: string,
    page: Page,
    fn: (page: Page) => Promise<void>,
    docType?: DocumentType
): Promise<void> {
    const e2eDir = path.resolve(__dirname, "..");
    const baseOutName = path.relative(e2eDir, testDir).split(path.sep).join("-");
    const id = uniqueId();
    let sourcePath: string;
    let outName: string;

    if (docType) {
        sourcePath = createSourceWithDocType(testDir, docType, id);
        outName = `${baseOutName}-${docType}-${id}`;
    } else {
        sourcePath = path.join(testDir, ENTRY_POINT);
        outName = baseOutName;
    }

    try {
        compile(sourcePath, outName);

        const url = `${getServerUrl()}/${outName}/`;
        await page.goto(url);
        await waitForReady(page);
        await fn(page);
    } finally {
        if (docType) {
            fs.unlinkSync(sourcePath);
        }
    }
}

/**
 * Waits for the Quarkdown document to be fully rendered.
 */
async function waitForReady(page: Page): Promise<void> {
    await expect(page.locator(".quarkdown")).toBeVisible();
    await page.waitForFunction(() => (window as any).isReady());
}
