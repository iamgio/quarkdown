import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies text-align-last: start only to non-split paragraphs",
    async (page) => {
        const paragraphs = page.locator("p");
        const first = paragraphs.nth(0);
        const second = paragraphs.nth(1);

        await expect(first).toHaveAttribute("data-split-to");
        await expect(first).not.toHaveCSS("text-align-last", "start");

        await expect(second).not.toHaveAttribute("data-split-to");
        await expect(second).toHaveCSS("text-align-last", "start");
    }
);
