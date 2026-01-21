import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("paragraph style applies to list items", async (page) => {
    const listItems = page.locator("li");
    const paragraphs = page.locator("p");

    await expect(listItems).toHaveCount(3);
    await expect(paragraphs).toHaveCount(2);

    // Get expected line-height from first list item (computed as px)
    const firstItemLineHeight = await listItems.first().evaluate((el) =>
        getComputedStyle(el).lineHeight
    );

    // List items have correct styling
    for (const item of await listItems.all()) {
        await expect(item).toHaveCSS("line-height", firstItemLineHeight);
        await expect(item).toHaveCSS("letter-spacing", /-0\.\d+px/);
        await expect(item).toHaveCSS("margin-top", "0px");
    }

    // Paragraphs have same styling as list items
    for (const p of await paragraphs.all()) {
        await expect(p).toHaveCSS("line-height", firstItemLineHeight);
        await expect(p).toHaveCSS("letter-spacing", /-0\.\d+px/);
        await expect(p).toHaveCSS("margin-top", "0px");
    }
});

test("elements are contiguous with no gaps", async (page) => {
    const list = page.locator("ul");
    const paragraphs = page.locator("p");

    const listBox = await list.boundingBox();
    const firstParagraphBox = await paragraphs.nth(0).boundingBox();
    const secondParagraphBox = await paragraphs.nth(1).boundingBox();

    expect(listBox).not.toBeNull();
    expect(firstParagraphBox).not.toBeNull();
    expect(secondParagraphBox).not.toBeNull();

    // End of list should be start of first paragraph
    const listEndY = listBox!.y + listBox!.height;
    expect(listEndY).toBeCloseTo(firstParagraphBox!.y, 0);

    // End of first paragraph should be start of second paragraph
    const firstParagraphEndY = firstParagraphBox!.y + firstParagraphBox!.height;
    expect(firstParagraphEndY).toBeCloseTo(secondParagraphBox!.y, 0);
});
