import {getComputedColor} from "../../__util/css";
import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix("appends numbered markers to marked lines and lists their descriptions", ["plain", "paged", "slides"], async (page) => {
    const code = page.locator("code[data-callouts]");
    await expect(code).toBeVisible();

    // A marker is appended at the end of each marked line, numbered progressively.
    await expect(code.locator(".code-callout-marker")).toHaveCount(2);
    await expect(code.locator('.hljs-ln-code[data-line-number="4"] .code-callout-marker')).toHaveText("1");
    await expect(code.locator('.hljs-ln-code[data-line-number="8"] .code-callout-marker')).toHaveText("2");

    // The description list below the code block mirrors the marker numbering.
    const callouts = page.locator("ul.code-callouts .code-callout");
    await expect(callouts).toHaveCount(2);
    await expect(callouts.nth(0).locator(".code-callout-marker")).toHaveText("1");
    await expect(callouts.nth(0)).toContainText("The constructor.");
    await expect(callouts.nth(1).locator(".code-callout-marker")).toHaveText("2");
    await expect(callouts.nth(1)).toContainText("Getter for the wrapped value.");

    // The list is visually attached to its code block.
    await expect(page.locator("pre:has(+ ul.code-callouts)")).toHaveCSS("margin-bottom", "0px");

    // Both the in-code markers and the list markers share the same theme-based styling.
    const mainColor = await getComputedColor(page, "var(--qd-main-color)");
    const backgroundColor = await getComputedColor(page, "var(--qd-background-color)");
    for (const marker of [
        code.locator(".code-callout-marker").first(),
        callouts.locator(".code-callout-marker").first(),
    ]) {
        await expect(marker).toHaveCSS("color", mainColor);
        await expect(marker).toHaveCSS("background-color", backgroundColor);
    }
});
