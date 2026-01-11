import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

async function getFullCaptionText(locator: any): Promise<string> {
    return locator.evaluate((el: Element) => {
        const before = getComputedStyle(el, "::before").content.replace(/^"|"$/g, "");
        return before + el.textContent;
    });
}

test("displays localized numbering labels", async (page) => {
    const figure = page.locator("figure[id^='figure-'] figcaption");
    expect(await getFullCaptionText(figure)).toEqual("Figure 1: Fig");

    const table = page.locator("table caption");
    expect(await getFullCaptionText(table)).toEqual("Table 1: Table");

    const code = page.locator("figure[id^='listing-'] figcaption");
    expect(await getFullCaptionText(code)).toEqual("Listing 1: Code");
});
