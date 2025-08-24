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
     * @return the HTML selector for an anchor with the specified [anchor] name
     */
    private fun anchorToSelector(anchor: String): String = "$ANCHOR_TAG[$ANCHOR_ATTRIBUTE='${toAnchorAttribute(anchor)}']"

    /**
     * Gets the element of the anchor with the specified [anchor] name within the given [element], if the anchor exists.
     * @param anchor the anchor name to look for
     * @param element the element to search in
     * @return the element of the anchor, or `null` if not found
     */
    fun getAnchorElement(
        anchor: String,
        element: Element,
    ): Element? = element.selectFirst(anchorToSelector(anchor))

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
 * @see [AnchorsHtml.getAnchorElement]
 */
fun Element.getAnchorElement(anchor: String): Element? = AnchorsHtml.getAnchorElement(anchor, this)

/**
 * @return the next sibling element of the anchor with the specified [anchor] name, or `null` if not found or no next sibling exists
 * @see [AnchorsHtml.getAnchorElement]
 */
fun Element.getAnchorNextElement(anchor: String): Element? = getAnchorElement(anchor)?.nextElementSibling()

/**
 * Checks if the given element has an anchor with the specified [anchor] name.
 * @param anchor the anchor name to check for
 * @return whether the element contains the anchor, even if nested
 */
fun Element.hasAnchor(anchor: String): Boolean = getAnchorElement(anchor) != null

/**
 * @see [AnchorsHtml.stripAnchors]
 */
fun Element.stripAnchors(): Element = AnchorsHtml.stripAnchors(this)
