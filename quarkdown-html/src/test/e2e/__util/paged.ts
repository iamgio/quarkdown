import {Page} from "@playwright/test";

/**
 * Collects the text of nodes that paged.js let flow out of a page into its hidden overflow column.
 * Such text was laid out but is invisible: it got lost at a page break.
 * @param page - Playwright page showing a paged document
 * @returns The trimmed text of each overflowing text node, empty when no page lost content
 */
export async function overflowingTexts(page: Page): Promise<string[]> {
    return page.evaluate(() =>
        Array.from(document.querySelectorAll<HTMLElement>(".pagedjs_page_content")).flatMap(content => {
            const right = content.getBoundingClientRect().right;
            const walker = document.createTreeWalker(content, NodeFilter.SHOW_TEXT);
            const texts: string[] = [];
            for (let node = walker.nextNode(); node; node = walker.nextNode()) {
                const range = document.createRange();
                range.selectNodeContents(node);
                if (Array.from(range.getClientRects()).some(rect => rect.width > 0 && rect.left >= right)) {
                    texts.push(range.toString().trim());
                }
            }
            return texts;
        }),
    );
}
