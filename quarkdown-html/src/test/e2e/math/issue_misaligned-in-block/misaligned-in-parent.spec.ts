import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("math in quote and outside have same rendering", async (page) => {
    const mathInQuote = page.locator("blockquote formula");
    const mathInBox = page.locator(".box formula");
    const mathOutside = page.locator("formula:not(blockquote formula, .box formula)");

    await expect(mathInQuote).toBeAttached();
    await expect(mathInBox).toBeAttached();
    await expect(mathOutside).toBeAttached();

    // Both should have the same inner structure
    const quoteChildren = await mathInQuote.locator(".katex-html *").all();
    const boxChildren = await mathInBox.locator(".katex-html *").all();
    const outsideChildren = await mathOutside.locator(".katex-html *").all();
    expect(quoteChildren.length).toBe(outsideChildren.length);
    expect(boxChildren.length).toBe(outsideChildren.length);

    let quoteOffset: number | null = null;
    let boxOffset: number | null = null;

    // Ensure relative offsets are consistent
    for (let i = 0; i < quoteChildren.length; i++) {
        const quoteBox = await quoteChildren[i].boundingBox();
        const boxBox = await boxChildren[i].boundingBox();
        const outsideBox = await outsideChildren[i].boundingBox();

        expect(quoteBox).not.toBeNull();
        expect(boxBox).not.toBeNull();
        expect(outsideBox).not.toBeNull();

        if (quoteOffset === null) {
            quoteOffset = quoteBox!.x - outsideBox!.x;
        } else {
            expect(quoteBox!.x - outsideBox!.x).toBeCloseTo(quoteOffset, 0);
        }

        if (boxOffset === null) {
            boxOffset = boxBox!.x - outsideBox!.x;
        } else {
            expect(boxBox!.x - outsideBox!.x).toBeCloseTo(boxOffset, 0);
        }

        expect(quoteBox!.height).toBeCloseTo(outsideBox!.height, 0);
    }
});
