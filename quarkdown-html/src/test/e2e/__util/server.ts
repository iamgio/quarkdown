import {ChildProcess, spawn} from "child_process";
import {PROJECT_ROOT} from "./paths";

/** Port counter for assigning unique ports to each test server. */
let portCounter = 8089;

/** Returns the next available port for a test server. */
export function nextPort(): number {
    return portCounter++;
}

/**
 * Starts a Quarkdown web server for a compiled output directory.
 * @param outputDir - Path to the compiled output directory
 * @param port - Port number to run the server on
 * @returns Server handle with process, url, and stop function
 */
export function startServer(outputDir: string, port: number): {process: ChildProcess; url: string; stop: () => void} {
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
export async function waitForServer(url: string, timeout = 10000): Promise<void> {
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
