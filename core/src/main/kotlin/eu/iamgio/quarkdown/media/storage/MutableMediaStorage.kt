package eu.iamgio.quarkdown.media.storage

import eu.iamgio.quarkdown.media.Media
import eu.iamgio.quarkdown.media.ResolvableMedia
import eu.iamgio.quarkdown.media.export.MediaOutputResourceConverter
import eu.iamgio.quarkdown.media.storage.name.MediaNameProviderStrategy
import eu.iamgio.quarkdown.media.storage.name.SanitizedMediaNameProvider
import eu.iamgio.quarkdown.pipeline.output.OutputResource
import eu.iamgio.quarkdown.pipeline.output.OutputResourceGroup
import java.io.File

/**
 * A media storage that can be modified with new entries.
 * @param nameProvider strategy used to generate media names.
 *                     The name of a media is defines the file name in the output directory,
 *                     hence this is the resource the document should refer to (e.g. images).
 */
class MutableMediaStorage(
    private val nameProvider: MediaNameProviderStrategy = SanitizedMediaNameProvider(),
) : ReadOnlyMediaStorage {
    /**
     * All the stored entries.
     */
    private val bindings = mutableMapOf<String, StoredMedia>()

    override val all: Set<StoredMedia>
        get() = bindings.values.toSet()

    override fun resolve(path: String): StoredMedia? = bindings[path]

    override fun toResource(): OutputResource {
        val subResources =
            this.all.asSequence()
                .map {
                    val converter = MediaOutputResourceConverter(it.name)
                    it.media.accept(converter)
                }
                .toSet()

        return OutputResourceGroup(name = "media", subResources)
    }

    /**
     * Binds a media to a path.
     */
    private fun bind(
        path: String,
        media: Media,
    ) {
        bindings[path] =
            StoredMedia(
                name = media.accept(nameProvider),
                media,
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
