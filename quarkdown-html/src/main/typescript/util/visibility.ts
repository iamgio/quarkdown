/**
 * Check if an element is hidden (has the data-hidden attribute).
 * @param element The element to check.
 * @returns Whether the element is hidden.
 */
export function isHidden(element: Element): boolean {
    return element.hasAttribute("data-hidden");
}

/**
 * Check if an element is blank (has no children or only hidden children).
 * @param element The element to check.
 * @returns Whether the element is blank.
 */
export function isBlank(element: HTMLElement): boolean {
    return element.childNodes.length === 0 ||
        Array.from(element.children).every(child => isHidden(child));
}