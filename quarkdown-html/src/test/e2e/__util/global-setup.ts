import {execSync, spawn} from "child_process";
import * as fs from "fs";
import * as path from "path";
import {OUTPUT_DIR, PROJECT_ROOT} from "./paths";

const SERVER_PORT = 8089;
const SERVER_STATE_FILE = path.join(OUTPUT_DIR, ".server-state.json");
const CLI_PATH = path.join(PROJECT_ROOT, "quarkdown-cli/build/install/quarkdown-cli/bin/quarkdown-cli");

export default async function globalSetup() {
    // Build CLI once
    console.log("Building Quarkdown CLI...");
    execSync("./gradlew :quarkdown-cli:installDist --quiet", {
        cwd: PROJECT_ROOT,
        stdio: "inherit",
    });

    // Ensure output directory exists
    fs.mkdirSync(OUTPUT_DIR, {recursive: true});

    // Spawn Quarkdown server using pre-built CLI
    const proc = spawn(CLI_PATH, ["start", "-f", OUTPUT_DIR, "-p", String(SERVER_PORT)], {
        detached: true,
        stdio: "ignore",
        cwd: PROJECT_ROOT,
    });
    proc.unref();

    // Store server info for tests and teardown
    const url = `http://localhost:${SERVER_PORT}`;
    fs.writeFileSync(SERVER_STATE_FILE, JSON.stringify({url, pid: proc.pid}));

    // Wait for server to be ready
    await waitForServer(url);

    console.log(`E2E server started at ${url} (pid: ${proc.pid})`);
}

async function waitForServer(url: string, timeout = 10000): Promise<void> {
    const start = Date.now();
    while (Date.now() - start < timeout) {
        try {
            const res = await fetch(url);
            if (res.status === 404 || res.ok) return; // 404 is fine, means server is up
        } catch {
            // Server not ready yet
        }
        await new Promise((r) => setTimeout(r, 100));
    }
    throw new Error(`Server not ready at ${url} after ${timeout}ms`);
}

export function getServerUrl(): string {
    const state = JSON.parse(fs.readFileSync(SERVER_STATE_FILE, "utf-8"));
    return state.url;
}
