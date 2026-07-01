package com.quarkdown.processor.util

/**
 * Reflectively invokes the no-argument public method [methodName] on this instance.
 *
 * Shared by the discovery-stage extractors that reach into KSP's shaded PSI types
 * (`KaSymbol`, `KtElement`, `KtImportList`, ...) whose fully-qualified names live in KSP's
 * relocated `ksp.org.jetbrains.kotlin.*` package and cannot be referenced from source without
 * hard-coding KSP's internal layout.
 *
 * Callers are expected to wrap chains of these calls in `runCatching` and to `setAccessible(true)`
 * the underlying `Method`/`Field` when the declaring class is package-private (KSP's `*AAImpl`
 * classes are).
 */
internal fun Any.callPublic(methodName: String): Any? = javaClass.getMethod(methodName).invoke(this)
