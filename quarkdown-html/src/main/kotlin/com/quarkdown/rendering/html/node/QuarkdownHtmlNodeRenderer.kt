package com.quarkdown.rendering.html.node

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.id.Identifiable
import com.quarkdown.core.ast.attributes.id.getId
import com.quarkdown.core.ast.attributes.localization.LocalizedKind
import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.attributes.location.getLocationLabel
import com.quarkdown.core.ast.attributes.reference.getDefinition
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.ast.base.block.BlockQuote
import com.quarkdown.core.ast.base.block.Code
import com.quarkdown.core.ast.base.block.Heading
import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.base.inline.CodeSpan
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.dsl.buildInline
import com.quarkdown.core.ast.quarkdown.CaptionableNode
import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyCitation
import com.quarkdown.core.ast.quarkdown.bibliography.BibliographyView
import com.quarkdown.core.ast.quarkdown.block.Box
import com.quarkdown.core.ast.quarkdown.block.Clipped
import com.quarkdown.core.ast.quarkdown.block.Collapse
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.ast.quarkdown.block.Figure
import com.quarkdown.core.ast.quarkdown.block.FullColumnSpan
import com.quarkdown.core.ast.quarkdown.block.Landscape
import com.quarkdown.core.ast.quarkdown.block.Math
import com.quarkdown.core.ast.quarkdown.block.MermaidDiagram
import com.quarkdown.core.ast.quarkdown.block.Numbered
import com.quarkdown.core.ast.quarkdown.block.PageBreak
import com.quarkdown.core.ast.quarkdown.block.SlidesFragment
import com.quarkdown.core.ast.quarkdown.block.SlidesSpeakerNote
import com.quarkdown.core.ast.quarkdown.block.Stacked
import com.quarkdown.core.ast.quarkdown.block.SubdocumentGraph
import com.quarkdown.core.ast.quarkdown.block.list.FocusListItemVariant
import com.quarkdown.core.ast.quarkdown.block.list.LocationTargetListItemVariant
import com.quarkdown.core.ast.quarkdown.block.toc.TableOfContentsView
import com.quarkdown.core.ast.quarkdown.block.toc.convertToListNode
import com.quarkdown.core.ast.quarkdown.inline.InlineCollapse
import com.quarkdown.core.ast.quarkdown.inline.LastHeading
import com.quarkdown.core.ast.quarkdown.inline.MathSpan
import com.quarkdown.core.ast.quarkdown.inline.PageCounter
import com.quarkdown.core.ast.quarkdown.inline.TextTransform
import com.quarkdown.core.ast.quarkdown.inline.TextTransformData
import com.quarkdown.core.ast.quarkdown.inline.Whitespace
import com.quarkdown.core.ast.quarkdown.invisible.PageMarginContentInitializer
import com.quarkdown.core.ast.quarkdown.invisible.SlidesConfigurationInitializer
import com.quarkdown.core.ast.quarkdown.reference.CrossReference
import com.quarkdown.core.ast.quarkdown.reference.CrossReferenceableNode
import com.quarkdown.core.bibliography.BibliographyEntry
import com.quarkdown.core.bibliography.style.getContent
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.localization.localizeOrNull
import com.quarkdown.core.context.shouldAutoPageBreak
import com.quarkdown.core.context.toc.TableOfContents
import com.quarkdown.core.document.layout.caption.CaptionPosition
import com.quarkdown.core.document.layout.caption.CaptionPositionInfo
import com.quarkdown.core.document.numbering.NumberingFormat
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.rendering.tag.buildMultiTag
import com.quarkdown.core.rendering.tag.buildTag
import com.quarkdown.core.rendering.tag.tagBuilder
import com.quarkdown.core.util.flattenedChildren
import com.quarkdown.rendering.html.HtmlIdentifierProvider
import com.quarkdown.rendering.html.HtmlTagBuilder
import com.quarkdown.rendering.html.css.CssBuilder
import com.quarkdown.rendering.html.css.asCSS

/**
 * A renderer for Quarkdown ([com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor]) nodes that exports their content into valid HTML code.
 * @param context additional information produced by the earlier stages of the pipeline
 */
