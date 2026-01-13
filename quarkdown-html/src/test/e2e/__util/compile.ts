import {execSync} from "child_process";
import * as path from "path";
import {OUTPUT_DIR, PROJECT_ROOT} from "./paths";

const CLI_PATH = path.join(PROJECT_ROOT, "quarkdown-cli/build/install/quarkdown-cli/bin/quarkdown-cli");

/**
 * Compiles a Quarkdown source file to HTML.
 * Uses the pre-built CLI from installDist.
 * @param source - Path to the source .qd file
 * @param outName - Name for the output directory
 * @returns Path to the compiled output directory
 */
export function compile(source: string, outName: string): string {
    const sourcePath = path.isAbsolute(source) ? source : path.join(PROJECT_ROOT, source);

    execSync(`"${CLI_PATH}" compile "${sourcePath}" --out "${OUTPUT_DIR}" --out-name "${outName}"`, {
        cwd: PROJECT_ROOT,
        stdio: "pipe",
    });

    return path.join(OUTPUT_DIR, outName);
}
