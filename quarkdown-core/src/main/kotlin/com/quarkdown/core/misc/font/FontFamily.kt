package com.quarkdown.core.misc.font

/**
 * A font family, which can be loaded from different sources.
 */
sealed interface FontFamily {
    /**
     * A font family that is installed on the system.
     * @param name the name of the system font
     */
    data class System(
        val name: String,
    ) : FontFamily

    /**
     * A font family that is loaded from a media source, such as a file or URL.
     * @param media the media object representing the font
     */
    data class Media(
        val media: com.quarkdown.core.media.Media,
    ) : FontFamily
}
