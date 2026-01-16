import {expect, Page} from "@playwright/test";

export type CaptionPosition = "top" | "bottom";

async function assertCaptionPosition(
    contentBox: {y: number; height: number},
    captionBox: {y: number; height: number},
    position: CaptionPosition
) {
    if (position === "bottom") {
        expect(captionBox.y).toBeGreaterThan(contentBox.y + contentBox.height - 1);
    } else {
        expect(captionBox.y + captionBox.height).toBeLessThanOrEqual(contentBox.y + 1);
    }
}

export async function assertFigureCaption(
    page: Page,
    contentSelector: string,
    position: CaptionPosition
) {
    const figure = page.locator(`figure:has(${contentSelector})`);
    await expect(figure).toBeAttached();

    const content = figure.locator(contentSelector);
    const figcaption = figure.locator("figcaption");

    await expect(content).toBeAttached();
    await expect(figcaption).toBeAttached();
    await expect(figcaption).toHaveText("Caption");

    const contentBox = await content.boundingBox();
    const captionBox = await figcaption.boundingBox();
    expect(contentBox).not.toBeNull();
    expect(captionBox).not.toBeNull();
    await assertCaptionPosition(contentBox!, captionBox!, position);
}

export async function assertTableCaption(page: Page, position: CaptionPosition) {
    const table = page.locator("table");
    await expect(table).toBeAttached();

    const caption = table.locator("caption");
    await expect(caption).toBeAttached();
    await expect(caption).toHaveText("Caption");

    const tbody = table.locator("tbody");
    const tbodyBox = await tbody.boundingBox();
    const captionBox = await caption.boundingBox();
    expect(tbodyBox).not.toBeNull();
    expect(captionBox).not.toBeNull();
    await assertCaptionPosition(tbodyBox!, captionBox!, position);
}
