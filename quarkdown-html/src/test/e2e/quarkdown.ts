import {expect, Page, test as base} from "@playwright/test";
import {DocumentType, OUTPUT_DIR} from "./__util/paths";
import {runTest} from "./__util/runner";
import * as path from "path";

export type {DocumentType} from "./__util/paths";

/**
 * Returns the output directory path for a given test directory.
 */
export function outputDir(testDir: string): string {
    const e2eDir = path.resolve(__dirname);
    const outName = path.relative(e2eDir, testDir).split(path.sep).join("-");
    return path.join(OUTPUT_DIR, outName);
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
