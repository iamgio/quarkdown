/**
 * Gets an existing footnote rule or creates a new one within the footnote area.
 * The footnote rule is a visual separator that appears at the top of the footnote area.
 * @param footnoteArea - The footnote area element to search within or add the rule to
 * @returns The existing or newly created footnote rule element
 */
export function getOrCreateFootnoteRule(footnoteArea: Element) {
    const footnoteRuleClassName = 'footnote-rule';
    const existingRule = footnoteArea.querySelector(`.${footnoteRuleClassName}`);
    if (existingRule) return existingRule;

    const rule = document.createElement('div');
    rule.className = footnoteRuleClassName;
    footnoteArea.insertAdjacentElement('afterbegin', rule)
    return rule;
}

/**
 * Gets an existing footnote area or creates a new one within the page.
 * The footnote area contains all footnotes for a page and includes a footnote rule.
 * @param page - The page element to search within or add the footnote area to
 * @returns The existing or newly created footnote area element
 */
export function getOrCreateFootnoteArea(page: Element) {
    const className = 'footnote-area';
    let footnoteArea = page.querySelector(`.${className}`);
    if (footnoteArea) return footnoteArea;

    footnoteArea = document.createElement('div');
    footnoteArea.className = className;
    page.appendChild(footnoteArea);
    getOrCreateFootnoteRule(footnoteArea);
    return footnoteArea;
}