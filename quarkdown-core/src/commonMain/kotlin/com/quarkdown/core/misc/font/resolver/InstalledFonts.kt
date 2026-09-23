package com.quarkdown.core.misc.font.resolver

/**
 * Lookup of the font families installed on the host, as far as the platform can tell.
 */
internal fun interface InstalledFonts {
    /**
     * @param name font family name
     * @return whether a font family called [name] is available to the renderer without being bundled
     */
    fun contains(name: String): Boolean
}

/**
 * The installed fonts of the current platform.
 */
internal expect val PLATFORM_INSTALLED_FONTS: InstalledFonts
