package com.quarkdown.rendering.html.post

import com.quarkdown.core.media.LocalMedia
import com.quarkdown.core.media.MediaVisitor
import com.quarkdown.core.media.RemoteMedia
import com.quarkdown.core.media.storage.ReadOnlyMediaStorage
import com.quarkdown.core.media.storage.StoredMedia
import com.quarkdown.core.misc.font.FontFamily

/**
 * Generator of `@font-face` and `@import` CSS rules for a list of [FontFamily],
 * to be used in HTML post-rendering. It supports system, local, and remote media sources.
 * @param families the list of [FontFamily] to generate CSS rules for
 * @param mediaStorage the media storage to resolve media paths against.
 * In case the storage contains the media, the stored media will be referenced rather than the original resource.
 */
class CssFontFacesImporter(
    private val families: List<FontFamily>,
    private val mediaStorage: ReadOnlyMediaStorage,
) {
    private fun toSnippet(family: FontFamily): String =
        when (family) {
            is FontFamily.System -> "@font-face { font-family: '${family.id}'; src: local('${family.name}'); }"
            is FontFamily.Media -> {
                val storedMedia: StoredMedia? = mediaStorage.resolve(family.path)
                family.media.accept(CssFontFaceImporterMediaVisitor(family, storedMedia))
            }
        }

    /**
     * @return one CSS import snippet per [FontFamily] in the list.
     */
    fun toSnippets(): List<String> = families.map(::toSnippet)

    companion object {
        /**
         * Creates a new [CssFontFacesImporter] from a list of [FontFamily].
         * @param mediaStorage the media storage to resolve media paths against.
         * @param families the list of [FontFamily] to generate CSS rules for. `null` values are ignored.
         */
        fun ofNullables(
            mediaStorage: ReadOnlyMediaStorage,
            vararg families: FontFamily?,
        ): CssFontFacesImporter =
            CssFontFacesImporter(
                families.filterNotNull(),
                mediaStorage,
            )
    }
}

private class CssFontFaceImporterMediaVisitor(
    private val family: FontFamily,
    private val storedMedia: StoredMedia?,
) : MediaVisitor<String> {
    override fun visit(media: LocalMedia) =
        "@font-face { font-family: '${family.id}'; src: url('${storedMedia?.path ?: media.file.absolutePath}'); }"

    override fun visit(media: RemoteMedia) = "@import url('${storedMedia?.path ?: media.url}');"
}
