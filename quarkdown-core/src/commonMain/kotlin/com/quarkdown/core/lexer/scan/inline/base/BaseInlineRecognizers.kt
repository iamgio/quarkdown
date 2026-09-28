package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.flavor.InlineLexerVariant
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer

/**
 * The inline recognizers of the base Markdown flavor, highest priority first.
 *
 * Every emphasis flavor is claimed by [DelimiterRunRecognizer]: pairing happens in the inline scanner's second
 * pass, so priority only decides who claims a character first.
 *
 * The link-label variant drops the diamond autolink, the link, the reference footnote, the reference link and
 * the URL autolink, and **keeps** both image recognizers: a link may not nest inside a link label, an image
 * may.
 *
 * The critical content recognizer is the last element of this list, so no caller can place a recognizer after
 * it.
 *
 * @param variant which constructs the lexer accepts
 * @return the recognizers, highest priority first
 */
fun baseInlineRecognizers(variant: InlineLexerVariant): List<InlineRecognizer> =
    buildList {
        if (variant != InlineLexerVariant.LINK_LABEL) {
            add(DiamondAutolinkRecognizer)
            add(LinkRecognizer)
            add(ReferenceFootnoteRecognizer)
            add(ReferenceLinkRecognizer)
            add(UrlAutolinkRecognizer)
        }
        add(LineBreakRecognizer)
        add(CodeSpanRecognizer)
        add(EscapeRecognizer)
        add(EntityRecognizer)
        add(InlineCommentRecognizer)
        add(ImageRecognizer)
        add(ReferenceImageRecognizer)
        add(DelimiterRunRecognizer)
        add(CriticalContentRecognizer)
    }
