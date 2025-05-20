package com.quarkdown.core.media.storage

import com.quarkdown.core.media.Media
import com.quarkdown.core.media.ResolvableMedia
import com.quarkdown.core.media.export.MediaOutputResourceConverter
import com.quarkdown.core.media.storage.name.MediaNameProviderStrategy
import com.quarkdown.core.media.storage.name.SanitizedMediaNameProvider
import com.quarkdown.core.media.storage.options.MediaStorageOptions
import com.quarkdown.core.media.storage.options.MediaTypeEnabledChecker
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import java.io.File

/**
 * Name of the subdirectory in the output directory where media is stored.
 */
private const val MEDIA_SUBDIRECTORY_NAME = "media"

/**
 * A media storage that can be modified with new entries.
 * @param options storage rules
 * @param nameProvider strategy used to generate media names.
 *                     The name of a media defines the file name in the output directory,
 *                     hence this is the resource the document should refer to (e.g. images).
 */
class MutableMediaStorage(
    options: MediaStorageOptions,
    private val nameProvider: MediaNameProviderStrategy = SanitizedMediaNameProvider(),
) : ReadOnlyMediaStorage {
    /**
     * All the stored entries.
     */
    private val bindings = mutableMapOf<String, StoredMedia>()

    /**
     * Visitor that checks if a media type is enabled and should be stored.
     */
    private val enabledChecker = MediaTypeEnabledChecker(options)

    override val all: Set<StoredMedia>
        get() = bindings.values.toSet()

    override fun resolve(path: String): StoredMedia? = bindings[path]

    override fun resolveMediaLocationOrFallback(path: String): String =
        resolve(path)
            ?.name
            ?.let { "$MEDIA_SUBDIRECTORY_NAME/$it" }
            ?: path

    override fun toResource(): OutputResource {
        val subResources =
            this.all
                .asSequence()
                .map {
                    val converter = MediaOutputResourceConverter(it.name)
                    it.media.accept(converter)
                }.toSet()

        return OutputResourceGroup(name = MEDIA_SUBDIRECTORY_NAME, subResources)
    }

    /**
     * Binds a media to a path.
     */
    private fun bind(
        path: String,
        media: Media,
    ) {
        // Media is not stored if its type isn't enabled.
        if (!media.accept(enabledChecker)) return

        bindings.putIfAbsent(
            path,
            StoredMedia(
                name = media.accept(nameProvider),
                media,
            ),
        )
    }

    /**
     * Registers a media by its path. The corresponding media is resolved lazily from the path.
     * @param path path to the media, either a file or a URL
     * @param workingDirectory directory to resolve the media from, in case the path is relative
     */
    fun register(
        path: String,
        workingDirectory: File?,
    ) = bind(path, ResolvableMedia(path, workingDirectory))
}
