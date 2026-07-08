package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

/**
 * Thin reflective wrapper around a PSI element from KSP2's shaded `ksp.org.jetbrains.kotlin.*`
 * package, whose types cannot be imported from source without coupling to KSP's internal layout.
 *
 * Callers read data through [get] with a typed [PsiOp]: the sealed op hierarchy encodes the
 * accessor name and the expected return shape, so there is no string method-name at the call
 * site and no `as?` cast beyond the one hidden in [get]. Every reflective failure is reported
 * through [PsiDiagnostics] and yields `null`, so downstream code degrades to "no data".
 */
internal class PsiNode(
    private val target: Any,
    private val diagnostics: PsiDiagnostics = PsiDiagnostics.NoOp,
) {
    /** Simple class name of the wrapped PSI type, e.g. `KtNameReferenceExpression`. */
    val simpleName: String get() = target.javaClass.simpleName

    /** Verbatim source text of the PSI element. Shortcut for `get(PsiOps.Text)`. */
    val text: String? get() = get(PsiOps.Text)

    /** File offset at which this element begins, or `null` when a `TextRange` isn't available. */
    val startOffset: Int? get() = get(PsiOps.TextRange)?.get(PsiOps.StartOffset)

    /**
     * Reads [op] from the wrapped element and returns the result in the op's typed shape,
     * or `null` when the accessor is missing, throws, or returns an unexpected type.
     */
    @Suppress("UNCHECKED_CAST")
    fun <R : Any> get(op: PsiOp<R>): R? {
        val raw = invoke(op.method) ?: return null
        val decoded: Any? =
            when (op) {
                is StringOp -> raw as? String
                is IntOp -> raw as? Int
                is NodeOp -> PsiNode(raw, diagnostics)
                is NodeListOp -> (raw as? List<*>)?.filterNotNull()?.map { PsiNode(it, diagnostics) }
            }
        return decoded as R?
    }

    /**
     * Yields this node and every descendant in pre-order. Suitable for walking small expression
     * trees (default expressions, annotation entries); not tuned for large PSI graphs.
     */
    fun walk(): Sequence<PsiNode> =
        sequence {
            yield(this@PsiNode)
            (invoke(GET_CHILDREN) as? Array<*>)?.forEach { child ->
                child?.let { yieldAll(PsiNode(it, diagnostics).walk()) }
            }
        }

    /** Invokes a no-arg method on the wrapped element, reporting failures through [diagnostics]. */
    private fun invoke(method: String): Any? =
        try {
            target.javaClass.getMethod(method).invoke(target)
        } catch (e: Throwable) {
            diagnostics.reflectionFailed(target.javaClass.simpleName, method, e)
            null
        }

    private companion object {
        const val GET_CHILDREN = "getChildren"
    }
}

/**
 * Entry points into PSI from KSP declarations.
 *
 * KSP hides the underlying `KaSymbol` behind either a private field (parameter, file) or a public
 * accessor (function). This class encapsulates the two access patterns and the mandatory
 * `setAccessible(true)` (KSP's `*AAImpl` classes are package-private, so even public methods
 * on them cannot be invoked reflectively without it).
 *
 * The [PsiDiagnostics] threaded in at construction time is propagated into every [PsiNode]
 * produced by this class, so a reflective failure anywhere down-stream of a `KspPsi.of(...)`
 * call goes through a single sink.
 */
internal class KspPsi(
    private val diagnostics: PsiDiagnostics = PsiDiagnostics.NoOp,
) {
    /** Returns the underlying PSI `KtFile` wrapped as a [PsiNode], or `null` when unreachable. */
    fun of(file: KSFile): PsiNode? = fromField(file, KSFILE_SYMBOL_FIELD)

    /** Returns the underlying PSI `KtParameter` wrapped as a [PsiNode], or `null` when unreachable. */
    fun of(parameter: KSValueParameter): PsiNode? = fromField(parameter, KSVALUE_PARAMETER_SYMBOL_FIELD)

    /** Returns the underlying PSI `KtNamedFunction` wrapped as a [PsiNode], or `null` when unreachable. */
    fun of(function: KSFunctionDeclaration): PsiNode? = fromMethod(function, KSFUNCTION_SYMBOL_ACCESSOR)

    private fun fromField(
        target: Any,
        fieldName: String,
    ): PsiNode? {
        val symbol =
            safely(target.javaClass.simpleName, fieldName) {
                val field = target.javaClass.getDeclaredField(fieldName).also { it.isAccessible = true }
                field.get(target)
            } ?: return null
        return psiOf(symbol)
    }

    private fun fromMethod(
        target: Any,
        methodName: String,
    ): PsiNode? {
        val symbol =
            safely(target.javaClass.simpleName, methodName) {
                val method = target.javaClass.getMethod(methodName).also { it.isAccessible = true }
                method.invoke(target)
            } ?: return null
        return psiOf(symbol)
    }

    /** Given a `KaSymbol`, reflectively reads its `.psi` and wraps it. */
    private fun psiOf(symbol: Any): PsiNode? {
        val psi =
            safely(symbol.javaClass.simpleName, GET_PSI) {
                symbol.javaClass.getMethod(GET_PSI).invoke(symbol)
            } ?: return null
        return PsiNode(psi, diagnostics)
    }

    private inline fun safely(
        target: String,
        method: String,
        block: () -> Any?,
    ): Any? =
        try {
            block()
        } catch (e: Throwable) {
            diagnostics.reflectionFailed(target, method, e)
            null
        }

    private companion object {
        /** Private field on `com.google.devtools.ksp.impl.symbol.kotlin.KSFileImpl`. */
        const val KSFILE_SYMBOL_FIELD = "ktFileSymbol"

        /** Private field on `com.google.devtools.ksp.impl.symbol.kotlin.KSValueParameterImpl`. */
        const val KSVALUE_PARAMETER_SYMBOL_FIELD = "ktValueParameterSymbol"

        /** Public accessor on `KSFunctionDeclarationImpl` (concrete class is package-private). */
        const val KSFUNCTION_SYMBOL_ACCESSOR = "getKtFunctionSymbol"

        const val GET_PSI = "getPsi"
    }
}
