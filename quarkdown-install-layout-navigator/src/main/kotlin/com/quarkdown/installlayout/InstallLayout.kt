package com.quarkdown.installlayout

/**
 * Type-safe navigator over the Quarkdown install `lib/` directory layout.
 *
 * The same layout is mirrored at dev time by the `assembleDevLib` Gradle task under
 * `<rootProject>/build/dev-lib/`.
 */
class InstallLayout(
    directory: InstallLayoutDirectory,
) : InstallLayoutEntry by directory {
    /** The directory containing `.qd` library files. */
    val quarkdownLibraries get() = resolveDirectory("qd")

    /** The subtree containing all HTML rendering resources. */
    val htmlResources get() = resolveDirectory("html").let(::Html)

    companion object {
        /** Lazily resolved singleton pointing to the current process's install layout. Throws if resolution fails. */
        val get by lazy(InstallDirectoryResolver::resolve)

        /**
         * Like [get], but returns `null` instead of throwing if the install directory
         * cannot be resolved (e.g. when the JAR's code source is unavailable).
         */
        val getOrNull: InstallLayout? by lazy {
            runCatching { get }.getOrNull()
        }
    }

    /**
     * The HTML subtree of the install layout.
     */
    class Html(
        directory: InstallLayoutDirectory,
    ) : InstallLayoutEntry by directory {
        /** Third-party JS/CSS libraries (e.g. KaTeX, Mermaid). */
        val libraries get() = resolveDirectory("lib")

        /** Compiled CSS themes, organized by kind (layout, color, locale). */
        val themes get() = resolveDirectory("theme").let(::Themes)

        /** The Quarkdown runtime script directory. */
        val scripts get() = resolveDirectory("script")

        /**
         * Per-kind theme directories.
         */
        class Themes(
            directory: InstallLayoutDirectory,
        ) : InstallLayoutEntry by directory {
            /** Global stylesheet (`global.css`). */
            val global get() = resolveFile("global.css")

            /** Layout themes. */
            val layout get() = resolveDirectory("layout")

            /** Color themes. */
            val color get() = resolveDirectory("color")

            /** Locale-specific themes. */
            val locale get() = resolveDirectory("locale")
        }
    }
}
