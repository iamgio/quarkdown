package com.quarkdown.core.flavor.quarkdown

import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.hooks.MediaStorerHook
import com.quarkdown.core.context.hooks.TableOfContentsGeneratorHook
import com.quarkdown.core.context.hooks.bibliography.BibliographyCitationHook
import com.quarkdown.core.context.hooks.location.LocationAwareLabelStorerHook
import com.quarkdown.core.context.hooks.location.LocationAwarenessHook
import com.quarkdown.core.context.hooks.location.NumberedEvaluatorHook
import com.quarkdown.core.flavor.TreeIteratorFactory
import com.quarkdown.core.flavor.base.BaseMarkdownTreeIteratorFactory

/**
 * [QuarkdownFlavor] tree iterator factory.
 */
class QuarkdownTreeIteratorFactory : TreeIteratorFactory {
    override fun default(context: MutableContext): ObservableAstIterator =
        BaseMarkdownTreeIteratorFactory()
            .default(context)
            .attach(LocationAwarenessHook(context))
            .attach(LocationAwareLabelStorerHook(context))
            .attach(NumberedEvaluatorHook(context))
            .attach(TableOfContentsGeneratorHook(context))
            .attach(BibliographyCitationHook(context))
            .apply {
                if (context.attachedPipeline?.options?.enableMediaStorage == true) {
                    attach(MediaStorerHook(context))
                }
            }
}
