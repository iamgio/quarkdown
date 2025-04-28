package eu.iamgio.quarkdown.media.storage.options

/**
 * Read-only options that affect the rules of a context's media storage.
 * @see eu.iamgio.quarkdown.context.MutableContextOptions for an implementation
 */
interface MediaStorageOptions {
    /**
     * Whether remote media associated to a URL should be stored locally.
     * If enabled, the media is downloaded, stored in the output directory
     * and the element that references the media is updated to reference the new local path.
     * If null, the preference is determined by the active renderer.
     */
    val enableRemoteMediaStorage: Boolean?

    /**
     * Whether local media should be stored locally in the output directory.
     * If enabled, the media is copied to the output directory
     * and the element that references the media is updated to reference the new path.
     * If null, the preference is determined by the active renderer.
     */
    val enableLocalMediaStorage: Boolean?
}
