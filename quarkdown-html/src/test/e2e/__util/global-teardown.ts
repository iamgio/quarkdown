import * as fs from "fs";
import * as path from "path";
import {OUTPUT_DIR} from "./paths";

const SERVER_STATE_FILE = path.join(OUTPUT_DIR, ".server-state.json");

export default async function globalTeardown() {
    try {
        const state = JSON.parse(fs.readFileSync(SERVER_STATE_FILE, "utf-8"));
        if (state.pid) {
            process.kill(state.pid);
            console.log(`E2E server stopped (pid: ${state.pid})`);
        }
        fs.unlinkSync(SERVER_STATE_FILE);
    } catch {
        // Server state file doesn't exist or process already stopped
    }
}
