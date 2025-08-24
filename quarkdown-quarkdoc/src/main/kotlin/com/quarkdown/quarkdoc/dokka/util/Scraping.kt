package com.quarkdown.quarkdoc.dokka.util

import com.quarkdown.quarkdoc.dokka.kdoc.DocTagBuilder
import com.quarkdown.quarkdoc.reader.anchors.AnchorsHtml
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder

// Utilities for facilitating scraping of documentation content via quarkdoc-reader.

/**
 * Creates an anchor element at documentation level for scraping purposes, which marks a specific section of documentation.
 * @param anchor the name of the anchor to create
 * @receiver the [DocTagBuilder] to add the anchor to
 */
fun DocTagBuilder.scrapingAnchor(anchor: String) {
    assert(AnchorsHtml.ANCHOR_TAG == "a")
    assert(AnchorsHtml.ANCHOR_ATTRIBUTE == "href")
    link(address = AnchorsHtml.toAnchorAttribute(anchor)) {}
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
