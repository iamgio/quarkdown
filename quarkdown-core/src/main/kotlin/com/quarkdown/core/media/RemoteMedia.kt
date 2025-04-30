package com.quarkdown.core.media

import java.net.URL

/**
 * A media stored remotely.
 * @param url the URL where the media is stored
 */
data class RemoteMedia(val url: URL) : Media {
    override fun <T> accept(visitor: MediaVisitor<T>): T = visitor.visit(this)
}
