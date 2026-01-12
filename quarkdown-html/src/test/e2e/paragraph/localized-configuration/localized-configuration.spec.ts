import {evaluateComputedStyle} from "../../__util/css";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies localized configuration (Chinese)", async (page) => {
    const paragraphs = page.locator("p");
    await expect(paragraphs).toHaveCount(2);

    for (const i of [0, 1]) {
        const style = await evaluateComputedStyle(paragraphs.nth(i));
        expect(style.lineHeight).not.toBe("0px");
        expect(style.textIndent).not.toBe("0px");
        expect(style.marginTop).toBe("0px");
    }
});
