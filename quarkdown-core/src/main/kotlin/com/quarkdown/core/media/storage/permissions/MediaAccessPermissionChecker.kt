package com.quarkdown.core.media.storage.permissions

import com.quarkdown.core.media.LocalMedia
import com.quarkdown.core.media.MediaVisitor
import com.quarkdown.core.media.RemoteMedia
import com.quarkdown.core.permissions.Permission
import com.quarkdown.core.permissions.PermissionHolder
import com.quarkdown.core.permissions.requirePermission
import com.quarkdown.core.permissions.requireReadPermission

/**
 * Checks whether a media type is allowed to be stored according to the granted permissions in [context].
 * If a required permission is missing, a [com.quarkdown.core.permissions.MissingPermissionException] will be thrown.
 * @param context the context to access the media storage from
 */
class MediaAccessPermissionChecker(
    private val holder: PermissionHolder,
) : MediaVisitor<Unit> {
    override fun visit(media: LocalMedia) {
        holder.requireReadPermission(media.file)
    }

    override fun visit(media: RemoteMedia) {
        holder.requirePermission(
            Permission.NetworkAccess,
            message = "Cannot access remote media ${media.url}",
        )
    }
}
