import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("document contains 5 mermaid diagrams", async (page) => {
    const diagrams = page.locator(".mermaid");
    await expect(diagrams).toHaveCount(5);
});

test("diagrams are served from cache on reload", async (page) => {
    const svgBefore = page.locator(".mermaid svg").nth(0);
    const boxBefore = await svgBefore.boundingBox();
    expect(boxBefore).not.toBeNull();
    expect(boxBefore!.width).toBeGreaterThan(0);
    expect(boxBefore!.height).toBeGreaterThan(0);

    await page.reload();
    await page.waitForFunction(() => (window as any).isReady());

    const diagrams = page.locator(".mermaid");
    await expect(diagrams).toHaveCount(5);

    for (let i = 0; i < 5; i++) {
        await expect(diagrams.nth(i)).toHaveAttribute("data-from-cache", "true");
    }

    // All cached diagrams have the same bounding box.
    const svgsAfter = page.locator(".mermaid svg");
    await expect(svgsAfter).toHaveCount(5);

    const firstBox = await svgsAfter.nth(0).boundingBox();
    expect(firstBox).not.toBeNull();
    expect(firstBox!.width).toBeGreaterThan(0);
    expect(firstBox!.height).toBeGreaterThan(0);

    for (let i = 1; i < 5; i++) {
        const box = await svgsAfter.nth(i).boundingBox();
        expect(box).not.toBeNull();
        expect(box!.width).toBeCloseTo(firstBox!.width, 0);
        expect(box!.height).toBeCloseTo(firstBox!.height, 0);
    }
});
