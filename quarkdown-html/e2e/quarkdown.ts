import {expect, Page, test as base} from "@playwright/test";
import {ChildProcess, execSync, spawn} from "child_process";
import * as fs from "fs";
import * as path from "path";

const PROJECT_ROOT = path.resolve(__dirname, "../..");
const OUTPUT_DIR = path.join(PROJECT_ROOT, "build/e2e");
const ENTRY_POINT = "main.qd";

export type DocumentType = "plain" | "slides" | "paged" | "docs";

/** Port counter for assigning unique ports to each test server. */
let portCounter = 8089;

/** Returns the next available port for a test server. */
function nextPort(): number {
    return portCounter++;
}

/**
 * Compiles a Quarkdown source file to HTML.
 * @param source - Path to the source .qd file
 * @param outName - Name for the output directory
 * @returns Path to the compiled output directory
 */
function compile(source: string, outName: string): string {
    const sourcePath = path.isAbsolute(source) ? source : path.join(PROJECT_ROOT, source);
    const args = `compile ${sourcePath} --out ${OUTPUT_DIR} --out-name ${outName}`;

    execSync(`./gradlew :quarkdown-cli:run --args="${args}" --quiet`, {
        cwd: PROJECT_ROOT,
        stdio: "pipe",
    });

    return path.join(OUTPUT_DIR, outName);
}

/**
 * Starts a Quarkdown web server for a compiled output directory.
 * @param outputDir - Path to the compiled output directory
 * @param port - Port number to run the server on
 * @returns Server handle with process, url, and stop function
 */
function startServer(outputDir: string, port: number): {process: ChildProcess; url: string; stop: () => void} {
    const args = `start -f ${outputDir} -p ${port}`;

    const proc = spawn("./gradlew", [":quarkdown-cli:run", `--args=${args}`, "--quiet"], {
        cwd: PROJECT_ROOT,
        stdio: "pipe",
    });

    return {
        process: proc,
        url: `http://localhost:${port}`,
        stop: () => proc.kill(),
    };
}

/**
 * Waits for a server to be ready by polling the URL.
 * @param url - URL to poll
 * @param timeout - Maximum time to wait in milliseconds
 * @throws Error if server is not ready within the timeout
 */
async function waitForServer(url: string, timeout = 10000): Promise<void> {
    const start = Date.now();
    while (Date.now() - start < timeout) {
        try {
            const res = await fetch(url);
            if (res.ok) return;
        } catch {
            // Server not ready yet
        }
        await new Promise((r) => setTimeout(r, 100));
    }
    throw new Error(`Server not ready at ${url} after ${timeout}ms`);
}

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
async function runTest(
    testDir: string,
    page: Page,
    fn: (page: Page) => Promise<void>,
    docType?: DocumentType
): Promise<void> {
    const baseOutName = path.relative(__dirname, testDir).split(path.sep).join("-");
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

/**
 * Creates a test suite for a specific directory.
 * @param testDir - The test directory (__dirname from the spec file)
 * @returns Suite object with test utilities
 */
export function suite(testDir: string) {
    return {
        /**
         * Defines a single test case.
         * @param name - Test name
         * @param fn - Test function receiving the Playwright page
         */
        test: (name: string, fn: (page: Page) => Promise<void>) => {
            base(name, async ({page}) => runTest(testDir, page, fn));
        },
        /**
         * Runs the same test across multiple document types.
         * Creates separate test cases for each document type.
         * @param name - Test name (document type will be appended in brackets)
         * @param docTypes - Array of document types to test against
         * @param fn - Test function receiving page and current document type
         */
        testMatrix: (
            name: string,
            docTypes: DocumentType[],
            fn: (page: Page, docType: DocumentType) => Promise<void>
        ) => {
            for (const docType of docTypes) {
                base(`${name} [${docType}]`, async ({page}) =>
                    runTest(testDir, page, (p) => fn(p, docType), docType)
                );
            }
        },
        /** Playwright expect function for assertions */
        expect,
    };
}
