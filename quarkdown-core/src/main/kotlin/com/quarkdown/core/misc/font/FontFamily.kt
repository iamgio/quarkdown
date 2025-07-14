package com.quarkdown.core.misc.font

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
     */
    val id: Int
        get() = path.hashCode()

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
}
