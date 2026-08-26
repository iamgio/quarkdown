package com.quarkdown.core.media.storage.options

import com.quarkdown.core.media.fetch.RemoteMediaFetcher

/**
 * Read-only options that affect the rules of a media storage.
 * Used in [com.quarkdown.core.rendering.PostRenderer] to determine the preferred rules of a rendering strategy.
 */
data class ReadOnlyMediaStorageOptions(
    override val enableRemoteMediaStorage: Boolean? = null,
    override val enableLocalMediaStorage: Boolean? = null,
    override val remoteMediaFetcher: RemoteMediaFetcher? = null,
) : MediaStorageOptions
