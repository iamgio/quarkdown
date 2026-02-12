import {ChildProcess, spawn} from "child_process";
import {expect, FrameLocator, Page} from "@playwright/test";
import * as fs from "fs";
import * as os from "os";
import * as path from "path";
import {CLI_PATH} from "./compile";
import {findFreePort} from "./free-port";
import {DocumentType, OUTPUT_DIR} from "./paths";

/**
 * Context passed to a live preview test function.
 */
export interface LivePreviewContext {
    /** The Playwright page instance. */
    page: Page;

    /**
     * A FrameLocator targeting the currently visible iframe.
     * Re-evaluates on each access, so it automatically follows iframe swaps.
     */
    activeFrame: FrameLocator;

    /**
     * Writes content to a file in the temp working directory.
     * For main.qd, automatically prepends the doctype declaration if one was specified.
     * @param relativePath - Path relative to the temp working directory (e.g., "main.qd", "sub.qd")
     * @param content - The new file content
     */
    editFile: (relativePath: string, content: string) => void;
}

export interface LivePreviewTestOptions {
    /** Document type to prepend to main.qd (e.g., "plain", "paged", "slides", "docs"). */
    docType?: DocumentType;

    /**
     * Subpath to navigate to within the live preview (e.g., "sub/index.html").
     * Defaults to "index.html".
     */
    subpath?: string;
}

/**
 * Polls a URL until it responds, with a timeout.
 */
async function waitForServer(url: string, timeout: number): Promise<void> {
    const start = Date.now();
    while (Date.now() - start < timeout) {
        try {
            const res = await fetch(url);
            if (res.ok || res.status === 404) return;
        } catch {
            // Server not ready yet.
        }
        await new Promise((r) => setTimeout(r, 250));
    }
    throw new Error(`Live preview server not ready at ${url} after ${timeout}ms`);
}

/**
 * Waits for the visible iframe's content to be fully rendered:
 * the `.quarkdown` element must be visible and `isReady()` must return true.
 */
async function waitForIframeReady(page: Page, timeout: number): Promise<void> {
    const deadline = Date.now() + timeout;

    // Wait for a visible iframe to exist.
    const visibleIframe = page.locator("iframe.visible");
    await expect(visibleIframe).toBeVisible({timeout});

    // Use frameLocator for assertions inside the iframe.
    const frameLocator = page.frameLocator("iframe.visible");

    // Wait for .quarkdown to be visible inside the iframe.
    const remaining = Math.max(deadline - Date.now(), 1000);
    await expect(frameLocator.locator(".quarkdown")).toBeVisible({timeout: remaining});

    // Wait for isReady() to return true inside the iframe.
    const iframeElement = await visibleIframe.elementHandle({timeout: 5000});
    if (!iframeElement) throw new Error("Could not get iframe element handle");
    const contentFrame = await iframeElement.contentFrame();
    if (!contentFrame) throw new Error("Could not get iframe content frame");

    const readyRemaining = Math.max(deadline - Date.now(), 1000);
    await contentFrame.waitForFunction(() => (window as any).isReady(), null, {timeout: readyRemaining});
}

/**
 * Runs a live preview e2e test. Handles the full lifecycle:
 * 1. Copies test source files to a temp directory
 * 2. Finds a free port and spawns the CLI with `-p -w`
 * 3. Waits for the server and navigates to the live preview
 * 4. Waits for iframe readiness
 * 5. Calls the test function
 * 6. Cleans up (kills process, removes temp dir)
 *
 * @param testDir - Directory containing the test's .qd source files
 * @param page - Playwright page instance
 * @param fn - Test function receiving a LivePreviewContext
 * @param options - Optional configuration (docType, subpath)
 */
export async function runLivePreviewTest(
    testDir: string,
    page: Page,
    fn: (ctx: LivePreviewContext) => Promise<void>,
    options?: LivePreviewTestOptions,
): Promise<void> {
    const {docType, subpath} = options ?? {};

    // Create a temp working directory and copy all .qd files into it.
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "qd-live-preview-"));
    const qdFiles = fs.readdirSync(testDir).filter((f) => f.endsWith(".qd"));
    for (const file of qdFiles) {
        fs.copyFileSync(path.join(testDir, file), path.join(tmpDir, file));
    }

    // Prepend doctype to main.qd if specified.
    if (docType) {
        const mainPath = path.join(tmpDir, "main.qd");
        const content = fs.readFileSync(mainPath, "utf-8");
        const docTypeValue = docType === "slides-print" ? "slides" : docType;
        fs.writeFileSync(mainPath, `.doctype {${docTypeValue}}\n\n${content}`);
    }

    const port = await findFreePort();
    const outName = `live-preview-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;

    let proc: ChildProcess | null = null;

    try {
        // Spawn the CLI with compile + preview + watch flags.
        proc = spawn(
            CLI_PATH,
            [
                "compile",
                path.join(tmpDir, "main.qd"),
                "--out", OUTPUT_DIR,
                "--out-name", outName,
                "-p", "-w",
                "--server-port", String(port),
                "-b", "none",
            ],
            {
                detached: true,
                stdio: "pipe",
            },
        );

        // Log stderr for debugging.
        proc.stderr?.on("data", (data: Buffer) => {
            const msg = data.toString().trim();
            if (msg) console.error(`[live-preview stderr] ${msg}`);
        });

        // Wait for the server to be ready (generous timeout for first compilation).
        await waitForServer(`http://localhost:${port}`, 30000);

        // Navigate to the live preview page.
        // The compile command starts the server rooted at the output subdirectory,
        // so the /live/ path is relative to that root (no outName prefix needed).
        const liveSubpath = subpath ?? "index.html";
        const liveUrl = `http://localhost:${port}/live/${liveSubpath}`;
        await page.goto(liveUrl);

        // Wait for the iframe content to be ready.
        await waitForIframeReady(page, 30000);

        // Build the context for the test function.
        const ctx: LivePreviewContext = {
            page,
            get activeFrame() {
                return page.frameLocator("iframe.visible");
            },
            editFile(relativePath: string, content: string) {
                let finalContent = content;
                if (relativePath === "main.qd" && docType) {
                    const docTypeValue = docType === "slides-print" ? "slides" : docType;
                    finalContent = `.doctype {${docTypeValue}}\n\n${content}`;
                }
                fs.writeFileSync(path.join(tmpDir, relativePath), finalContent);
            },
        };

        await fn(ctx);
    } finally {
        // Kill the CLI process and its children.
        if (proc && proc.pid) {
            try {
                process.kill(-proc.pid, "SIGTERM");
            } catch {
                try {
                    proc.kill("SIGTERM");
                } catch {
                    // Process already exited.
                }
            }
        }

        // Clean up the temp directory.
        try {
            fs.rmSync(tmpDir, {recursive: true, force: true});
        } catch {
            // Best-effort cleanup.
        }
    }
}
