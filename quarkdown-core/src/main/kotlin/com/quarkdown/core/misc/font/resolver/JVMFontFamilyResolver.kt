package com.quarkdown.core.misc.font.resolver

import com.quarkdown.core.media.ResolvableMedia
import com.quarkdown.core.misc.font.FontFamily
import java.awt.GraphicsEnvironment
import java.io.File

/**
 * JVM/AWT implementation of [FontFamilyResolver].
 */
internal object JVMFontFamilyResolver : FontFamilyResolver {
    private fun isSystemFont(name: String) = name in GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames

    override fun resolve(
        nameOrPath: String,
        workingDirectory: File?,
    ): FontFamily? =
        when {
            isSystemFont(nameOrPath) -> FontFamily.System(nameOrPath)
            else -> {
                val media = ResolvableMedia(nameOrPath, workingDirectory)
                FontFamily.Media(media)
            }
        }
}
