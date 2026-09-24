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

    /** The bundled agent skill directory, containing the `SKILL.md` entrypoint and any supporting files. */
    val agentSkill get() = resolveDirectory("skills").resolveDirectory("quarkdown")

    companion object

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
