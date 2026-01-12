import {expect, Page} from "@playwright/test";
import * as fs from "fs";
import * as path from "path";
import {compile} from "./compile";
import {DocumentType, ENTRY_POINT} from "./paths";
import {nextPort, startServer, waitForServer} from "./server";

/**
 * Creates a temporary source file with a document type prepended.
 * @param testDir - Directory containing the main.qd file
 * @param docType - Document type to prepend
 * @returns Path to the temporary source file
 */
function createSourceWithDocType(testDir: string, docType: DocumentType): string {
    const sourcePath = path.join(testDir, ENTRY_POINT);
    const content = fs.readFileSync(sourcePath, "utf-8");
    const withDocType = `.doctype {${docType}}\n\n${content}`;

    const tempPath = path.join(testDir, `main-${docType}.qd`);
    fs.writeFileSync(tempPath, withDocType);
    return tempPath;
}

/**
 * Runs a test by compiling a document, starting a server, and executing assertions.
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
    let sourcePath: string;
    let outName: string;

    if (docType) {
        sourcePath = createSourceWithDocType(testDir, docType);
        outName = `${baseOutName}-${docType}`;
    } else {
        sourcePath = path.join(testDir, ENTRY_POINT);
        outName = baseOutName;
    }

    const outputDir = compile(sourcePath, outName);
    const port = nextPort();
    const server = startServer(outputDir, port);

    try {
        await waitForServer(server.url);
        await page.goto(server.url);
        await expect(page.locator(".quarkdown")).toBeVisible();
        await page.waitForFunction(() => (window as any).isReady());
        await fn(page);
    } finally {
        server.stop();
        if (docType) {
            fs.unlinkSync(sourcePath);
        }
    }
}
