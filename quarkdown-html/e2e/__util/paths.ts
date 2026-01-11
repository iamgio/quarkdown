import * as path from "path";

export const PROJECT_ROOT = path.resolve(__dirname, "../../..");
export const OUTPUT_DIR = path.join(PROJECT_ROOT, "build/e2e");
export const ENTRY_POINT = "main.qd";

export type DocumentType = "plain" | "slides" | "paged" | "docs";
