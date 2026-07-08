package com.quarkdown.processor.discovery

/**
 * Typed handle for a reflective read from a PSI element. Each subclass encodes both the target
 * accessor's name (a shaded PSI method) and its expected return shape, so the extractors that
 * consume [PsiNode] can pull data through a compile-time-typed API instead of writing
 * `psi.call("getSomething") as? Foo` at the call site.
 *
 * Every string method-name in the codebase now lives on one of the `PsiOps.*` values, giving
 * the processor a single reviewable surface for "what does this module read out of PSI?".
 *
 * @param method name of the no-arg accessor on the wrapped PSI element
 */
internal sealed class PsiOp<R : Any>(
    val method: String,
)

/** Op producing a plain [String] (e.g. `getText`, `asString`). */
internal class StringOp(
    method: String,
) : PsiOp<String>(method)

/** Op producing an [Int] (e.g. `getStartOffset`). */
internal class IntOp(
    method: String,
) : PsiOp<Int>(method)

/** Op producing a single child [PsiNode] (e.g. `getShortName`, `getDefaultValue`). */
internal class NodeOp(
    method: String,
) : PsiOp<PsiNode>(method)

/** Op producing a homogeneous list of [PsiNode]s (e.g. `getImports`, `getAnnotationEntries`). */
internal class NodeListOp(
    method: String,
) : PsiOp<List<PsiNode>>(method)

/**
 * Catalog of every reflective PSI accessor the processor knows about. Adding a new PSI method
 * means adding one line here and using the typed constant at the call site; the reflection
 * shape (return type, cast) is derived by [PsiNode.get].
 *
 * These names correspond to methods on shaded PSI types under `ksp.org.jetbrains.kotlin.*`,
 * which cannot be imported from source without coupling to KSP's internal layout.
 */
internal object PsiOps {
    val Text = StringOp("getText")
    val AsString = StringOp("asString")
    val ReferencedName = StringOp("getReferencedName")

    val StartOffset = IntOp("getStartOffset")

    val TextRange = NodeOp("getTextRange")
    val DefaultValue = NodeOp("getDefaultValue")
    val ShortName = NodeOp("getShortName")
    val ImportList = NodeOp("getImportList")
    val ImportedFqName = NodeOp("getImportedFqName")

    val AnnotationEntries = NodeListOp("getAnnotationEntries")
    val Imports = NodeListOp("getImports")
}
