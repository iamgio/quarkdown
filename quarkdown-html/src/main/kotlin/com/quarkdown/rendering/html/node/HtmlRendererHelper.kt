package com.quarkdown.rendering.html.node

import com.quarkdown.core.ast.attributes.localization.LocalizedKind
import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.attributes.location.getLocationLabel
import com.quarkdown.core.ast.quarkdown.CaptionableNode
import com.quarkdown.core.ast.quarkdown.reference.CrossReferenceableNode
import com.quarkdown.core.ast.quarkdown.reference.linkableReferenceId
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.localization.localizeOrNull
import com.quarkdown.core.document.layout.caption.CaptionPosition
import com.quarkdown.core.document.layout.caption.CaptionPositionInfo
import com.quarkdown.core.document.numbering.NumberingFormat
import com.quarkdown.core.rendering.tag.buildTag
import com.quarkdown.rendering.html.HtmlIdentifierProvider.Companion.sanitizeId
import com.quarkdown.rendering.html.HtmlTagBuilder
import com.quarkdown.rendering.html.css.asCSS

/**
 * Helper providing shared HTML-building utilities for HTML renderers.
 * @param renderer renderer this helper builds tags for
 * @param context additional information produced by the earlier stages of the pipeline
 */
class HtmlRendererHelper(
    private val renderer: BaseHtmlNodeRenderer,
    private val context: Context,
) {
    /**
     * Adds a `data-location` attribute to the location-trackable node, if its location is available.
     * The location is formatted according to the current [NumberingFormat].
     * @param builder builder of the tag to add the attribute to
     */
    fun withLocationLabel(
        builder: HtmlTagBuilder,
        node: LocationTrackableNode,
    ): HtmlTagBuilder =
        builder.optionalAttribute(
            "data-location",
            node.getLocationLabel(context)?.takeUnless { it.isEmpty() },
        )

    /**
     * Adds a `data-localized-kind` attribute to the localizable node.
     * The kind name is localized according to the current locale.
     * @param builder builder of the tag to add the attribute to
     */
    fun withLocalizedKind(
        builder: HtmlTagBuilder,
        node: LocalizedKind,
    ): HtmlTagBuilder =
        builder.optionalAttribute(
            "data-localized-kind",
            context.localizeOrNull(key = node.kindLocalizationKey),
        )

    /**
     * Resolves the position of a caption, falling back to the document's default position.
     * @param positionProvider position of the caption relative to the content
     */
    fun resolveCaptionPosition(positionProvider: CaptionPositionInfo.() -> CaptionPosition?): CaptionPosition =
        context.documentInfo.layout.captionPosition
            .getOrDefault(positionProvider)

    /**
     * Retrieves the location-based label of the [node], displays an optional caption preceded by the label, and also applies the label as its ID.
     * The label is pre-formatted according to the current [NumberingFormat].
     *
     * At the end, thanks to injected CSS variables, the visible outcome is `<localized_kind> <label>: <caption>`.
     *
     * @param builder builder of the tag hosting the caption
     * @param node node to display the caption, and apply the ID, for
     * @param position position of the caption relative to the content
     * @param captionTagName tag name of the caption element. E.g. "figcaption" for figures, "caption" for tables
     * @param idPrefix prefix for the ID. For instance, the prefix `figure` lets the ID be `figure-X.Y`, where `X.Y` is the label.
     * @see CaptionableNode
     * @see getLocationLabel to retrieve the numbered label
     */
    fun <T> numberedCaption(
        builder: HtmlTagBuilder,
        node: T,
        position: CaptionPosition,
        captionTagName: String = "figcaption",
        idPrefix: String = node.kindLocalizationKey,
    ): HtmlTagBuilder where T : CaptionableNode, T : LocationTrackableNode, T : LocalizedKind =
        builder.apply {
            // The reference ID or label is set as the ID of the element, allowing cross-references to link to it.
            val label = node.getLocationLabel(context)
            val id =
                (node as? CrossReferenceableNode)
                    ?.linkableReferenceId
                    ?.let(::sanitizeId)
                    ?: label?.let { "$idPrefix-$it" }
            id?.let { optionalAttribute("id", it) }

            if (node.caption == null && label == null) {
                // No caption and no label: nothing to show.
                return@apply
            }

            // The caption is appended as rendered content rather than as a sub-tag,
            // since sub-tags are rendered before text content, which would break
            // the position of bottom captions relative to their sibling content.
            +renderer.buildTag(captionTagName) {
                className("caption-${position.asCSS}")
                withLocationLabel(this, node)
                withLocalizedKind(this, node)

                node.caption?.let { +it }
            }
        }

    /**
     * Appends the given [content] along with the numbered caption of [node] (via [numberedCaption]),
     * ordered according to the caption position.
     * @param builder builder of the tag hosting the captioned content
     * @param node node to display the caption for
     * @param positionProvider position of the caption relative to the content
     * @param content builder of the captioned content
     */
    fun <T> captionedContent(
        builder: HtmlTagBuilder,
        node: T,
        positionProvider: CaptionPositionInfo.() -> CaptionPosition?,
        content: HtmlTagBuilder.() -> Unit,
    ): HtmlTagBuilder where T : CaptionableNode, T : LocationTrackableNode, T : LocalizedKind =
        builder.apply {
            val position = resolveCaptionPosition(positionProvider)

            if (position == CaptionPosition.TOP) numberedCaption(this, node, position)
            content()
            if (position == CaptionPosition.BOTTOM) numberedCaption(this, node, position)
        }
}
