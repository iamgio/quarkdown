package com.quarkdown.core.media.passthrough

/**
 * Handles passthrough paths in link and image URLs.
 *
 * A passthrough path starts with the [PATH_PREFIX] symbol (`@`) and usually resolves to the output root.
 * This allows referencing shared assets (e.g. static files, images) from any subdocument without relying on fragile relative paths.
 *
 * For example, when rendering to HTML, `@/logo.png` refers to `logo.png` at the output root,
 * regardless of the current subdocument's depth.
 *
 * Passthrough paths are:
 * - Excluded from [com.quarkdown.core.context.hooks.LinkUrlResolverHook] resolution, preserving the `@` prefix intact.
 * - Excluded from [com.quarkdown.core.context.hooks.MediaStorerHook] registration, since the referenced file
 *   is expected to already be present in the output (e.g. via the `public/` static assets directory in HTML rendering).
 *
 * The actual replacement of the `@` prefix with a concrete relative path (e.g. `.` or `..`)
 * is performed by the active [com.quarkdown.core.rendering.NodeRenderer].
 *
 * @see com.quarkdown.core.rendering.NodeRenderer
 */
object MediaPassthrough {
    /**
     * Symbol used in URLs to indicate the root of the output directory.
     */
    const val PATH_PREFIX = "@"

    /**
     * Whether the given [path] is a passthrough path,
     * i.e. it equals [PATH_PREFIX] or starts with it followed by a path separator.
     */
    fun isPassthroughPath(path: String): Boolean =
        path == PATH_PREFIX ||
            path.startsWith("$PATH_PREFIX/") ||
            path.startsWith("$PATH_PREFIX\\")

    /**
     * Replaces the [PATH_PREFIX] at the start of [path] with [replacement], if present.
     * If the path is not a passthrough path, it is returned unchanged.
     * @param path the URL to process
     * @param replacement the string to substitute for [PATH_PREFIX]
     * @return the path with the prefix replaced, or the original path
     */
    fun replacePassthroughPrefix(
        path: String,
        replacement: String,
    ): String =
        if (isPassthroughPath(path)) {
            path.replaceFirst(PATH_PREFIX, replacement)
        } else {
            path
        }
}
