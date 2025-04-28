package eu.iamgio.quarkdown.media.storage

import eu.iamgio.quarkdown.media.Media

/**
 * A media file paired with a name, used in a [ReadOnlyMediaStorage].
 * @param name symbolic name of the media. This is the name which elements in the document should refer to (e.g. a local image).
 *             This is reflected to the output file name in the output directory when the media is exported
 * @param media the media
 */
data class StoredMedia(val name: String, val media: Media)
