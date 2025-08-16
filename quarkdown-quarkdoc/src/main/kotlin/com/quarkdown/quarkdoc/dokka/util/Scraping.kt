package com.quarkdown.quarkdoc.dokka.util

import com.quarkdown.quarkdoc.reader.anchors.AnchorsHtml
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.doc.A
import org.jetbrains.dokka.model.doc.DocTag

// Utilities for facilitating scraping of documentation content via quarkdoc-reader.

/**
 * Creates an anchor element at documentation level for scraping purposes, which marks a specific section of documentation.
 * @param anchor the name of the anchor to create
 * @return an [com.quarkdown.quarkdoc.reader.anchors.Anchors] anchor element as a [DocTag]
 */
fun scrapingAnchor(anchor: String): DocTag {
    assert(AnchorsHtml.ANCHOR_TAG == "a")
    return A(
        params =
            mapOf(
                AnchorsHtml.ANCHOR_ATTRIBUTE to AnchorsHtml.toAnchorAttribute(anchor),
            ),
    )
}

/**
 * Creates an anchor element at rendering level for scraping purposes, which marks a specific section of documentation.
 * @param anchor the name of the anchor to create
 * @receiver the [PageContentBuilder.DocumentableContentBuilder] to add the anchor to
 */
fun PageContentBuilder.DocumentableContentBuilder.scrapingAnchor(anchor: String) {
    assert(AnchorsHtml.ANCHOR_TAG == "a")
    assert(AnchorsHtml.ANCHOR_ATTRIBUTE == "href")
    link(text = "", address = AnchorsHtml.toAnchorAttribute(anchor))
}
