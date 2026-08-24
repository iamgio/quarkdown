package com.quarkdown.interaction.os

/**
 * A family of operating systems Quarkdown distinguishes between when locating
 * external programs, such as browsers, whose install locations are platform-specific.
 */
enum class OsFamily {
    WINDOWS,
    MACOS,
    LINUX,

    /**
     * A Unix-like system that is neither macOS nor Linux, or an unrecognized system.
     */
    OTHER,
    ;

    companion object {
        /**
         * Resolves the family from the value of the `os.name` system property.
         * @param osName the raw system property value, in any casing
         * @return the matching family, or [OTHER] if unrecognized
         */
        fun of(osName: String): OsFamily {
            val name = osName.lowercase()
            // macOS is matched first: "darwin" contains "win", which would otherwise match Windows.
            return when {
                "mac" in name || "darwin" in name -> MACOS
                "win" in name -> WINDOWS
                "nux" in name -> LINUX
                else -> OTHER
            }
        }
    }
}
