import {expect, Page, test as base} from "@playwright/test";
import {ChildProcess, execSync, spawn} from "child_process";
import * as path from "path";

const PROJECT_ROOT = path.resolve(__dirname, "../..");
const OUTPUT_DIR = path.join(PROJECT_ROOT, "build/e2e");
const SERVER_PORT = 8089;
const ENTRY_POINT = "main.qd";

function compile(source: string, outName: string): string {
    const sourcePath = path.isAbsolute(source) ? source : path.join(PROJECT_ROOT, source);
    const args = `compile ${sourcePath} --out ${OUTPUT_DIR} --out-name ${outName}`;

    execSync(`./gradlew :quarkdown-cli:run --args="${args}" --quiet`, {
        cwd: PROJECT_ROOT,
        stdio: "pipe",
    });

    return path.join(OUTPUT_DIR, outName);
}

function startServer(outputDir: string): {process: ChildProcess; url: string; stop: () => void} {
    const args = `start -f ${outputDir} -p ${SERVER_PORT}`;

    const proc = spawn("./gradlew", [":quarkdown-cli:run", `--args=${args}`, "--quiet"], {
        cwd: PROJECT_ROOT,
        stdio: "pipe",
    });

    return {
        process: proc,
        url: `http://localhost:${SERVER_PORT}`,
        stop: () => proc.kill(),
    };
}

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
 * Compiles a document, starts a server, navigates to it, and runs custom test logic.
 *
 * @param testDir - The test directory (__dirname from the spec file)
 * @param page - Playwright page
 * @param fn - Custom test logic to run after setup
 */
export async function testDocument(
    testDir: string,
    page: Page,
    fn: (page: Page) => Promise<void>
): Promise<void> {
    const sourcePath = path.join(testDir, ENTRY_POINT);
    const outName = path.relative(__dirname, testDir).split(path.sep).join("-");
    const outputDir = compile(sourcePath, outName);
    const server = startServer(outputDir);

    try {
        await waitForServer(server.url);
        await page.goto(server.url);
        await expect(page.locator(".quarkdown")).toBeVisible();
        await page.waitForFunction(() => (window as any).isReady());
        await fn(page);
    } finally {
        server.stop();
    }
}

export {expect};
export const test = base;
