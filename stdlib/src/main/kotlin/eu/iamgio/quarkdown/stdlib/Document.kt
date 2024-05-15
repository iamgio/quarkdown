package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.document.DocumentInfo
import eu.iamgio.quarkdown.document.DocumentType
import eu.iamgio.quarkdown.document.page.PageFormatInfo
import eu.iamgio.quarkdown.document.page.Sizes
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.reflect.Name
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.VoidValue
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
            javaClass.getResource("/render/quarkdown/theme/$new.css")
                ?: throw IOPipelineException("Theme $new not found")

            this.theme = new
        },
    )

@Name("pageformat")
fun pageFormat(
    @Injected context: Context,
    margin: Sizes? = null,
): VoidValue {
    context.documentInfo.pageFormat =
        PageFormatInfo(
            margin,
        )

    return VoidValue
}
