package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.ast.iterator.ObservableAstIterator
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.context.hooks.CodePresenceHook
import eu.iamgio.quarkdown.context.hooks.LinkDefinitionRegistrationHook
import eu.iamgio.quarkdown.context.hooks.MathPresenceHook
import eu.iamgio.quarkdown.flavor.TreeIteratorFactory

/**
 * [BaseMarkdownFlavor] tree iterator factory.
 */
class BaseMarkdownTreeIteratorFactory : TreeIteratorFactory {
    override fun default(context: MutableContext): ObservableAstIterator =
        ObservableAstIterator()
            // Registers link definitions.
            .attach(LinkDefinitionRegistrationHook(context))
            // Allows loading code libraries (e.g. highlight.js syntax highlighting)
            // if at least one code block is present.
            .attach(CodePresenceHook(context))
            // Allows loading math libraries (e.g. MathJax)
            // if at least one math block is present.
            .attach(MathPresenceHook(context))
}
