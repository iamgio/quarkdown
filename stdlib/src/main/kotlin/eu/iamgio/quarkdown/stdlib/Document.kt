package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.ast.quarkdown.PageCounterInitializer
import eu.iamgio.quarkdown.ast.quarkdown.PageMarginContentInitializer
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.document.DocumentInfo
import eu.iamgio.quarkdown.document.DocumentTheme
import eu.iamgio.quarkdown.document.DocumentType
import eu.iamgio.quarkdown.document.page.PageMarginPosition
import eu.iamgio.quarkdown.document.page.PageSizeFormat
import eu.iamgio.quarkdown.document.page.Size
import eu.iamgio.quarkdown.document.page.Sizes
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.reflect.Name
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.VoidValue
import eu.iamgio.quarkdown.function.value.data.Lambda
import eu.iamgio.quarkdown.function.value.wrappedAsValue
import eu.iamgio.quarkdown.pipeline.error.IOPipelineException

/**
 * `Document` stdlib module exporter.
 * This module handles document information and details.
 * @see eu.iamgio.quarkdown.document.DocumentInfo
 */
val Document: Module =
    setOf(
        ::docType,
        ::docName,
        ::docAuthor,
        ::theme,
        ::pageFormat,
        ::pageMarginContent,
        ::footer,
        ::pageCounter,
    )

/**
 * If [value] is not `null`, it updates document information (according to [set]).
 * Document information is fetched from [this] context via [Context.documentInfo].
 * If it's `null`, the needed value (according to [get]) from the current document is returned.
 * @param value (optional) value to assign to a document info field
 * @return the result of [get], wrapped in a [StringValue], if [value] is `null`. [VoidValue] otherwise
 */
private fun <T> Context.modifyOrEchoDocumentInfo(
    value: T?,
    get: DocumentInfo.() -> String,
    set: DocumentInfo.(T) -> Unit,
): OutputValue<*> {
    if (value == null) {
        return get(this.documentInfo).wrappedAsValue()
    }

    set(this.documentInfo, value)
    return VoidValue
}

/**
 * If [type] is not `null`, it sets the document type to its value.
 * The document type affects its final output style.
 * If it's `null`, the name of the current document type is returned.
 * @param type (optional) type to assign to the document
 * @return the lowercase name of the current document type if [type] is `null`
 */
@Name("doctype")
fun docType(
    @Injected context: Context,
    type: DocumentType? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        type,
        get = { this.type.name.lowercase() },
        set = { this.type = it },
    )

/**
 * If [name] is not `null`, it sets the document name to its value.
 * If it's `null`, the current document name is returned.
 * @param name (optional) name to assign to the document
 * @return the current document name if [name] is `null`
 */
@Name("docname")
fun docName(
    @Injected context: Context,
    name: String? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        name,
        get = { this.name ?: "" },
        set = { this.name = it },
    )

/**
 * If [author] is not `null`, it sets the document author to its value.
 * If it's `null`, the current document author is returned.
 * @param author (optional) author to assign to the document
 * @return the current document author if [author] is `null`
 */
@Name("docauthor")
fun docAuthor(
    @Injected context: Context,
    author: String? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        author,
        get = { this.author ?: "" },
        set = { this.author = it },
    )

/**
 * Sets the global document theme.
 * @param color (optional) color scheme to assign (searched in `resources/render/theme/color`)
 * @param layout (optional) layout format to assign (searched in `resources/render/theme/layout`)
 * @throws IOPipelineException if any of the theme components isn't resolved
 */
fun theme(
    @Injected context: Context,
    color: String? = null,
    layout: String? = null,
): VoidValue {
    /**
     * @throws IOPipelineException if [theme] is not a valid theme
     */
    fun checkExistance(theme: String) {
        object {}.javaClass.getResource("/render/theme/${theme.lowercase()}.css")
            ?: throw IOPipelineException("Theme $theme not found")
    }

    // Update global theme.
    context.documentInfo.theme =
        DocumentTheme(
            color = color?.lowercase()?.also { checkExistance("color/$it") },
            layout = layout?.lowercase()?.also { checkExistance("layout/$it") },
        )

    return VoidValue
}

/**
 * Sets the format of the document.
 * If a value is `null`, the default value supplied by the underlying renderer is used.
 * @param format standard size format of each page (overrides [width] and [height])
 * @param width width of each page
 * @param height height of each page
 * @param margin blank space around the content of each page. Only supported in paged mode.
 * @throws IllegalArgumentException if both [format] and either [width] or [height] are not `null`
 */
@Name("pageformat")
fun pageFormat(
    @Injected context: Context,
    @Name("size") format: PageSizeFormat? = null,
    width: Size? = null,
    height: Size? = null,
    margin: Sizes? = null,
): VoidValue {
    if (format != null && (width != null || height != null)) {
        throw IllegalArgumentException("Specifying a page size format overrides manual width and height")
    }

    with(context.documentInfo.pageFormat) {
        this.pageWidth = format?.width ?: width
        this.pageHeight = format?.height ?: height
        this.margin = margin
    }

    return VoidValue
}

/**
 * Displays text content on each page of a document.
 * @param position position of the content within the page
 * @param content content to be displayed on each page
 * @return a wrapped [PageMarginContentInitializer] node
 */
@Name("pagemargincontent")
fun pageMarginContent(
    position: PageMarginPosition = PageMarginPosition.TOP_CENTER,
    content: MarkdownContent,
): NodeValue =
    PageMarginContentInitializer(
        content.children,
        position,
    ).wrappedAsValue()

/**
 * Displays text content on the bottom center of each page of a document.
 * Shortcut for [pageMarginContent] with [PageMarginPosition.BOTTOM_CENTER] as its position.
 * @see pageMarginContent
 * @return a wrapped [PageMarginContentInitializer] node with its position set to bottom center
 */
fun footer(content: MarkdownContent): NodeValue =
    pageMarginContent(
        PageMarginPosition.BOTTOM_CENTER,
        content,
    )

/**
 * Sets the global page counter for a paged document.
 * @param position position of the counter within the page
 * @param text action that returns the text of the counter.
 *             Accepts two arguments: index of the current page and total amount of pages.
 *             Markdown content is not supported.
 * @return a wrapped [PageCounterInitializer] node
 */
@Name("pagecounter")
fun pageCounter(
    @Injected context: Context,
    position: PageMarginPosition = PageMarginPosition.BOTTOM_CENTER,
    text: Lambda =
        Lambda(context) { (current, total) ->
            "$current / $total"
        },
): NodeValue =
    PageCounterInitializer(
        content = { current, total ->
            val textValue =
                text.invoke<String, StringValue>(
                    StringValue(current),
                    StringValue(total),
                ).unwrappedValue

            listOf(Text(textValue))
        },
        position,
    ).wrappedAsValue()
