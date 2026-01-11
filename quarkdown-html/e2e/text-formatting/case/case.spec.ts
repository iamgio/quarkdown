import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies text case transforms", async (page) => {
    await expect(page.locator("text=LOWERCASE")).toHaveCSS("text-transform", "lowercase");
    await expect(page.locator("text=uppercase")).toHaveCSS("text-transform", "uppercase");
    await expect(page.locator("text=capitalize this")).toHaveCSS("text-transform", "capitalize");
});
