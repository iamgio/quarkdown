import {Locator, Page} from "@playwright/test";

/**
 * Gets the full visible text of an element including ::before and ::after pseudo-element content.
 * @param locator - Playwright locator for the element
 * @returns The combined text content (before + inner + after), trimmed
 */
export async function getFullText(locator: Locator): Promise<string> {
    return locator.evaluate((el: Element) => {
        const extractContent = (pseudo: string) => {
            const content = getComputedStyle(el, pseudo).content;
            return content === "none" ? "" : content.replace(/^"|"$/g, "");
        };
        return (extractContent("::before") + el.textContent + extractContent("::after")).trim();
    });
}

/**
 * Gets the raw CSS content value of the ::before pseudo-element.
 * @param locator - Playwright locator for the element
 * @returns The raw content value (e.g., '"1"' or 'none')
 */
export async function getBeforeContent(locator: Locator): Promise<string> {
    return locator.evaluate((el: Element) => getComputedStyle(el, "::before").content);
}

/**
 * Checks if the ::before pseudo-element is on the same line as the element's text.
 * Compares the Y position of the ::before (via element box) with the text content's Y.
 * @param locator - Playwright locator for the element
 * @returns True if the ::before and text share the same vertical position
 */
export async function isBeforeInline(locator: Locator): Promise<boolean> {
    return locator.evaluate((el: Element) => {
        const textNode = el.firstChild;
        if (!textNode || textNode.nodeType !== Node.TEXT_NODE) return false;

        const range = document.createRange();
        range.selectNodeContents(textNode);
        const textRect = range.getBoundingClientRect();
        const elementRect = el.getBoundingClientRect();

        // ::before is inline if element top matches text top (within 1px tolerance)
        return Math.abs(elementRect.top - textRect.top) <= 1;
    });
}

/**
 * Computes a CSS value by applying it to a temporary element and reading the computed result.
 * @param context - Playwright locator or Page for context
 * @param property - CSS property to set (e.g., "height", "color")
 * @param value - CSS value to compute (e.g., "var(--qd-link-color)", "1.3em")
 * @param getter - Function to extract the computed value from the temp element
 */
async function computeCssValue<T>(
    context: Locator | Page,
    property: string,
    value: string,
    getter: (el: HTMLElement) => T
): Promise<T> {
    const locator = "goto" in context ? context.locator("body") : context;
    return locator.evaluate(
        (el, {prop, val, getterFn}) => {
            const temp = document.createElement("div");
            temp.style.setProperty(prop, val);
            el.appendChild(temp);
            const result = new Function("el", `return ${getterFn}`)(temp);
            temp.remove();
            return result;
        },
        {prop: property, val: value, getterFn: getter.toString().replace(/^\(?\w+\)?\s*=>\s*/, "")}
    );
}

/**
 * Gets the computed pixel value of a CSS size in the context of a specific element.
 * Useful for em values which are relative to the element's inherited font-size.
 * @param context - Playwright locator for the context element, or Page (uses body)
 * @param value - CSS value to compute (e.g., "var(--qd-block-margin)", "1.3em")
 * @returns The computed size in pixels
 */
export async function getComputedSizeProperty(context: Locator | Page, value: string): Promise<number> {
    return computeCssValue(context, "height", value, (el) => el.getBoundingClientRect().height);
}

/**
 * Gets the computed RGB color value of a CSS value.
 * @param context - Playwright locator or Page for context
 * @param value - CSS color value (e.g., "var(--qd-link-color)", "hsl(0, 100%, 50%)")
 * @returns The computed color in RGB format (e.g., "rgb(168, 0, 0)")
 */
export async function getComputedColor(context: Locator | Page, value: string): Promise<string> {
    return computeCssValue(context, "color", value, (el) => getComputedStyle(el).color);
}

/**
 * Gets the computed style of an element.
 * @param locator - Playwright locator for the element
 * @returns The computed CSSStyleDeclaration as a plain object
 */
export async function evaluateComputedStyle(locator: Locator): Promise<CSSStyleDeclaration> {
    return locator.evaluate((el) => globalThis.getComputedStyle(el));
}
