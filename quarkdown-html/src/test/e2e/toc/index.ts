import {expect, Page} from "@playwright/test";

export const EXPECTED_NUMBERS = ["1", "2", "2.1", "2.1.1", "3", "3.1", "3.2"];

export async function assertTocStructure(page: Page, itemCount: number = 7) {
    const nav = page.locator("nav");
    await expect(nav).toBeAttached();

    const items = nav.locator("li");
    await expect(items).toHaveCount(itemCount);

    return {nav, items};
}

export async function assertTocNumbering(
    page: Page,
    format: "latex" | "minimal",
    expectedNumbers: string[] = EXPECTED_NUMBERS
) {
    const nav = page.locator("nav");
    const items = nav.locator("li");

    for (let i = 0; i < expectedNumbers.length; i++) {
        const content = await items.nth(i).evaluate((el) => {
            return getComputedStyle(el, "::before").content;
        });
        // latex: '" 1 "', minimal: '" 1"'
        const expected = format === "latex"
            ? `" ${expectedNumbers[i]} "`
            : `" ${expectedNumbers[i]}"`;
        expect(content).toBe(expected);
    }
}

export async function assertTocLinks(page: Page, expectedTexts: string[] = EXPECTED_NUMBERS) {
    const nav = page.locator("nav");
    const links = nav.locator("a");

    await expect(links).toHaveCount(expectedTexts.length);

    for (let i = 0; i < expectedTexts.length; i++) {
        const link = links.nth(i);
        await expect(link).toHaveText(expectedTexts[i]);
        await expect(link).toHaveAttribute("href", `#_${expectedTexts[i].replace(/\./g, "")}`);
    }
}
