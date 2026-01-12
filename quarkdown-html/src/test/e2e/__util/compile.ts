import {execSync} from "child_process";
import * as path from "path";
import {OUTPUT_DIR, PROJECT_ROOT} from "./paths";

/**
 * Compiles a Quarkdown source file to HTML.
 * @param source - Path to the source .qd file
 * @param outName - Name for the output directory
 * @returns Path to the compiled output directory
 */
export function compile(source: string, outName: string): string {
    const sourcePath = path.isAbsolute(source) ? source : path.join(PROJECT_ROOT, source);
    const args = `compile ${sourcePath} --out ${OUTPUT_DIR} --out-name ${outName}`;

    execSync(`./gradlew :quarkdown-cli:run --args="${args}" --quiet`, {
        cwd: PROJECT_ROOT,
        stdio: "pipe",
    });

    return path.join(OUTPUT_DIR, outName);
}
