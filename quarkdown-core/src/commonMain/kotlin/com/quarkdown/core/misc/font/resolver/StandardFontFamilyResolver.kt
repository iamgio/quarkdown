package com.quarkdown.core.misc.font.resolver

import com.quarkdown.core.filesystem.FileSystem
import com.quarkdown.core.media.ResolvableMedia
import com.quarkdown.core.misc.font.FontFamily

private const val GOOGLE_FONTS_PREFIX = "GoogleFonts:"

/**
 * [FontFamilyResolver] that recognizes Google Fonts by prefix, installed fonts by name, and anything else as media.
 * @param installedFonts the fonts available on the host
 */
internal class StandardFontFamilyResolver(
    private val installedFonts: InstalledFonts,
) : FontFamilyResolver {
    override fun resolve(
        nameOrPath: String,
        fileSystem: FileSystem,
    ): FontFamily? =
        when {
            nameOrPath.startsWith(GOOGLE_FONTS_PREFIX) -> FontFamily.GoogleFont(nameOrPath.removePrefix(GOOGLE_FONTS_PREFIX))
            installedFonts.contains(nameOrPath) -> FontFamily.System(nameOrPath)
            else -> FontFamily.Media(ResolvableMedia(nameOrPath, fileSystem), nameOrPath)
        }
}
