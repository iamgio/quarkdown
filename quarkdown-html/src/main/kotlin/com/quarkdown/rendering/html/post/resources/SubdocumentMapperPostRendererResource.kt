package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.context.Context
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.document.sub.getOutputFileName
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.core.util.Escape

/**
 * Base class for [PostRendererResource]s that emit a single artifact indexing the site's subdocuments,
 * such as `sitemap.xml` ([SitemapPostRendererResource]) or `llms.txt` ([LlmsTxtPostRendererResource]).
 *
 * The resource is always skipped in preview mode, when the base URL is unset, or when the document has no subdocuments.
 *
 * @param context the context of the document being rendered
 */
abstract class SubdocumentMapperPostRendererResource(
    protected val context: Context,
) : PostRendererResource {
    override val runsInPreviewMode: Boolean = false

    protected val baseUrl: String
        get() =
            requireNotNull(
                context.options.html.baseUrl
                    ?.trimEnd('/'),
            )

    override fun includeTo(
        resources: MutableSet<OutputResource>,
        rendered: CharSequence,
    ) {
        if (context.options.html.baseUrl == null) return

        val subdocuments = context.subdocumentEntries()
        if (subdocuments.none()) return

        val content = buildResourceContent(subdocuments) ?: return

        resources +=
            TextOutputArtifact(
                name = resourceName,
                content = content,
                type = ArtifactType.AUTO,
            )
    }

    /**
     * Every subdocument in this document's shared data, paired with the [Context] it was rendered in,
     * excluding the [Subdocument.Root] entry.
     */
    protected fun Context.subdocumentEntries(): Sequence<Pair<Subdocument, Context>> =
        sharedSubdocumentsData.withContexts
            .asSequence()
            .filter { (subdocument, _) -> subdocument !== Subdocument.Root }
            .map { (subdocument, subdocumentContext) -> subdocument to subdocumentContext }

    /**
     * Name of the emitted artifact (e.g. `sitemap.xml`, `llms.txt`).
     */
    protected abstract val resourceName: String

    /**
     * Builds the body of the emitted artifact from the given [subdocuments].
     * @param subdocuments non-root subdocuments to include in the index
     * @return the rendered artifact content, or `null` to skip emission
     */
    protected abstract fun buildResourceContent(subdocuments: Sequence<Pair<Subdocument, Context>>): String?

    /**
     * Absolute URL of a subdocument identified by its output name.
     * @param subdocumentOutputName the subdocument's output file name, before URL escaping
     * @param extension optional file extension to append after the escaped name (e.g. `.md`), or `null` for the HTML directory form
     * @return the absolute URL
     */
    protected fun getSubdocumentUrl(
        subdocumentOutputName: String,
        extension: String?,
    ): String =
        buildString {
            append(baseUrl)
            if (!baseUrl.endsWith('/')) append('/')
            append(Escape.Url.escape(subdocumentOutputName))
            when {
                extension != null -> append(extension)
                else -> append('/')
            }
        }

    /**
     * Absolute URL of the given [subdocument], resolved through its own [subdocumentContext].
     * @param subdocument the subdocument to resolve
     * @param subdocumentContext the context the subdocument was rendered in, used to compute its output file name
     * @param extension optional file extension to append (e.g. `.md`), or `null` for none
     * @return the absolute URL
     */
    protected fun getSubdocumentUrl(
        subdocument: Subdocument,
        subdocumentContext: Context,
        extension: String?,
    ): String {
        val subdocumentOutputName = subdocument.getOutputFileName(subdocumentContext)
        return getSubdocumentUrl(subdocumentOutputName, extension)
    }
}
