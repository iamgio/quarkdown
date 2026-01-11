import {getFullText} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("displays cross-references with numbers only", async (page) => {
    const refs = page.locator(".cross-reference");
    await expect(refs).toHaveCount(3);

    const headingRef = refs.nth(0);
    const equationRef = refs.nth(1);
    const tableRef = refs.nth(2);

    await expect(headingRef).toHaveAttribute("data-location", "1");
    await expect(equationRef).toHaveAttribute("data-location", "(1)");
    await expect(tableRef).toHaveAttribute("data-location", "1");

    expect(await getFullText(headingRef)).toEqual("1");
    expect(await getFullText(equationRef)).toEqual("(1)");
    expect(await getFullText(tableRef)).toEqual("1");
});
