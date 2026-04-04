package com.quarkdown.core.media.storage

import com.quarkdown.core.media.Media
import com.quarkdown.core.media.ResolvableMedia
import com.quarkdown.core.media.export.MediaOutputResourceConverter
import com.quarkdown.core.media.storage.name.MediaNameProviderStrategy
import com.quarkdown.core.media.storage.name.SanitizedMediaNameProvider
import com.quarkdown.core.media.storage.options.MediaStorageOptions
import com.quarkdown.core.media.storage.options.MediaTypeEnabledChecker
import com.quarkdown.core.media.storage.permissions.MediaAccessPermissionChecker
import com.quarkdown.core.permissions.PermissionHolder
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import java.io.File

/**
 * The name of the media subdirectory in the output resources.
 */
const val MEDIA_SUBDIRECTORY_NAME = "media"

/**
 * A media storage that can be modified with new entries.
 * @param options storage rules
 * @param permissionHolder holder to check media access permissions against
 * @param nameProvider strategy used to generate media names.
 *                     The name of a media defines the file name in the output directory,
 *                     hence this is the resource the document should refer to (e.g. images).
 */
class MutableMediaStorage(
    options: MediaStorageOptions,
    permissionHolder: PermissionHolder,
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

    /**
     * Visitor that checks if a media can be accessed according to the granted permissions.
     * If a required permission is missing, a [com.quarkdown.core.permissions.MissingPermissionException] will be thrown.
     */
    private val permissionChecker = MediaAccessPermissionChecker(permissionHolder)

    override val name: String = MEDIA_SUBDIRECTORY_NAME

    override val all: Set<StoredMedia>
        get() = bindings.values.toSet()

    override fun resolve(path: String): StoredMedia? = bindings[path]

    override fun toResource(): OutputResource {
        val subResources =
            this.all
                .map {
                    val converter = MediaOutputResourceConverter(it.name)
                    it.media.accept(converter)
                }.toSet()

        return OutputResourceGroup(this.name, subResources)
    }

    /**
     * Binds a media to a path.
     * @return the [StoredMedia] associated with the path. If a media was already bound to the path, it is returned. Otherwise, the new [media] is returned.
     * It may also return `null` if the media is not accepted into the storage.
     */
    private fun bind(
        path: String,
        media: Media,
    ): StoredMedia? {
        // Media is not stored if its type isn't enabled.
        if (!media.accept(enabledChecker)) return null
        // Binding fails if the required permissions to access it aren't granted.
        media.accept(permissionChecker)

        val media =
            StoredMedia(
                name = media.accept(nameProvider),
                media = media,
                storage = this,
            )

        return bindings.putIfAbsent(path, media) ?: media
    }

    /**
     * Registers a media by its path.
     * @param path path to the media, either a file or a URL
     * @param media media to register
     * @return the [StoredMedia] associated with the media. If a media was already bound to the path, it is returned. Otherwise, the new [media] is returned.
     */
    fun register(
        path: String,
        media: Media,
    ): StoredMedia? = bind(path, media)

    /**
     * Registers a media by its path. The corresponding media is resolved lazily from the path.
     * @param path path to the media, either a file or a URL
     * @param workingDirectory directory to resolve the media from, in case the path is relative
     * @return the [StoredMedia] associated with the path. If a media was already bound to the path, it is returned. Otherwise, the new [media] is returned.
     * It may also return `null` if the media is not accepted into the storage.
     * @see ResolvableMedia
     */
    fun register(
        path: String,
        workingDirectory: File?,
    ): StoredMedia? = register(path, ResolvableMedia(path, workingDirectory))
}
