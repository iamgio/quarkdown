package com.quarkdown.processor.discovery

import com.google.devtools.ksp.processing.KSPLogger
import com.quarkdown.processor.discovery.PsiDiagnostics.Companion.NoOp

/**
 * Sink for reflective PSI access failures. Every reflective call in [PsiNode] and [KspPsi] is
 * wrapped in a try/catch that reports here before returning `null` - so any failure surfaces
 * through a single, swappable channel rather than being silently swallowed.
 *
 * The default implementation is [NoOp], keeping production quiet since a missing PSI accessor
 * degrades gracefully (extractors return `null`, the wrapper loses the corresponding piece of
 * source-level information). Turn on [LoggingPsiDiagnostics] with the `quarkdown.psi.debug=true`
 * processor option during a KSP upgrade to have every reflective failure logged.
 */
internal fun interface PsiDiagnostics {
    /**
     * Called once for every reflective access that fails. [targetClass] is the simple class name
     * of the wrapped element, [method] is the reflected accessor, and [cause] is the underlying
     * throwable (typically [NoSuchMethodException], [NoSuchFieldException], or a cast issue).
     */
    fun reflectionFailed(
        targetClass: String,
        method: String,
        cause: Throwable,
    )

    companion object {
        /** Discards every failure; the production default. */
        val NoOp: PsiDiagnostics = PsiDiagnostics { _, _, _ -> }
    }
}

/**
 * [PsiDiagnostics] implementation that funnels failures into [KSPLogger.warn]. Intended to be
 * opt-in via the `quarkdown.psi.debug=true` processor option during a KSP upgrade, where every
 * failed accessor is a hint about what the shaded PSI API has renamed or moved.
 */
internal class LoggingPsiDiagnostics(
    private val logger: KSPLogger,
) : PsiDiagnostics {
    override fun reflectionFailed(
        targetClass: String,
        method: String,
        cause: Throwable,
    ) {
        logger.warn("PSI reflection failed: $targetClass#$method: ${cause.message}")
    }
}
