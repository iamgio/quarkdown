/**
 * Extracts the target ID from an anchor element's href attribute.
 * @param link - The anchor element to extract the target ID from
 * @returns The decoded target ID (without the leading #), or undefined if invalid
 */
export function getAnchorTargetId(link: HTMLAnchorElement): string | undefined {
    const href = link.getAttribute('href');
    if (!href || !href.startsWith('#')) {
        return undefined;
    }

    let decoded: string;
    try {
        decoded = decodeURIComponent(href);
    } catch {
        return undefined;
    }

    const id = decoded.slice(1);
    return id.length > 0 ? id : undefined;
}
