import {getFullText} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("displays localized numbering labels", async (page) => {
    const figure = page.locator("figure[id^='figure-'] figcaption");
    expect(await getFullText(figure)).toEqual("Figure 1: Fig");

    const table = page.locator("table caption");
    expect(await getFullText(table)).toEqual("Table 1: Table");

    const code = page.locator("figure[id^='listing-'] figcaption");
    expect(await getFullText(code)).toEqual("Listing 1: Code");
});
