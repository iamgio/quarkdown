package com.quarkdown.core.context.options

import com.quarkdown.core.media.fetch.RemoteMediaFetcher
import com.quarkdown.core.media.fetch.UrlRemoteMediaFetcher
import com.quarkdown.core.media.storage.options.MediaStorageOptions
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val DEFAULT_SUBDOCUMENT_URL_SUFFIXES = setOf(".qd", ".md")

/**
 * Mutable [ContextOptions] implementation.
 */
@OptIn(ExperimentalUuidApi::class)
data class MutableContextOptions(
    override var autoPageBreakHeadingMaxDepth: Int = 1,
    override var enableAutomaticIdentifiers: Boolean = true,
    override var enableLocationAwareness: Boolean = true,
    override var subdocumentUrlSuffixes: Set<String> = DEFAULT_SUBDOCUMENT_URL_SUFFIXES,
    override var uuidSupplier: () -> String = { Uuid.random().toString() },
    override var enableRemoteMediaStorage: Boolean = false,
    override var enableLocalMediaStorage: Boolean = false,
    override var remoteMediaFetcher: RemoteMediaFetcher = UrlRemoteMediaFetcher,
    override var html: HtmlOptions = HtmlOptions(),
) : ContextOptions {
    /**
     * Mutates this instance by merging the current media storage rules with the given [options].
     * An option is overridden and merged only if its value from [options] is set, i.e. not `null`.
     * @param options options to merge this instance with
     */
    fun mergeMediaStorageOptions(options: MediaStorageOptions) {
        options.enableRemoteMediaStorage?.let { enableRemoteMediaStorage = it }
        options.enableLocalMediaStorage?.let { enableLocalMediaStorage = it }
        options.remoteMediaFetcher?.let { remoteMediaFetcher = it }
    }
}
