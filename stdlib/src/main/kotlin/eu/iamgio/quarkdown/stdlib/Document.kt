package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.PageCounterInitializer
import eu.iamgio.quarkdown.ast.PageMarginContentInitializer
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.document.DocumentInfo
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
 * If [theme] is not `null`, it sets the document theme to its value.
 * If it's `null`, the current document theme is returned.
 * @param theme (optional) theme to assign to the document
 * @return the current document theme if [theme] is `null`
 * @throws IOPipelineException if the theme isn't resolved
 */
@Name("theme")
fun theme(
    @Injected context: Context,
    theme: String? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        theme,
        get = { this.theme ?: "" },
        set = {
            val new = it.lowercase()

            // Existance check.
            javaClass.getResource("/render/theme/$new.css")
                ?: throw IOPipelineException("Theme $new not found")

            this.theme = new
        },
    )

/**
 * Sets the format of the document.
 * If a value is `null`, the default value supplied by the underlying renderer is used.
 * @param format standard size format of each page (overrides [width] and [height])
 * @param width width of each page
 * @param height height of each page
 * @param margin blank space around the content of each page
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
 * Displays text content on each page of a paged document.
 * @param position position of the content within the page
 * @param text text content to be displayed on each page
 * @return a wrapped [PageMarginContentInitializer] node
 */
@Name("pagemargincontent")
fun pageMarginContent(
    @Injected context: Context,
    position: PageMarginPosition = PageMarginPosition.TOP_CENTER,
    text: Lambda,
): NodeValue =
    PageMarginContentInitializer(
        text.invoke<String, StringValue>().unwrappedValue,
        position,
    ).wrappedAsValue()

/**
 * Sets the global page counter for a paged document.
 * @param position position of the counter within the page
 * @param text action that returns the text of the counter.
 *             Accepts two arguments: index of the current page and total amount of pages.
 *             Markdown content is not supported
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
        text = { current, total ->
            text.invoke<String, StringValue>(
                StringValue(current),
                StringValue(total),
            ).unwrappedValue
        },
        position,
    ).wrappedAsValue()
