package com.quarkdown.core.context.hooks

import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.ast.base.inline.setSubdocument
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.document.sub.Subdocument
import com.quarkdown.core.log.Log

/**
 * Hook that registers [Subdocument]s in the subdocument graph of [context].
 * A subdocument is a separate document file that is referenced by a link from the main document or another subdocument.
 * @param context the context to attach this hook to
 */
class SubdocumentRegistrationHook(
    private val context: MutableContext,
) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        iterator.on<SubdocumentLink> { link ->
            val file = context.fileSystem.resolve(path = link.url)

            if (!file.exists()) {
                Log.warn("Cannot find subdocument referenced by a link: $file")
                return@on
            }

            val subdocument =
                Subdocument.Resource(
                    name = file.nameWithoutExtension,
                    path = file.absolutePath,
                    workingDirectory = file.parentFile,
                    content = file.readText(),
                )

            link.setSubdocument(context, subdocument)

            context.subdocumentGraph =
                context.subdocumentGraph
                    .addVertex(subdocument)
                    .addEdge(context.subdocument to subdocument)
        }
    }
}
