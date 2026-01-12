import {evaluateComputedStyle} from "../../__util/css";
import {isHashedFont} from "../index";
import {suite} from "../../quarkdown";

const {test, expect} = suite(__dirname);

test("applies different custom fonts to main, headings, and code", async (page) => {
    // Paragraph uses custom main font (hashed)
    const paragraph = page.locator("p");
    const pStyle = await evaluateComputedStyle(paragraph);
    expect(isHashedFont(pStyle.fontFamily)).toBe(true);

    // Heading uses custom heading font (hashed)
    const h2 = page.locator("h2");
    const h2Style = await evaluateComputedStyle(h2);
    expect(isHashedFont(h2Style.fontFamily)).toBe(true);

    // Code block uses custom code font (hashed)
    const codeBlock = page.locator("pre code");
    const codeBlockStyle = await evaluateComputedStyle(codeBlock);
    expect(isHashedFont(codeBlockStyle.fontFamily)).toBe(true);

    // All three fonts are different from each other
    expect(pStyle.fontFamily).not.toBe(h2Style.fontFamily);
    expect(pStyle.fontFamily).not.toBe(codeBlockStyle.fontFamily);
    expect(h2Style.fontFamily).not.toBe(codeBlockStyle.fontFamily);
});
