package com.quarkdown.core.context.hooks

import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.ast.base.inline.setSubdocument
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.log.Log
import com.quarkdown.core.util.IOUtils

/**
 * Hook that registers [Subdocument]s in the subdocument graph of [context].
 * A subdocument is a separate document file that is referenced by a link from the main document or another subdocument.
 * @param context the context to attach this hook to
 * @param failOnUnresolved whether to skip unresolved links
 */
class SubdocumentRegistrationHook(
    private val context: MutableContext,
    private val failOnUnresolved: Boolean = true,
) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        iterator.on<SubdocumentLink> { link ->
            val file = IOUtils.resolvePath(path = link.url, context.attachedPipeline?.options?.workingDirectory)

            if (failOnUnresolved && !file.exists()) {
                Log.warn("Cannot find subdocument referenced by a link: $file")
                return@on
            }

            val subdocument =
                Subdocument(
                    name = file.nameWithoutExtension,
                    path = file.absolutePath,
                    reader = { file.reader() },
                )

            link.setSubdocument(context, subdocument)

            context.subdocumentGraph =
                context.subdocumentGraph
                    .addVertex(subdocument)
                    .addEdge(Subdocument.ROOT, subdocument) // TODO edge from the current subdoc
        }
    }
}
