package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

/**
 * Thin reflective wrapper around a PSI element from KSP2's shaded `ksp.org.jetbrains.kotlin.*`
 * package, whose types cannot be imported from source without coupling to KSP's internal layout.
 *
 * The wrapper exposes the small set of accessors the discovery extractors actually use (text,
 * offsets, children walk, arbitrary property navigation), so the reflection contract lives in
 * one place. Any method call returning `null` (from a missing accessor, a wrong return type,
 * or a genuine null on the underlying element) is silently swallowed; callers use `?.` chains
 * and treat `null` as "no data".
 */
internal class PsiNode(
    private val target: Any,
) {
    /** Simple class name of the wrapped PSI type, e.g. `KtNameReferenceExpression`. */
    val simpleName: String get() = target.javaClass.simpleName

    /** Verbatim source text of the PSI element. */
    val text: String? get() = call(GET_TEXT) as? String

    /** File offset at which this element begins, or `null` when a `TextRange` isn't available. */
    val startOffset: Int?
        get() = asNode(GET_TEXT_RANGE)?.call(GET_START_OFFSET) as? Int

    /**
     * Yields this node and every descendant in pre-order. Suitable for walking small expression
     * trees (default expressions, annotation entries); not tuned for large PSI graphs.
     */
    fun walk(): Sequence<PsiNode> =
        sequence {
            yield(this@PsiNode)
            (call(GET_CHILDREN) as? Array<*>)?.forEach { child ->
                child?.let { yieldAll(PsiNode(it).walk()) }
            }
        }

    /** Reflectively invokes a no-arg method on the wrapped element; returns `null` on any failure. */
    fun call(method: String): Any? = runCatching { target.javaClass.getMethod(method).invoke(target) }.getOrNull()

    /** Reflectively invokes a no-arg method and wraps the result as another [PsiNode]. */
    fun asNode(method: String): PsiNode? = call(method)?.let(::PsiNode)

    /** Reflectively invokes a no-arg method and interprets the result as a list of [PsiNode]s. */
    fun asList(method: String): List<PsiNode> = (call(method) as? List<*>)?.filterNotNull()?.map(::PsiNode).orEmpty()

    private companion object {
        const val GET_TEXT = "getText"
        const val GET_TEXT_RANGE = "getTextRange"
        const val GET_START_OFFSET = "getStartOffset"
        const val GET_CHILDREN = "getChildren"
    }
}

/**
 * Entry points into PSI from KSP declarations.
 *
 * KSP hides the underlying `KaSymbol` behind either a private field (parameter, file) or a public
 * accessor (function). This factory encapsulates the two access patterns and the mandatory
 * `setAccessible(true)` (KSP's `*AAImpl` classes are package-private, so even public methods
 * on them cannot be invoked reflectively without it).
 *
 * Every entry returns `null` on any reflective failure; downstream code degrades to "no data".
 */
internal object KspPsi {
    /** Returns the underlying PSI `KtFile` wrapped as a [PsiNode], or `null` when unreachable. */
    fun of(file: KSFile): PsiNode? = fromField(file, KSFILE_SYMBOL_FIELD)

    /** Returns the underlying PSI `KtParameter` wrapped as a [PsiNode], or `null` when unreachable. */
    fun of(parameter: KSValueParameter): PsiNode? = fromField(parameter, KSVALUE_PARAMETER_SYMBOL_FIELD)

    /** Returns the underlying PSI `KtNamedFunction` wrapped as a [PsiNode], or `null` when unreachable. */
    fun of(function: KSFunctionDeclaration): PsiNode? = fromMethod(function, KSFUNCTION_SYMBOL_ACCESSOR)

    private fun fromField(
        target: Any,
        fieldName: String,
    ): PsiNode? =
        runCatching {
            val field = target.javaClass.getDeclaredField(fieldName).also { it.isAccessible = true }
            psiOf(field.get(target))
        }.getOrNull()

    private fun fromMethod(
        target: Any,
        methodName: String,
    ): PsiNode? =
        runCatching {
            val method = target.javaClass.getMethod(methodName).also { it.isAccessible = true }
            psiOf(method.invoke(target))
        }.getOrNull()

    /** Given a `KaSymbol` (or `null`), reflectively reads its `.psi` and wraps it. */
    private fun psiOf(symbol: Any?): PsiNode? {
        if (symbol == null) return null
        val psi = symbol.javaClass.getMethod(GET_PSI).invoke(symbol) ?: return null
        return PsiNode(psi)
    }

    /** Private field on `com.google.devtools.ksp.impl.symbol.kotlin.KSFileImpl`. */
    private const val KSFILE_SYMBOL_FIELD = "ktFileSymbol"

    /** Private field on `com.google.devtools.ksp.impl.symbol.kotlin.KSValueParameterImpl`. */
    private const val KSVALUE_PARAMETER_SYMBOL_FIELD = "ktValueParameterSymbol"

    /** Public accessor on `KSFunctionDeclarationImpl` (concrete class is package-private). */
    private const val KSFUNCTION_SYMBOL_ACCESSOR = "getKtFunctionSymbol"

    private const val GET_PSI = "getPsi"
}
