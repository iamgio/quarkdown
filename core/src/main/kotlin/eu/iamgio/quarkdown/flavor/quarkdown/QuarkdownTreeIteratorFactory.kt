package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.ast.iterator.ObservableAstIterator
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.context.hooks.LocationAwarenessHook
import eu.iamgio.quarkdown.context.hooks.MediaStorerHook
import eu.iamgio.quarkdown.context.hooks.TableOfContentsGeneratorHook
import eu.iamgio.quarkdown.flavor.TreeIteratorFactory
import eu.iamgio.quarkdown.flavor.base.BaseMarkdownTreeIteratorFactory

/**
 * [QuarkdownFlavor] tree iterator factory.
 */
class QuarkdownTreeIteratorFactory : TreeIteratorFactory {
    override fun default(context: MutableContext): ObservableAstIterator =
        BaseMarkdownTreeIteratorFactory()
            .default(context)
            .attach(LocationAwarenessHook(context))
            .attach(TableOfContentsGeneratorHook(context))
            .apply {
                if (context.attachedPipeline?.options?.enableMediaStorage == true) {
                    attach(MediaStorerHook(context))
                }
            }
}
