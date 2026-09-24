package com.quarkdown.core.flavor.base

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.iterator.AstIterator
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.hooks.HeadingIdentifierDeduplicationHook
import com.quarkdown.core.context.hooks.LinkUrlResolverHook
import com.quarkdown.core.context.hooks.SubdocumentRegistrationHook
import com.quarkdown.core.context.hooks.presence.CodePresenceHook
import com.quarkdown.core.context.hooks.presence.MathPresenceHook
import com.quarkdown.core.context.hooks.presence.MermaidDiagramPresenceHook
import com.quarkdown.core.context.hooks.reference.FootnoteResolverHook
import com.quarkdown.core.context.hooks.reference.LinkDefinitionResolverHook
import com.quarkdown.core.flavor.TreeIteratorFactory

/**
 * [BaseMarkdownFlavor] tree iterator factory.
 */
class BaseMarkdownTreeIteratorFactory : TreeIteratorFactory {
    override fun default(context: MutableContext): ObservableAstIterator =
        ObservableAstIterator()
            // Resolves reference links to their link definitions.
            .attach(LinkDefinitionResolverHook(context))
            // Registers subdocuments.
            .attach(SubdocumentRegistrationHook(context))
            // Resolves local URLs/paths for links and images loaded from different base paths.
            .attach(LinkUrlResolverHook(context))
            // Resolves footnotes.
            .attach(FootnoteResolverHook(context))
            // Assigns a deterministic occurrence index to headings that share a base identifier.
            .attach(HeadingIdentifierDeduplicationHook(context))
            // Allows loading code libraries (e.g. highlight.js syntax highlighting)
            // if at least one code block is present.
            .attach(CodePresenceHook(context))
            // Allows loading Mermaid libraries
            // if at least one diagram is present.
            .attach(MermaidDiagramPresenceHook(context))
            // Allows loading math libraries (e.g. KaTeX)
            // if at least one math block is present.
            .attach(MathPresenceHook(context))

    /**
     * Base Markdown has no notion of function extensions, so the rewriter is a no-op that returns the input tree unchanged.
     */
    override fun rewriter(context: MutableContext): AstIterator<AstRoot> =
        object : AstIterator<AstRoot> {
            override fun traverse(root: AstRoot) = root
        }
}
