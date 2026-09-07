import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("row inherits a centered global alignment", async (page) => {
    const row = page.locator(".stack.stack-row");
    await expect(row).toHaveCSS("justify-content", "center");

    const stackBox = (await row.boundingBox())!;
    const childBox = (await row.locator("p").boundingBox())!;
    const stackCenter = stackBox.x + stackBox.width / 2;
    const childCenter = childBox.x + childBox.width / 2;
    expect(childCenter).toBeCloseTo(stackCenter, 0);
});
