package com.quarkdown.stdlib

import com.quarkdown.core.ast.InlineMarkdownContent
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.SharedContext
import com.quarkdown.core.context.file.FileSystem
import com.quarkdown.core.context.file.SimpleFileSystem
import com.quarkdown.core.function.library.module.QuarkdownModule
import com.quarkdown.core.function.library.module.moduleOf
import com.quarkdown.core.function.reflect.annotation.Injected
import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.value.GeneralCollectionValue
import com.quarkdown.core.function.value.IterableValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.VoidValue
import com.quarkdown.core.function.value.factory.ValueFactory
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.stdlib.internal.asString
import java.io.Reader

/**
 * `Ecosystem` stdlib module exporter.
 * This module handles interaction between Quarkdown sources.
 */
val Ecosystem: QuarkdownModule =
    moduleOf(
        ::include,
        ::includeAll,
        ::subdocument,
    )

/**
 * Includes the parsed content of the given raw Quarkdown [code], read by [reader], in the current document.
 * The context of the main file is shared, allowing for sharing of variables, functions and other declarations.
 * @param context main context to share
 * @param reader reader of the raw Quarkdown source to include
 * @return the content of the file as a node
 */
internal fun includeResource(
    context: Context,
    reader: Reader,
): NodeValue {
    val code = reader.readText()

    // Evaluate the Quarkdown source.
    // This automatically converts the source into a value (e.g. a node, a string, a number, etc.)
    // and fills the current context with new declarations (e.g. variables, functions, link definitions, etc.)
    return ValueFactory.blockMarkdown(code, context).asNodeValue()
}

/**
 * This function has two behaviors:
 * - Reads a Quarkdown file and includes its parsed content in the current document.
 * - Loads a library into the current context. Loadable libraries are fetched from the library folder (`--libs` CLI option).
 *
 * The context of the main file is shared to the sub-file and vice versa, allowing for sharing of variables, functions and other declarations.
 *
 * @param path either a path (relative or absolute with extension) to the file to include, or the name of a loadable library
 * @return the content of the file as a node if a file is included, or nothing if a library is loaded
 * @throws IllegalArgumentException if the loaded Quarkdown source cannot be evaluated
 * @wiki Including other Quarkdown files
 */
fun include(
    @Injected context: MutableContext,
    path: String,
): OutputValue<*> {
    // Load library by name if it exists.
    context.loadLibrary(path)?.let { return VoidValue }

    // File lookup
    val file = file(context, path)

    // Shared context initialization with updated working directory.
    val newFileSystem: FileSystem = SimpleFileSystem(file.parentFile)
    val newContext: Context = SharedContext(context, newFileSystem)

    return includeResource(newContext, file.bufferedReader())
}

/**
 * Performs a bulk include of the given paths via [include].
 * @param paths paths to the files or library names to include
 * @return a collection containing the output of the included files
 * @throws IllegalArgumentException if any of the loaded sources cannot be evaluated
 * @see include for information about file inclusion
 * @wiki Including other Quarkdown files
 */
@Name("includeall")
fun includeAll(
    @Injected context: MutableContext,
    @LikelyBody paths: Iterable<Value<*>>,
): IterableValue<OutputValue<*>> =
    paths
        .map { include(context, it.asString()) }
        .let(::GeneralCollectionValue)

/**
 * Creates a link to a subdocument located at the given [path].
 *
 * This is an alias to the link syntax, `[Label](path)`, with more freedom:
 * - Function calls are supported, whereas the link syntax only supports static paths.
 * - The link syntax recognizes subdocuments only by their file extension (`.qd` or `.md`).
 *
 * @param path path to the subdocument
 * @param label optional label for the link.
 *              If not provided, this function will just add the subdocument to the document graph, without displaying a link.
 * @return a [SubdocumentLink] node, which may be hidden if [label] is not provided
 * @wiki Subdocuments
 */
fun subdocument(
    path: String,
    label: InlineMarkdownContent? = null,
): NodeValue =
    SubdocumentLink(
        Link(
            label = label?.children ?: emptyList(),
            url = path,
            title = null,
        ),
    ).wrappedAsValue()
