package com.quarkdown.core.media.storage.options

import com.quarkdown.core.media.LocalMedia
import com.quarkdown.core.media.MediaVisitor
import com.quarkdown.core.media.RemoteMedia

/**
 * Checks whether a media type is enabled in [options].
 * @param options media storage rules
 */
class MediaTypeEnabledChecker(private val options: MediaStorageOptions) : MediaVisitor<Boolean> {
    override fun visit(media: LocalMedia) =
        options.enableLocalMediaStorage
            // Should not happen, but it's best to throw an error to avoid ambiguous behavior.
            ?: throw IllegalStateException("Media storage option enableLocalMediaStorage is not determined.")

    override fun visit(media: RemoteMedia) =
        options.enableRemoteMediaStorage
            // Should not happen, but it's best to throw an error to avoid ambiguous behavior.
            ?: throw IllegalStateException("Media storage option enableRemoteMediaStorage is not determined.")
}
