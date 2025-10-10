import {FootnotePair} from "./footnote-pair";

/**
 * Retrieves all footnote definition elements from the document.
 * @param sorted - Whether to sort definitions by their footnote index
 * @returns Array of footnote definition HTML elements
 */
function getFootnoteDefinitions(sorted: boolean): HTMLElement[] {
    const definitions = Array.from(document.querySelectorAll<HTMLElement>('.footnote-definition'));
    if (!sorted) {
        return definitions;
    }
    return definitions.sort((a, b) => {
        const indexA = parseInt(a.dataset.footnoteIndex || '0');
        const indexB = parseInt(b.dataset.footnoteIndex || '0');
        return indexA - indexB;
    });
}

function getFootnoteFirstReference(definitionId: string): HTMLElement | null {
    return document.querySelector<HTMLElement>(`.footnote-reference[data-definition="${definitionId}"]`);
}

/**
 * Creates footnote pairs by matching definitions with their first references.
 * @param sorted - Whether to sort definitions by footnote index (default: true)
 * @returns Array of footnote pairs, linking definitions to their first references
 */
export function getFootnoteDefinitionsAndFirstReference(sorted: boolean = true): FootnotePair[] {
    const definitions: HTMLElement[] = getFootnoteDefinitions(sorted);

    // For each definition, gets the first reference to it.
    return definitions.map(definition => {
        const reference = getFootnoteFirstReference(definition.id);
        return reference ? new FootnotePair(reference, definition) : null;
    }).filter(item => item !== null);
}