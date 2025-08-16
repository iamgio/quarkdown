package com.quarkdown.quarkdoc.reader.anchors

import com.quarkdown.quarkdoc.reader.anchors.AnchorsHtml.ANCHOR_TAG
import org.jsoup.nodes.Element

/**
 * Utilities for handling HTML [Anchors].
 */
object AnchorsHtml {
    /**
     * The HTML tag used for anchors.
     */
    const val ANCHOR_TAG = "a"

    /**
     * The attribute used to store the anchor inside [ANCHOR_TAG] elements.
     */
    const val ANCHOR_ATTRIBUTE = "href"

    /**
     * Converts an anchor name to the HTML attribute value for [ANCHOR_TAG].
     * @param anchor the anchor name
     * @return the HTML attribute value for the anchor
     */
    fun toAnchorAttribute(anchor: String): String = "#anchor__$anchor"

    /**
     * Checks if the given [element] has an anchor with the specified [anchor] name.
     * @param anchor the anchor name to check for
     * @param element the element to check
     * @return whether the element contains the anchor, even if nested
     */
    fun hasAnchor(
        anchor: String,
        element: Element,
    ) = element.selectFirst("$ANCHOR_TAG[$ANCHOR_ATTRIBUTE='${toAnchorAttribute(anchor)}']") != null

    /**
     * Removes all anchors from the given [element].
     * @param element the element to strip anchors from
     * @return a copy of the element without any anchors
     */
    fun stripAnchors(element: Element): Element {
        val copy = element.clone()
        copy.select("$ANCHOR_TAG[$ANCHOR_ATTRIBUTE^='${toAnchorAttribute("")}']").forEach { anchor ->
            anchor.remove()
        }
        return copy
    }
}

/**
 * @see [AnchorsHtml.toAnchorAttribute]
 */
fun Element.hasAnchor(anchor: String): Boolean = AnchorsHtml.hasAnchor(anchor, this)

/**
 * @see [AnchorsHtml.stripAnchors]
 */
fun Element.stripAnchors(): Element = AnchorsHtml.stripAnchors(this)
