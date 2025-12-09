package com.quarkdown.core.context

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.AstAttributes
import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.ReferenceImage
import com.quarkdown.core.ast.base.inline.ReferenceLink
import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.context.file.FileSystem
import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.flavor.MarkdownFlavor
import com.quarkdown.core.function.Function
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.call.UncheckedFunctionCall
import com.quarkdown.core.function.library.Library
import com.quarkdown.core.graph.Graph
import com.quarkdown.core.localization.Locale
import com.quarkdown.core.localization.LocaleNotSetException
import com.quarkdown.core.localization.LocalizationTables
import com.quarkdown.core.media.storage.ReadOnlyMediaStorage
import com.quarkdown.core.pipeline.Pipeline

/**
 * Container of information about the current state of the pipeline, shared across the whole pipeline itself.
 */
interface Context {
    /**
     * The Markdown flavor in use.
     */
    val flavor: MarkdownFlavor

    /**
     * The pipeline this context is attached to, if it exists.
     * A context can have up to 1 attached pipeline.
     * @see com.quarkdown.core.pipeline.Pipelines.getAttachedPipeline
     * @see com.quarkdown.core.pipeline.Pipelines.attach
     */
    val attachedPipeline: Pipeline?

    /**
     * Mutable information about the final document that is being created.
     */
    val documentInfo: DocumentInfo

    /**
     * Global properties that affect several behaviors
     * and that can be altered through function calls.
     */
    val options: ContextOptions

    /**
     * Information about the node tree that is being processed by the [attachedPipeline].
     */
    val attributes: AstAttributes

    /**
     * Loaded libraries to look up functions from.
     */
    val libraries: Set<Library>

    /**
     * External libraries that can be loaded by the user into [libraries].
     * These libraries are, for instance, fetched from the library directory (`--libs` option)
     * and can be loaded via the `.include {name}` function.
     */
    val loadableLibraries: Set<Library>

    /**
     * Tables that store key-value localization pairs for each supported locale.
     * Each table is identified by a unique name.
     * @see localize
     */
    val localizationTables: LocalizationTables

    /**
     * Media storage that contains all the media files that are referenced within the document.
     * For example, if an image node references a local image file "image.png",
     * the local file needs to be exported to the output directory in order for a browser to look it up.
     * This storage is used to keep track of all the media files that may need to be exported.
     * @see com.quarkdown.core.context.hooks.MediaStorerHook
     */
    val mediaStorage: ReadOnlyMediaStorage

    /**
     * The subdocument that is being processed by this context.
     * A subdocument can be the root one or another referenced by a link.
     */
    val subdocument: Subdocument

    /**
     * Directed graph of the subdocuments that are part of the document complex.
     * Each subdocument is a separate document file that can be rendered independently,
     * and is referenced by a link from the main document or another subdocument.
     */
    val subdocumentGraph: Graph<Subdocument>

    /**
     * The file system relative to this context
     * which can be used to access files starting from a certain working directory.
     */
    val fileSystem: FileSystem

    /**
     * Looks up a function by name.
     * @param name name of the function to look up, case-sensitive
     * @return the corresponding function, if it exists
     */
    fun getFunctionByName(name: String): Function<*>?

    /**
     * Tries to resolve a reference link to an actual link.
     * If the resolution succeeds, [ReferenceLink.onResolve] callbacks are executed.
     * @param reference reference link to lookup
     * @return the corresponding link node, if it exists
     */
    fun resolve(reference: ReferenceLink): LinkNode?

    /**
     * @param call function call node to get a function call from
     * @return a new function call that [call] references to, with [call]'s arguments,
     * or `null` if [call] references to an unknown function
     */
    fun resolve(call: FunctionCallNode): FunctionCall<*>?

    /**
     * @param call function call node to get a function call from
     * @return an [UncheckedFunctionCall] that wraps the referenced function call if it has been resolved.
     * Calling `execute()` on an [UncheckedFunctionCall] whose function isn't resolved throws an exception
     * @see UncheckedFunctionCall
     * @see resolve
     */
    fun resolveUnchecked(call: FunctionCallNode): UncheckedFunctionCall<*>

    /**
     * Generates a new UUID via [ContextOptions.uuidSupplier].
     * @return a new UUID as a string
     */
    fun newUuid(): String = options.uuidSupplier()

    /**
     * Localizes a string to this context's language (the locale set in [documentInfo]) by looking up a key in a localization table.
     * @param tableName name of the localization table, which must exist within [localizationTables]
     * @param key localization key to look up within the table
     * @param locale the locale to use for localization, defaulting to the one set in [documentInfo], if any
     * @return the localized string corresponding to the key in the table, if there is any
     * @throws com.quarkdown.core.localization.LocaleNotSetException if [locale] is not explicitly set and a locale is not set within [documentInfo]
     * @throws com.quarkdown.core.localization.LocalizationTableNotFoundException if the table does not exist
     * @throws com.quarkdown.core.localization.LocalizationKeyNotFoundException if the locale does not exist in the table
     * @throws com.quarkdown.core.localization.LocalizationKeyNotFoundException if the key does not exist in the table entry for the locale
     * @see localizationTables
     */
    fun localize(
        tableName: String,
        key: String,
        locale: Locale = documentInfo.locale ?: throw LocaleNotSetException(),
    ): String

    /**
     * @return a new scope context, forked from this context, that shares several base properties
     */
    fun fork(): ScopeContext
}

/**
 * @param reference reference link to lookup
 * @return the corresponding looked up link node if it exists, its fallback node otherwise
 */
fun Context.resolveOrFallback(reference: ReferenceLink): Node = resolve(reference) ?: reference.fallback()

/**
 * @param reference reference image to lookup
 * @return the corresponding looked up image node if it exists, its fallback node otherwise
 */
fun Context.resolveOrFallback(reference: ReferenceImage): Node =
    resolve(reference.link)
        ?.let { Image(it, reference.width, reference.height, reference.referenceId) }
        ?: reference.link.fallback()