class QuarkdownHtmlNodeRenderer(
    context: Context,
) : BaseHtmlNodeRenderer(context) {
    /**
     * A `<div class="styleClass">...</div>` tag.
     */
    private fun div(
        styleClass: String? = null,
        init: HtmlTagBuilder.() -> Unit,
    ) = tagBuilder("div", init = init)
        .className(styleClass)
        .build()

    /**
     * A `<div class="styleClass">children</div>` tag.
     */
    private fun div(
        styleClass: String,
        children: List<Node>,
    ) = div(styleClass) { +children }

    /**
     * Adds a `data-location` attribute to the location-trackable node, if its location is available.
     * The location is formatted according to [format].
     */
    private fun HtmlTagBuilder.withLocationLabel(node: LocationTrackableNode) =
        optionalAttribute(
            "data-location",
            node.getLocationLabel(context)?.takeUnless { it.isEmpty() },
        )

    /**
     * Adds a `data-localized-kind` attribute to the localizable node.
     * The kind name is localized according to the current locale.
     */
    private fun HtmlTagBuilder.withLocalizedKind(node: LocalizedKind) =
        optionalAttribute(
            "data-localized-kind",
            context.localizeOrNull(key = node.kindLocalizationKey),
        )

    /**
     * Retrieves the location-based label of the [node], displays an optional caption preceded by the label, and also applies the label as its ID.
     * The label is pre-formatted according to the current [NumberingFormat].
     *
     * At the end, thanks to injected CSS variables, the visible outcome is `<localized_kind> <label>: <caption>`.
     *
     * @param node node to display the caption, and apply the ID, for
     * @param captionTagName tag name of the caption element. E.g. "figcaption" for figures, "caption" for tables
     * @param idPrefix prefix for the ID. For instance, the prefix `figure` lets the ID be `figure-X.Y`, where `X.Y` is the label.
     * @param positionProvider position of the caption relative to the content
     * @param requiresCaption whether [CaptionableNode.caption] is explictly required to show the element.
     *                        If `true` and no caption is set, nothing is shown
     * @see CaptionableNode
     * @see getLocationLabel to retrieve the numbered label
     */
    private fun <T> HtmlTagBuilder.numberedCaption(
        node: T,
        captionTagName: String = "figcaption",
        idPrefix: String = node.kindLocalizationKey,
        positionProvider: CaptionPositionInfo.() -> CaptionPosition?,
    ): HtmlTagBuilder where T : CaptionableNode, T : LocationTrackableNode, T : LocalizedKind =
        this.apply {
            val position =
                context.documentInfo.layout.captionPosition
                    .getOrDefault(positionProvider)

            // The label is set as the ID of the element.
            val label = node.getLocationLabel(context)
            label?.let { optionalAttribute("id", "$idPrefix-$it") }

            if (node.caption == null && label == null) {
                // No caption and no label: nothing to show.
                return@apply
            }

            +buildTag(captionTagName) {
                className("caption-${position.asCSS}")
                withLocationLabel(node)
                withLocalizedKind(node)

                node.caption?.let { +escapeCriticalContent(it) }
            }
        }

    // Quarkdown node rendering

    // The function was already expanded by previous stages: its output nodes are stored in its children.
    override fun visit(node: FunctionCallNode): CharSequence = visit(AstRoot(node.children))

    // Block

    override fun visit(node: Figure<*>) =
        buildTag("figure") {
            +node.child
            numberedCaption(node, positionProvider = { figures })
        }

    // An empty div that acts as a page break.
    override fun visit(node: PageBreak) =
        tagBuilder("div")
            .className("page-break")
            .hidden()
            .build()

    override fun visit(node: Math) =
        buildTag("formula") {
            +node.expression
            attribute("data-block", "")
            optionalAttribute("data-location", node.getLocationLabel(context))
        }

    override fun visit(node: Container) =
        buildTag("div") {
            classNames(
                "container",
                "fullwidth".takeIf { node.fullWidth },
                "float".takeIf { node.float != null },
                node.textTransform?.size?.asCSS,
                node.className,
            )

            +node.children

            style {
                "width" value node.width
                "height" value node.height
                "color" value node.foregroundColor
                "background-color" value node.backgroundColor
                "margin" value node.margin
                "padding" value node.padding
                "border-color" value node.borderColor
                "border-width" value node.borderWidth
                "border-radius" value node.cornerRadius

                "border-style" value
                    when {
                        // If the border style is set, it is used.
                        node.borderStyle != null -> node.borderStyle
                        // If border properties are set, a normal (solid) border is used.
                        node.borderColor != null || node.borderWidth != null -> Container.BorderStyle.NORMAL
                        // No border style.
                        else -> null
                    }

                "justify-items" value node.alignment
                "text-align" value node.textAlignment
                "float" value node.float
                node.textTransform?.let { textTransform(it) }
            }
        }

    override fun visit(node: Stacked) =
        div("stack stack-${node.layout.asCSS}") {
            +node.children

            style {
                (node.layout as? Stacked.Grid)?.let {
                    // The amount of 'auto' matches the amount of columns/rows.
                    "grid-template-columns" value "auto ".repeat(it.columnCount).trimEnd()
                }

                "justify-content" value node.mainAxisAlignment
                "align-items" value node.crossAxisAlignment
                "gap" value node.gap
            }
        }

    override fun visit(node: Numbered) =
        buildMultiTag {
            +node.children
        }

    override fun visit(node: Landscape) = div("landscape", node.children)

    override fun visit(node: FullColumnSpan) = div("full-column-span", node.children)

    override fun visit(node: Clipped) =
        div("clip clip-${node.clip.asCSS}") {
            +Container(children = node.children)
        }

    override fun visit(node: Box) =
        div {
            classNames("box", node.type.asCSS)

            if (node.title != null) {
                tag("header") {
                    tag("h4", node.title!!)

                    style {
                        "color" value node.foregroundColor // Must be repeated to force override.
                        "padding" value node.padding
                    }
                }
            }

            // Box actual content.
            +div("box-content") {
                +node.children

                style { "padding" value node.padding }
            }

            // Box style. Padding is applied separately to the header and the content.
            style {
                "background-color" value node.backgroundColor
                "color" value node.foregroundColor
            }
        }

    override fun visit(node: Collapse) =
        buildTag("details") {
            if (node.isOpen) {
                attribute("open", "")
            }

            tag("summary") { +node.title }
            +node.children
        }

    override fun visit(node: Whitespace) =
        // If at least one of the dimensions is set, the square will have a fixed size.
        // Otherwise, a blank character is rendered.
        when {
            node.width == null && node.height == null -> {
                buildTag("span", "&nbsp;")
            }

            else -> {
                buildTag("div") {
                    style {
                        "width" value node.width
                        "height" value node.height
                    }
                }
            }
        }

    override fun visit(node: TableOfContentsView): CharSequence {
        val tableOfContents = context.attributes.tableOfContents ?: return ""

        // Filter items based on whether to include unnumbered headings.
        var filteredItems =
            if (node.includeUnnumbered) {
                tableOfContents.items
            } else {
                // Only include items with numbered headings (canTrackLocation == true).
                tableOfContents.items.filter { (it.target as? Heading)?.canTrackLocation != false }
            }

        // Optionally include Bibliography as a ToC item if present in the document.
        if (node.includeBibliography) {
            // Find the first BibliographyView node in the AST to sync title and id.
            val root = context.attributes.root
            val flat = root?.flattenedChildren()
            val bibliographyView = flat?.firstOrNull { it is BibliographyView } as BibliographyView?

            if (bibliographyView != null) {
                // Use the actual rendered title inline content if provided, else fallback to localized default.
                val bibliographyTitleInline =
                    bibliographyView.title
                        ?: context
                            .localizeOrNull(key = "bibliography")
                            ?.let {
                                buildInline { text(it) }
                            }

                if (bibliographyTitleInline != null) {
                    val bibliographyHeading =
                        Heading(
                            depth = 1,
                            text = bibliographyTitleInline,
                            isDecorative = bibliographyView.isTitleDecorative,
                        )

                    // Avoid duplicates: compute anchor id and skip if an item with same anchor already exists.
                    val provider = HtmlIdentifierProvider.of(this)
                    val bibliographyId = provider.getId(bibliographyHeading)
                    val existingIds = filteredItems.mapNotNull { (it.target as? Identifiable)?.accept(provider) }.toSet()

                    if (bibliographyId !in existingIds) {
                        // Insert the bibliography item according to document order.
                        // Compare the index of the BibliographyView with the indices of the ToC item targets (Heading) in the flattened AST.
                        val biblioIndex = flat?.indexOf(bibliographyView) ?: -1

                        // Pair each item with its AST index; if not found, keep Int.MAX_VALUE to push it to the end.
                        val itemsWithIndex =
                            filteredItems.map { item ->
                                val idx =
                                    (item.target as? Node)
                                        ?.let { t -> flat?.indexOf(t) }
                                        ?: Int.MAX_VALUE
                                item to idx
                            }

                        // Find the first item that appears after the bibliography in the document.
                        val insertionPos = itemsWithIndex.indexOfFirst { (_, idx) -> idx > biblioIndex }

                        val bibliographyItem = TableOfContents.Item(bibliographyHeading)
                        filteredItems =
                            if (insertionPos == -1) {
                                // No later item found: append at the end.
                                filteredItems + bibliographyItem
                            } else {
                                // Insert before the first later item.
                                val before = filteredItems.subList(0, insertionPos)
                                val after = filteredItems.subList(insertionPos, filteredItems.size)
                                before + bibliographyItem + after
                            }
                    }
                }
            }
        }

        return buildMultiTag {
            // Localized title.
            val titleText = context.localizeOrNull(key = "tableofcontents")

            // Title heading. Its content is either the node's user-set title or a default localized one.
            +Heading(
                depth = 1,
                text = node.title ?: buildInline { titleText?.let { text(it) } },
                customId = "table-of-contents",
            )

            // Content.
            +buildTag("nav") {
                +node.convertToListNode(
                    this@QuarkdownHtmlNodeRenderer,
                    filteredItems,
                    linkUrlMapper = { item ->
                        "#" + HtmlIdentifierProvider.of(this@QuarkdownHtmlNodeRenderer).getId(item.target)
                    },
                )
            }
        }
    }

    override fun visit(node: BibliographyView) =
        buildMultiTag {
            // Localized title.
            val titleText = context.localizeOrNull(key = "bibliography")

            // Title heading. Its content is either the node's user-set title or a default localized one.
            val title = node.title ?: titleText?.let { buildInline { text(it) } }
            title?.let {
                val heading =
                    Heading(
                        depth = 1,
                        text = it,
                        isDecorative = node.isTitleDecorative,
                    )
                +heading
            }

            // Content.
            +buildTag("div") {
                classNames("bibliography", "bibliography-${node.style.name}")
                node.bibliography.entries.values.mapIndexed { index, entry ->
                    tag("span") {
                        className("bibliography-entry-label")
                        +node.style.labelProvider.getLabel(entry, index)
                    }
                    tag("span") {
                        className("bibliography-entry-content")
                        +node.style.contentProvider.getContent(entry)
                    }
                }
            }
        }

    override fun visit(node: MermaidDiagram) =
        buildTag("pre") {
            classNames("mermaid", "fill-height")
            +escapeCriticalContent(node.code)
        }

    override fun visit(node: SubdocumentGraph): CharSequence {
        fun id(subdocument: Subdocument) = subdocument.name.hashCode()

        val content =
            "graph LR\n" +
                context.subdocumentGraph.edges.joinToString("\n") { edge ->
                    val from = edge.first
                    val to = edge.second
                    val (idFrom, idTo) = id(from) to id(to)
                    val (nameFrom, nameTo) = from.name to to.name

                    "$idFrom[\"$nameFrom\"] --> $idTo[\"$nameTo\"]"
                }

        return MermaidDiagram(content).accept(this)
    }

    // Inline

    override fun visit(node: MathSpan) = buildTag("formula", node.expression)

    override fun visit(node: CrossReference): CharSequence {
        val definition: CrossReferenceableNode = node.getDefinition(context) ?: return Text("[???]").accept(this)

        // The target node could have an ID. If so, the reference is a link to that node.
        val anchorId = (definition as? Identifiable)?.accept(HtmlIdentifierProvider.of(this))

        val reference =
            buildTag("span") {
                className("cross-reference")

                when (definition) {
                    is LocationTrackableNode if definition.getLocationLabel(context) != null ->
                        withLocationLabel(definition)
                    // If no label is available, use the caption if possible.
                    is CaptionableNode if definition.caption != null -> +definition.caption!!
                    // Fallback: use the target's text if possible.
                    is TextNode -> +definition.text
                    // Fallback: raw reference ID.
                    else -> +node.referenceId
                }
                if (definition is LocalizedKind) {
                    withLocalizedKind(definition)
                }
            }

        return when (anchorId) {
            null -> reference // No linkable ID.
            else ->
                buildTag("a") {
                    // ID available: link to the target.
                    attribute("href", "#$anchorId")
                    +reference
                }
        }
    }

    override fun visit(node: BibliographyCitation): CharSequence {
        val (entry: BibliographyEntry, view: BibliographyView) =
            node.getDefinition(context) ?: return Text("[???]").accept(this)

        val index = view.bibliography.indexOf(entry)
        val label = view.style.labelProvider.getLabel(entry, index)
        return Text(label).accept(this)
    }

    override fun visit(node: SlidesFragment) =
        tagBuilder("div", node.children)
            .classNames("fragment", node.behavior.asCSS)
            .build()

    override fun visit(node: SlidesSpeakerNote) =
        buildTag("aside") {
            className("notes")
            hidden()
            +node.children
        }

    /**
     * Applies the text transformation of [data] into [this] CSS builder.
     */
    private fun CssBuilder.textTransform(data: TextTransformData) {
        "font-weight" value data.weight
        "font-style" value data.style
        "font-variant" value data.variant
        "text-decoration" value data.decoration
        "text-transform" value data.case
        "color" value data.color
    }

    override fun visit(node: TextTransform) =
        buildTag("span") {
            classNames(
                node.data.size?.asCSS, // e.g. 'size-small' class
                node.className,
            )
            +node.children
            style { textTransform(node.data) }
        }

    override fun visit(node: InlineCollapse) =
        buildTag("span") {
            // Dynamic behavior is handled by JS.
            className("inline-collapse")
            attribute("data-full-text", buildMultiTag { +node.text })
            attribute("data-collapsed-text", buildMultiTag { +node.placeholder })
            attribute("data-collapsed", !node.isOpen)
            +if (node.isOpen) node.text else node.placeholder
        }

    // Invisible nodes

    override fun visit(node: PageMarginContentInitializer) =
        // In slides and paged documents, these elements are copied to each page in post-processing.
        buildTag("div") {
            classNames(
                "page-margin-content",
                "page-margin-${node.position.asCSS}",
            )
            attribute("data-on-left-page", node.position.forLeftPage.asCSS)
            attribute("data-on-right-page", node.position.forRightPage.asCSS)
            +node.children
        }

    override fun visit(node: PageCounter) =
        // The current or total page number.
        // The actual number is filled by a script at runtime
        // (either slides.js or paged.js, depending on the document type).
        buildTag("span") {
            +"-" // The default placeholder in case it is not filled by a script (e.g. plain documents).
            className(
                when (node.target) {
                    PageCounter.Target.CURRENT -> "current-page-number"
                    PageCounter.Target.TOTAL -> "total-page-number"
                },
            )
        }

    override fun visit(node: LastHeading) =
        buildTag("span") {
            // Since pagination is performed at runtime, the last heading must be retrieved at runtime as well.
            className("last-heading")
            attribute("data-depth", node.depth)
        }

    override fun visit(node: SlidesConfigurationInitializer): CharSequence =
        buildTag("script") {
            hidden()
            // Injects properties that are read at runtime after the document is loaded.
            +buildString {
                append("window.slidesConfig = {")
                node.centerVertically?.let {
                    append("center: $it,")
                }
                node.showControls?.let {
                    append("showControls: $it,")
                }
                node.showNotes?.let {
                    append("showNotes: $it,")
                }
                node.transition?.let {
                    append("transitionStyle: '${it.style.asCSS}',")
                    append("transitionSpeed: '${it.speed.asCSS}',")
                }
                append("};")
            }
        }

    // Additional behavior of base nodes

    // On top of the default behavior, an anchor ID is set,
    // and it could force an automatic page break if suitable.
    override fun visit(node: Heading): String {
        val tagBuilder =
            when {
                // When a heading has a depth of 0 (achievable only via functions), it is an invisible marker with an ID.
                node.isMarker ->
                    tagBuilder("div") {
                        className("marker")
                        hidden()
                    }
                // Regular headings.
                else -> tagBuilder("h${node.depth}", node.text)
            }

        // The heading tag itself.
        val tag =
            tagBuilder
                .optionalAttribute(
                    "id",
                    // Generate an automatic identifier if allowed by settings.
                    HtmlIdentifierProvider
                        .of(renderer = this)
                        .takeIf { context.options.enableAutomaticIdentifiers || node.customId != null }
                        ?.getId(node),
                ).optionalAttribute("data-decorative", "".takeIf { node.isDecorative })
                .withLocationLabel(node)
                .build()

        return buildMultiTag {
            if (context.shouldAutoPageBreak(node)) {
                +PageBreak()
            }
            +tag
        }
    }

    // On top of the base behavior, a blockquote can have a type and an attribution.
    override fun visit(node: BlockQuote) =
        buildTag("blockquote") {
            // If the quote has a type (e.g. TIP),
            // the whole quote is marked as a 'tip' blockquote
            // and a localized label is shown (e.g. 'Tip:' for English).
            node.type?.asCSS?.let { type ->
                className(type)
                // The type is associated to a localized label
                // only if the documant language is set and the set language is supported.
                context.localizeOrNull(key = type)?.let { localizedLabel ->
                    // The localized label is set as a CSS variable.
                    // Themes can customize label appearance and formatting.
                    style { "--quote-type-label" value "'$localizedLabel'" }
                    // The quote is marked as labeled to allow further customization.
                    attribute("data-labeled", "")
                }
            }

            +node.children
            node.attribution?.let {
                +tagBuilder("p", it)
                    .className("attribution")
                    .build()
            }
        }

    // Quarkdown introduces table captions, also numerated.
    override fun visit(node: Table) =
        super
            .tableBuilder(node)
            .apply {
                numberedCaption(
                    node,
                    captionTagName = "caption",
                    positionProvider = { tables },
                )
            }.build()

    override fun visit(node: Code): String {
        val block = super.visit(node)

        // If the code is numbered or has a caption, it is wrapped in a figure.
        if (node.caption == null && node.getLocationLabel(context) == null) {
            return block
        }
        return buildTag("figure") {
            +block
            numberedCaption(node, positionProvider = { codeBlocks })
        }
    }

    // A code span can contain additional content, such as a color preview.
    override fun visit(node: CodeSpan): String {
        val codeTag = super.visit(node)

        // The code is wrapped to allow additional content.
        return buildTag("span") {
            className("codespan-content")

            +codeTag

            when (val content = node.content) {
                null -> {} // No additional content.
                is CodeSpan.ColorContent -> {
                    // If the code contains a color code, show the color preview.
                    +buildTag("span") {
                        style { "background-color" value content.color }
                        className("color-preview")
                    }
                }
            }
        }
    }

    // List item variants.

    override fun visit(variant: FocusListItemVariant): HtmlTagBuilder.() -> Unit =
        {
            if (variant.isFocused) {
                className("focused")
            }
        }

    override fun visit(variant: LocationTargetListItemVariant): HtmlTagBuilder.() -> Unit = { withLocationLabel(variant.target) }
}
