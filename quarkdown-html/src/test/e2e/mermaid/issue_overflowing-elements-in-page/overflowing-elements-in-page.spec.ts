import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

function getPageContent(page: import("@playwright/test").Page, index: number) {
    return page.locator(".pagedjs_page").nth(index);
}

test("document has exactly three pages", async (page) => {
    const pages = page.locator(".pagedjs_page");
    await expect(pages).toHaveCount(3);
});

test("first page contains mermaid diagram and paragraphs 1, 2, 3", async (page) => {
    const content = getPageContent(page, 0);

    await expect(content.locator("figure")).toHaveCount(1);

    // Select only paragraphs that are outside the mermaid figure.
    const paragraphs = content.locator("p:not(figure p)");
    await expect(paragraphs.nth(0)).toHaveText("1");
    await expect(paragraphs.nth(1)).toHaveText("2");
    await expect(paragraphs.nth(2)).toHaveText("3");
});

test("second page contains mermaid diagram and paragraphs 4, 5, 6", async (page) => {
    const content = getPageContent(page, 1);

    await expect(content.locator("figure")).toHaveCount(1);

    const paragraphs = content.locator("p:not(figure p)");
    await expect(paragraphs.nth(0)).toHaveText("4");
    await expect(paragraphs.nth(1)).toHaveText("5");
    await expect(paragraphs.nth(2)).toHaveText("6");
});

test("both mermaid diagrams have the same SVG size", async (page) => {
    const svg1 = getPageContent(page, 0).locator("figure svg");
    const svg2 = getPageContent(page, 1).locator("figure svg");

    const box1 = await svg1.boundingBox();
    const box2 = await svg2.boundingBox();

    expect(box1).not.toBeNull();
    expect(box2).not.toBeNull();
    expect(box1!.width).toBeCloseTo(box2!.width, -1);
    expect(box1!.height).toBeCloseTo(box2!.height, -1);
});

test("third page contains paragraphs 1, 2, 3, 4, 5, 6 and no mermaid", async (page) => {
    const content = getPageContent(page, 2);

    await expect(content.locator("figure")).toHaveCount(0);

    const paragraphs = content.locator("p");
    await expect(paragraphs.nth(0)).toHaveText("1");
    await expect(paragraphs.nth(1)).toHaveText("2");
    await expect(paragraphs.nth(2)).toHaveText("3");
    await expect(paragraphs.nth(3)).toHaveText("4");
    await expect(paragraphs.nth(4)).toHaveText("5");
    await expect(paragraphs.nth(5)).toHaveText("6");
});
