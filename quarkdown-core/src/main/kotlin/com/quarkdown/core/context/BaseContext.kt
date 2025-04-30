package com.quarkdown.core.context

import com.quarkdown.core.ast.attributes.AstAttributes
import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.ReferenceLink
import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.flavor.MarkdownFlavor
import com.quarkdown.core.function.Function
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.call.UncheckedFunctionCall
import com.quarkdown.core.function.library.Library
import com.quarkdown.core.localization.LocaleNotSetException
import com.quarkdown.core.localization.LocalizationKeyNotFoundException
import com.quarkdown.core.localization.LocalizationLocaleNotFoundException
import com.quarkdown.core.localization.LocalizationTable
import com.quarkdown.core.localization.LocalizationTableNotFoundException
import com.quarkdown.core.media.storage.MutableMediaStorage
import com.quarkdown.core.media.storage.ReadOnlyMediaStorage
import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.Pipelines
import com.quarkdown.core.util.toPlainText

/**
 * An immutable [Context] implementation.
 * This might be used in tests as a toy context, but in a concrete execution, its mutable subclass [MutableContext] is used.
 * @param attributes attributes of the node tree, produced by the parsing stage
 * @param flavor Markdown flavor used for this pipeline. It specifies how to produce the needed components
 * @param libraries loaded libraries to look up functions from
 */
open class BaseContext(
    override val attributes: AstAttributes,
    override val flavor: MarkdownFlavor,
    override val libraries: Set<Library> = emptySet(),
) : Context {
    override val attachedPipeline: Pipeline?
        get() = Pipelines.getAttachedPipeline(this)

    override val documentInfo = DocumentInfo()

    override val options: ContextOptions = MutableContextOptions()

    override val loadableLibraries = emptySet<Library>()

    override val localizationTables = emptyMap<String, LocalizationTable>()

    override val mediaStorage: ReadOnlyMediaStorage by lazy { MutableMediaStorage(options) }

    override fun getFunctionByName(name: String): Function<*>? {
        return libraries.asSequence()
            .flatMap { it.functions }
            .find { it.name == name }
    }

    override fun resolve(reference: ReferenceLink): LinkNode? {
        return attributes.linkDefinitions.firstOrNull { it.label.toPlainText() == reference.reference.toPlainText() }
            ?.let { Link(reference.label, it.url, it.title) }
            ?.also { link ->
                reference.onResolve.forEach { action -> action(link) }
            }
    }

    override fun resolve(call: FunctionCallNode): FunctionCall<*>? {
        val function = getFunctionByName(call.name)

        return function?.let {
            FunctionCall(
                it,
                call.arguments,
                context = this,
                sourceNode = call,
            )
        }
    }

    override fun resolveUnchecked(call: FunctionCallNode): UncheckedFunctionCall<*> {
        return UncheckedFunctionCall(call.name) { resolve(call) }
    }

    override fun localize(
        tableName: String,
        key: String,
    ): String {
        val locale = documentInfo.locale ?: throw LocaleNotSetException()
        val table = localizationTables[tableName] ?: throw LocalizationTableNotFoundException(tableName)
        val entries = table[locale] ?: throw LocalizationLocaleNotFoundException(tableName, locale)
        return entries[key] ?: throw LocalizationKeyNotFoundException(tableName, locale, key)
    }

    override fun fork(): ScopeContext = ScopeContext(parent = this)
}
