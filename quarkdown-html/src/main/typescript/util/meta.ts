const ROOT_PATH_META = "quarkdown:root-path";

/**
 * Retrieves the content of a meta tag by its name.
 * @param name - The name attribute of the meta tag
 * @returns The content of the meta tag, or null if not found
 */
export function getMetaContent(name: string): string | null {
    const meta = document.querySelector(`meta[name="${name}"]`);
    return meta?.getAttribute("content") ?? null;
}

/**
 * Retrieves the path to the root of the Quarkdown output directory,
 * from the `quarkdown:root-path` meta tag.
 * @returns The root path, or "/" if not specified
 */
export function getRootPath(): string {
    return getMetaContent(ROOT_PATH_META) || "/";
}