package com.quarkdown.core.context

import com.quarkdown.core.ast.attributes.MutableAstAttributes
import com.quarkdown.core.ast.base.block.LinkDefinition
import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.flavor.MarkdownFlavor
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.library.LibraryRegistrant
import com.quarkdown.core.graph.VisitableOnceGraph
import com.quarkdown.core.localization.MutableLocalizationTables
import com.quarkdown.core.media.storage.MutableMediaStorage

/**
 * A mutable [Context] implementation, which allows registering new data to be looked up later.
 * @param flavor Markdown flavor used for this pipeline. It specifies how to produce the needed components
 * @param libraries loaded libraries to look up functions from
 * @param attributes attributes of the node tree, which can be manipulated on demand
 * @param subdocument the subdocument this context is processing
 */
open class MutableContext(
    flavor: MarkdownFlavor,
    libraries: Set<Library> = emptySet(),
    loadableLibraries: Set<Library> = emptySet(),
    subdocument: Subdocument = Subdocument.Root,
    override val attributes: MutableAstAttributes = MutableAstAttributes(),
    override val options: MutableContextOptions = MutableContextOptions(),
) : BaseContext(attributes, flavor, libraries, subdocument) {
    override val libraries: MutableSet<Library> = super.libraries.toMutableSet()

    override var documentInfo: DocumentInfo = super.documentInfo

    override val loadableLibraries: MutableSet<Library> = (super.loadableLibraries + loadableLibraries).toMutableSet()

    override val localizationTables: MutableLocalizationTables = super.localizationTables.toMutableMap()

    override val mediaStorage: MutableMediaStorage
        get() = super.mediaStorage as MutableMediaStorage

    override var subdocumentGraph: VisitableOnceGraph<Subdocument> = super.subdocumentGraph

    // Prevents function calls from being enqueued.
    private var lockFunctionCallEnqueuing = false

    /**
     * Registers a new [LinkDefinition], which can be later looked up
     * via [resolve] to produce a concrete link from a reference.
     * @param linkDefinition definition to register
     */
    open fun register(linkDefinition: LinkDefinition) {
        attributes.linkDefinitions += linkDefinition
    }

    /**
     * Enqueues a new [FunctionCallNode], which is executed in the next stage of the pipeline.
     * Nothing happens if enqueuing is locked via [lockFunctionCallEnqueuing].
     * @param functionCall function call to register
     */
    open fun register(functionCall: FunctionCallNode) {
        if (!lockFunctionCallEnqueuing) {
            attributes.functionCalls += functionCall
        }
    }

    // This override makes sure the same function call is dequeued from the execution queue
    // after its execution is completed, so that it won't be accidentally executed again.
    // A double execution may happen if it's in the execution queue AND another function evaluates it.
    override fun resolve(call: FunctionCallNode): FunctionCall<*>? =
        super.resolve(call)?.also {
            it.onComplete = { attributes.functionCalls.remove(call) }
        }

    override fun fork(subdocument: Subdocument): ScopeContext = ScopeContext(parent = this, subdocument)

    /**
     * Loads a loadable library by name and registers it in the context.
     * After a successful load, the library is removed from [loadableLibraries] and added to [libraries],
     * with its [Library.onLoad] action executed.
     * @param name name of the library to load, case-sensitive
     * @return the loaded library, if it exists
     */
    fun loadLibrary(name: String): Library? =
        loadableLibraries.find { it.name == name }?.also {
            loadableLibraries.remove(it)
            LibraryRegistrant(this).register(it)
        }

    /**
     * Returns a copy of the queue containing registered function calls and clears the original one.
     * @return all the registered function call nodes until now
     */
    fun dequeueAllFunctionCalls(): List<FunctionCallNode> =
        attributes.functionCalls.toList().also {
            attributes.functionCalls.clear()
        }

    /**
     * Performs an action locking the enqueuing of function calls.
     * This causes [register] to do nothing until the action is completed.
     * Any function call enqueued during the action is discarded and won't be expanded by the pipeline.
     * @param block action to perform
     * @return the result of the action
     */
    fun <T> lockFunctionCallEnqueuing(block: MutableContext.() -> T): T {
        lockFunctionCallEnqueuing = true
        return block().also { lockFunctionCallEnqueuing = false }
    }
}
