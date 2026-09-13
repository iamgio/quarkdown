package com.quarkdown.core.media

import io.ktor.http.Url

/**
 * A media stored remotely.
 * @param url the URL where the media is stored
 */
data class RemoteMedia(
    val url: Url,
) : Media {
    override fun <T> accept(visitor: MediaVisitor<T>): T = visitor.visit(this)
}
