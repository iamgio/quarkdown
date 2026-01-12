import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

async function getCaptionBeforeContent(page: any, figcaption: any): Promise<string> {
    return figcaption.evaluate((el: Element) => getComputedStyle(el, "::before").content);
}

test("displays numbered captions", async (page) => {
    const figures = page.locator("figure");
    await expect(figures).toHaveCount(2);

    // First code block: numbering 1 displayed via ::before
    const caption1 = figures.nth(0).locator("figcaption");
    expect(await getCaptionBeforeContent(page, caption1)).toContain("1");

    // Second code block: numbering 2, with caption text
    const caption2 = figures.nth(1).locator("figcaption");
    expect(await getCaptionBeforeContent(page, caption2)).toContain("2");
    await expect(caption2).toContainText("A print statement");
});
