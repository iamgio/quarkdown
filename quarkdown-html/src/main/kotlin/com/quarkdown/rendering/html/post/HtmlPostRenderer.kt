package com.quarkdown.rendering.html.post

import com.quarkdown.core.context.Context
import com.quarkdown.core.document.DocumentTheme
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.orDefault
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.rendering.PostRenderer
import com.quarkdown.installlayout.InstallLayout
import com.quarkdown.rendering.html.post.resources.MediaPostRendererResource
import com.quarkdown.rendering.html.post.resources.PostRendererResource
import com.quarkdown.rendering.html.post.resources.ProxiedPostRendererResource
import com.quarkdown.rendering.html.post.resources.ScriptPostRendererResource
import com.quarkdown.rendering.html.post.resources.SearchIndexPostRendererResource
import com.quarkdown.rendering.html.post.resources.SitemapPostRendererResource
import com.quarkdown.rendering.html.post.resources.StaticAssetsPostRendererResource
import com.quarkdown.rendering.html.post.resources.ThemePostRendererResource
import com.quarkdown.rendering.html.post.resources.ThirdPartyPostRendererResource
import com.quarkdown.rendering.html.search.SearchIndexGenerator

// Default theme components to use if not specified by the user.
private val DEFAULT_THEME =
    DocumentTheme(
        color = "paperwhite",
        layout = "latex",
    )

/**
 * A [PostRenderer] that wraps content into a full HTML document. This includes all the features of [HtmlOnlyPostRenderer], plus:
 * - Theme components
 * - Runtime scripts
 * - Media resources
 * - Third-party libraries (e.g. KaTeX, Mermaid)
 * - User-provided static assets (from a `public/` directory)
 * - Search index (for docs)
 *
 * @param context the [Context] of the document being rendered
 * @param resourcesLayout the install layout node for the `html/` subtree, used to locate themes, scripts, and third-party libraries
 * @param relativePathToRoot relative path from the current document to the root document, used to correctly link resources
 * @param base the base [HtmlOnlyPostRenderer] to delegate HTML generation to
 * @param resourcesProvider supplier of the set of [PostRendererResource] to include in the output. Delegation to [base] is always included
 */
class HtmlPostRenderer(
    val context: Context,
    resourcesLayout: InstallLayout.Html? = null,
    relativePathToRoot: String = ".",
    private val base: HtmlOnlyPostRenderer =
        HtmlOnlyPostRenderer(
            context,
            relativePathToRoot = relativePathToRoot,
        ),
    private val resourcesProvider: () -> Set<PostRendererResource> =
        {
            setOfNotNull(
                ThemePostRendererResource(
                    theme = context.documentInfo.theme.orDefault(DEFAULT_THEME),
                    locale = context.documentInfo.locale,
                    themeLayout = resourcesLayout?.themes,
                ),
                ScriptPostRendererResource(scriptsLayout = resourcesLayout?.scripts),
                MediaPostRendererResource(context.mediaStorage),
                context.fileSystem.workingDirectory
                    ?.let(::StaticAssetsPostRendererResource),
                SitemapPostRendererResource(context),
                SearchIndexGenerator
                    .takeIf { context.documentInfo.type == DocumentType.DOCS }
                    ?.generate(context.sharedSubdocumentsData)
                    ?.let(::SearchIndexPostRendererResource),
                ThirdPartyPostRendererResource(context, librariesLayout = resourcesLayout?.libraries),
            )
        },
) : PostRenderer by base {
    override fun generateResources(rendered: CharSequence): Set<OutputResource> =
        buildSet {
            // The HTML content is always included, regardless of the other options.
            val resources = resourcesProvider() + ProxiedPostRendererResource(base)
            resources.forEach { it.includeTo(this, rendered) }
        }

    override fun wrapResources(
        name: String,
        resources: Set<OutputResource>,
    ) = OutputResourceGroup(name, resources)
}
