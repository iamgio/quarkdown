package com.quarkdown.quarkdoc.dokka.kdoc

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.doc.A
import org.jetbrains.dokka.model.doc.CodeInline
import org.jetbrains.dokka.model.doc.DocTag
import org.jetbrains.dokka.model.doc.DocumentationLink
import org.jetbrains.dokka.model.doc.H1
import org.jetbrains.dokka.model.doc.H2
import org.jetbrains.dokka.model.doc.H3
import org.jetbrains.dokka.model.doc.H4
import org.jetbrains.dokka.model.doc.Li
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.model.doc.Ul

/**
 * A DSL for building [DocTag]s, which are used to represent documentation content in Dokka.
 * This DSL allows for a more readable and structured way to create documentation tags.
 */
class DocTagBuilder {
    private val tags = mutableListOf<DocTag>()

    private fun add(tag: DocTag) {
        tags.add(tag)
    }

    private operator fun DocTag.unaryPlus() {
        add(this)
    }

    private fun build(block: DocTagBuilder.() -> Unit): List<DocTag> = buildDocTags(block)

    /**
     * @see Text
     */
    fun text(text: String) {
        +Text(text)
    }

    /**
     * @see CodeInline
     */
    fun codeInline(text: String) {
        +CodeInline(children = listOf(Text(text)))
    }

    /**
     * @see H1
     */
    fun h1(block: DocTagBuilder.() -> Unit) {
        +H1(children = build(block))
    }

    /**
     * @see H2
     */
    fun h2(block: DocTagBuilder.() -> Unit) {
        +H2(children = build(block))
    }

    /**
     * @see H3
     */
    fun h3(block: DocTagBuilder.() -> Unit) {
        +H3(children = build(block))
    }

    /**
     * @see H4
     */
    fun h4(block: DocTagBuilder.() -> Unit) {
        +H4(children = build(block))
    }

    /**
     * @see A
     */
    fun link(
        address: String,
        block: DocTagBuilder.() -> Unit,
    ) {
        +A(
            params = mapOf("href" to address),
            children = build(block),
        )
    }

    /**
     * @see A
     */
    fun link(
        address: String,
        text: String,
    ) {
        link(address) { text(text) }
    }

    /**
     * @see DocumentationLink
     */
    fun link(
        dri: DRI,
        block: DocTagBuilder.() -> Unit,
    ) {
        +DocumentationLink(
            dri = dri,
            children = build(block),
        )
    }

    /**
     * @see Ul
     */
    fun unorderedList(block: DocTagBuilder.() -> Unit) {
        +Ul(children = build(block))
    }

    /**
     * @see Li
     */
    fun listItem(block: DocTagBuilder.() -> Unit) {
        +Li(children = build(block))
    }

    /**
     * @return the built tags
     */
    fun build(): List<DocTag> = tags.toList()
}

/**
 * Builds a list of [DocTag]s using the provided [block] via a [DocTagBuilder] DSL.
 * This is a convenient function to create documentation tags in a more readable way.
 * @param block the DSL block to build the tags
 * @return a list of [DocTag]s created by the DSL block
 */
fun buildDocTags(block: DocTagBuilder.() -> Unit): List<DocTag> = DocTagBuilder().apply(block).build()
