package com.quarkdown.core.misc.font.resolver

import java.awt.GraphicsEnvironment

/**
 * The font families AWT reports as installed.
 */
internal actual val PLATFORM_INSTALLED_FONTS: InstalledFonts =
    InstalledFonts { name -> name in GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames }
