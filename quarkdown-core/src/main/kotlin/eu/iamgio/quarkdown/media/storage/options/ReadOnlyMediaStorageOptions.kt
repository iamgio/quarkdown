package eu.iamgio.quarkdown.media.storage.options

/**
 * Read-only options that affect the rules of a media storage.
 * Used in [eu.iamgio.quarkdown.rendering.PostRenderer] to determine the preferred rules of a rendering strategy.
 */
data class ReadOnlyMediaStorageOptions(
    override val enableRemoteMediaStorage: Boolean? = null,
    override val enableLocalMediaStorage: Boolean? = null,
) : MediaStorageOptions
