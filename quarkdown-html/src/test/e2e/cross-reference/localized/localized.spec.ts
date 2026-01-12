import {getFullText} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("displays cross-references with localized labels", async (page) => {
    const refs = page.locator(".cross-reference");
    await expect(refs).toHaveCount(3);

    const headingRef = refs.nth(0);
    const equationRef = refs.nth(1);
    const tableRef = refs.nth(2);

    await expect(headingRef).toHaveAttribute("data-localized-kind", "Section");
    expect(await getFullText(headingRef)).toEqual("Section 1");
    expect(await getFullText(equationRef)).toEqual("(1)");
    expect(await getFullText(tableRef)).toEqual("Table 1");
});
