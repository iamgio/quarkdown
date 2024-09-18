package eu.iamgio.quarkdown.context

import eu.iamgio.quarkdown.ast.AstAttributes
import eu.iamgio.quarkdown.ast.base.LinkNode
import eu.iamgio.quarkdown.ast.base.inline.Link
import eu.iamgio.quarkdown.ast.base.inline.ReferenceLink
import eu.iamgio.quarkdown.ast.quarkdown.FunctionCallNode
import eu.iamgio.quarkdown.document.DocumentInfo
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.function.Function
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.call.UncheckedFunctionCall
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.localization.LocaleNotSetException
import eu.iamgio.quarkdown.localization.LocalizationKeyNotFoundException
import eu.iamgio.quarkdown.localization.LocalizationLocaleNotFoundException
import eu.iamgio.quarkdown.localization.LocalizationTable
import eu.iamgio.quarkdown.localization.LocalizationTableNotFoundException
import eu.iamgio.quarkdown.media.storage.MutableMediaStorage
import eu.iamgio.quarkdown.media.storage.ReadOnlyMediaStorage
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.Pipelines

/**
 * An immutable [Context] implementation.
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

    override val localizationTables = emptyMap<String, LocalizationTable>()

    override val mediaStorage: ReadOnlyMediaStorage by lazy { MutableMediaStorage(options) }

    override fun getFunctionByName(name: String): Function<*>? {
        return libraries.asSequence()
            .flatMap { it.functions }
            .find { it.name == name }
    }

    override fun resolve(reference: ReferenceLink): LinkNode? {
        return attributes.linkDefinitions.firstOrNull { it.label == reference.reference }
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
