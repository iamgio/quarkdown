package com.quarkdown.core.function.library.module

import com.quarkdown.core.function.library.loader.ExportableFunction
import com.quarkdown.core.function.library.loader.MultiFunctionLibraryLoader
import com.quarkdown.core.function.reflect.KFunctionAdapter
import com.quarkdown.core.function.value.OutputValue
import kotlin.reflect.KFunction

/**
 * A subsection of Quarkdown functions that can be exported via a [MultiFunctionLibraryLoader].
 *
 * While this class might seem redundant in place of a typealias,
 * having an actual class makes it easier and more robust on Quarkdoc[^1]'s side to identify modules.
 *
 * [^1]: Quarkdoc is Quarkdown's documentation generator, based on Dokka. See the `quarkdown-quarkdoc` module.
 *
 * @param functions the functions to export in the module
 */
class QuarkdownModule(
    functions: Set<ExportableFunction>,
) : HashSet<ExportableFunction>(functions) {
    /**
     * Creates a [QuarkdownModule] that wraps multiple [QuarkdownModule]s, joining their functions into a single module.
     * The identity of the submodules is lost in the process.
     * @param modules the modules to include
     */
    constructor(vararg modules: QuarkdownModule) : this(modules.flatMap { it.asSequence() }.toSet())

    operator fun plus(other: QuarkdownModule): QuarkdownModule = QuarkdownModule(this + other)
}

/**
 * Creates a [QuarkdownModule] from a set of pre-built exportable functions.
 * @param functions the functions to export in the module
 */
fun moduleOf(vararg functions: ExportableFunction): QuarkdownModule = setOf(*functions).let(::QuarkdownModule)

/**
 * Creates a [QuarkdownModule] by adapting Kotlin [KFunction] references via [KFunctionAdapter].
 * @param functions the Kotlin function references to adapt and export
 */
fun moduleOf(vararg functions: KFunction<OutputValue<*>>): QuarkdownModule =
    functions.map { KFunctionAdapter(it) }.toSet().let(::QuarkdownModule)
