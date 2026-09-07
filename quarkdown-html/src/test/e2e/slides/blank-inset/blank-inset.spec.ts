import {evaluateComputedStyle, getComputedSizeProperty} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("blank slides receive the vertical inset padding", async (page) => {
    const sections = page.locator(".reveal .slides > section");
    await expect(sections).toHaveCount(2);

    // The slide with a heading is not affected.
    await expect(sections.nth(0)).toHaveCSS("padding-top", "0px");

    // The blank (headerless) slide is padded by the inset.
    const inset = await getComputedSizeProperty(page, "var(--qd-slides-blank-vertical-inset)");
    expect(inset).toBeGreaterThan(0);
    const style = await evaluateComputedStyle(sections.nth(1));
    expect(parseFloat(style.paddingTop)).toBeCloseTo(inset, 1);
});
