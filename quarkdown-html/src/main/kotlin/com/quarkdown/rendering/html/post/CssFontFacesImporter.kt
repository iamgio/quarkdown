package com.quarkdown.rendering.html.post

import com.quarkdown.core.media.LocalMedia
import com.quarkdown.core.media.MediaVisitor
import com.quarkdown.core.media.RemoteMedia
import com.quarkdown.core.media.storage.ReadOnlyMediaStorage
import com.quarkdown.core.media.storage.StoredMedia
import com.quarkdown.core.misc.font.FontFamily

private fun fontFaceSnippet(
    name: String,
    src: String,
): String = "@font-face { font-family: '$name'; src: $src; }"

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
            // local(name) for system fonts.
            is FontFamily.System -> fontFaceSnippet(family.id, "local('${family.name}')")
            // url(path) for local or remote media.
            // If the media is stored, it will use the stored media path.
            is FontFamily.Media -> {
                val storedMedia: StoredMedia? = mediaStorage.resolve(family.path)
                family.media.accept(CssFontFaceImporterMediaVisitor(family, storedMedia))
            }
            // @import for Google Fonts.
            is FontFamily.GoogleFont -> {
                "@import url('${family.path}&display=swap');"
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
        fontFaceSnippet(
            family.id,
            "url('${storedMedia?.path ?: media.file.absolutePath}')",
        )

    override fun visit(media: RemoteMedia) =
        fontFaceSnippet(
            family.id,
            "url('${storedMedia?.path ?: media.url}')",
        )
}
