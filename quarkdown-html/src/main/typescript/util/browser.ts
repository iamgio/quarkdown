/**
 * @returns whether the current browser is Safari (excludes Chrome-based and Android browsers).
 */
export function isSafari(): boolean {
    return /^((?!chrome|android).)*safari/i.test(navigator.userAgent);
}
