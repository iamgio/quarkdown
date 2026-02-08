package com.quarkdown.core.context.subdocument

import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.graph.Graph
import com.quarkdown.core.graph.VisitableOnceGraph

/**
 * Immutable container of information about the subdocuments that are part of a document complex.
 * A subdocument is a separate document file that can be rendered independently,
 * and is referenced by a link from the main document or another subdocument.
 *
 * This data is shared, meaning that all contexts involved in the document complex
 * have access to the same subdocument graph and information, regardless of the sandboxing level.
 * For instance, [com.quarkdown.core.context.SubdocumentContext] has strong isolation from the main document context,
 * but can still access and modify the shared subdocument data.
 *
 * @param graph directed graph of the subdocuments that are part of the document complex
 * @param withContexts mapping of each subdocument in the graph to the context it is processed with
 */
data class SubdocumentsData<G : Graph<Subdocument>>(
    val graph: G,
    val withContexts: Map<Subdocument, Context>,
) {
    /**
     * Adds a new subdocument and its context to the current data,
     * returning a new instance with the updated mapping.
     *
     * Note that this does not modify the [graph]; the new subdocument
     * must already be part of it.
     *
     * @param subdocument the subdocument to add
     * @param context the context the subdocument is processed with
     * @return a new instance of [SubdocumentsData] with the updated mapping
     */
    fun addContext(
        subdocument: Subdocument,
        context: Context,
    ): SubdocumentsData<G> = this.copy(withContexts = this.withContexts + (subdocument to context))
}

/**
 * The directed graph of subdocuments that are part of the document complex.
 * @see SubdocumentsData.graph
 */
val Context.subdocumentGraph: Graph<Subdocument>
    get() = this.sharedSubdocumentsData.graph

/**
 * The directed graph of subdocuments that are part of the document complex.
 * @see SubdocumentsData.graph
 */
var MutableContext.subdocumentGraph: VisitableOnceGraph<Subdocument>
    get() = this.sharedSubdocumentsData.graph
    set(value) {
        this.sharedSubdocumentsData = this.sharedSubdocumentsData.copy(graph = value)
    }

/**
 * Finds a [Subdocument.Resource] vertex in the graph by its absolute [path].
 * @return the matching resource, or `null` if not found
 */
fun Graph<Subdocument>.findResourceByPath(path: String): Subdocument.Resource? =
    vertices.asSequence()
        .filterIsInstance<Subdocument.Resource>()
        .find { it.path == path }
