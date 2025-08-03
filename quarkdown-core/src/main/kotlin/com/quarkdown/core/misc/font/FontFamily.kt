package com.quarkdown.core.misc.font

import java.net.URLEncoder

private const val GOOGLE_FONTS_URL = "https://fonts.googleapis.com/css2"

/**
 * A font family, which can be loaded from different sources.
 */
sealed interface FontFamily {
    /**
     * Name, path or URL that the font family was loaded from.
     */
    val path: String

    /**
     * Unique identifier for the font family, based on its path.
     * Multiple font families with the same path will have the same ID.
     *
     * In HTML rendering, this ID is assigned to `font-family`.
     */
    val id: String
        get() = path.hashCode().toString()

    /**
     * A font family that is installed on the system.
     * @param name the name of the system font
     */
    data class System(
        val name: String,
    ) : FontFamily {
        override val path: String
            get() = name
    }

    /**
     * A font family that is loaded from a media source, such as a file or URL.
     * @param media the media object representing the font
     */
    data class Media(
        val media: com.quarkdown.core.media.Media,
        override val path: String,
    ) : FontFamily

    /**
     * A font family that is loaded from [Google Fonts](https://fonts.google.com).
     * @param name the name of the Google Font, case-sensitive
     */
    data class GoogleFont(
        val name: String,
    ) : FontFamily {
        override val path: String
            get() = "$GOOGLE_FONTS_URL?family=${URLEncoder.encode(name, Charsets.UTF_8)}"

        override val id: String
            get() = name
    }
}
