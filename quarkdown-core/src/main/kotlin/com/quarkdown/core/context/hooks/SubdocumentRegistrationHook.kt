package com.quarkdown.core.context.hooks

import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.ast.base.inline.setSubdocument
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.subdocument.findResourceByPath
import com.quarkdown.core.context.subdocument.subdocumentGraph
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
            val fileSystem = link.fileSystem ?: context.fileSystem
            val file = fileSystem.resolve(path = link.url)

            if (!file.exists()) {
                Log.warn("Cannot find subdocument referenced by a link: $file")
                return@on
            }

            val path = file.canonicalFile.absolutePath

            // Reuse an already-registered subdocument to avoid redundant file I/O.
            val subdocument =
                context.subdocumentGraph.findResourceByPath(path)
                    ?: Subdocument.Resource(
                        name = file.nameWithoutExtension,
                        path = path,
                        workingDirectory = file.parentFile.canonicalFile,
                        content = file.readText(),
                    )

            link.setSubdocument(context, subdocument)

            context.subdocumentGraph =
                context.subdocumentGraph
                    .addVertexAndEdge(
                        vertex = subdocument,
                        edgeFrom = context.subdocument,
                        edgeTo = subdocument,
                    )
        }
    }
}
