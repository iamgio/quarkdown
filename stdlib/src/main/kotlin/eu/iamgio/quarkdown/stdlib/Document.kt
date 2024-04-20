package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.document.DocumentInfo
import eu.iamgio.quarkdown.function.reflect.FunctionName
import eu.iamgio.quarkdown.function.reflect.Injected
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.VoidValue
import eu.iamgio.quarkdown.function.value.wrappedAsValue

/**
 * `Document` stdlib module exporter.
 * This module handles document information and details.
 * @see eu.iamgio.quarkdown.document.DocumentInfo
 */
val Document =
    setOf(
        ::docName,
        ::docAuthor,
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
 * If [name] is not `null`, it sets the document name to its value.
 * If it's `null`, the current document name is returned.
 * @param name (optional) name to assign to the document
 * @return the current document name if [name] is `null`
 */
@FunctionName("docname")
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
@FunctionName("docauthor")
fun docAuthor(
    @Injected context: Context,
    author: String? = null,
): OutputValue<*> =
    context.modifyOrEchoDocumentInfo(
        author,
        get = { this.author ?: "" },
        set = { this.author = it },
    )
