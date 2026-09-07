import {evaluateComputedStyle, getComputedSizeProperty} from "../../__util/css";
import {suite} from "../../quarkdown";

const {testMatrix, expect} = suite(__dirname);

testMatrix("pdf slides apply the horizontal inset padding", ["slides-print"], async (page) => {
    const pdfPage = page.locator(".slides > .pdf-page").first();
    const section = pdfPage.locator("section");

    const inset = await getComputedSizeProperty(page, "var(--qd-slides-horizontal-inset)");
    expect(inset).toBeGreaterThan(0);

    const style = await evaluateComputedStyle(section);
    expect(parseFloat(style.paddingLeft)).toBeCloseTo(inset, 1);
    expect(parseFloat(style.paddingRight)).toBeCloseTo(inset, 1);

    // The content is offset from the section edge by the inset.
    const sectionBox = (await section.boundingBox())!;
    const paragraphBox = (await section.locator("p").boundingBox())!;
    expect(paragraphBox.x - sectionBox.x).toBeCloseTo(inset, 0);
});
