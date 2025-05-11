package com.quarkdown.stdlib

import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.quarkdown.block.SlidesFragment
import com.quarkdown.core.ast.quarkdown.invisible.SlidesConfigurationInitializer
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.slides.Transition
import com.quarkdown.core.function.library.loader.Module
import com.quarkdown.core.function.library.loader.moduleOf
import com.quarkdown.core.function.reflect.annotation.Name
import com.quarkdown.core.function.reflect.annotation.OnlyForDocumentType
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.wrappedAsValue

/**
 * `Slides` stdlib module exporter.
 * This module handles slides properties.
 */
val Slides: Module =
    moduleOf(
        ::setSlidesConfiguration,
        ::fragment,
    )

/**
 * Sets global properties that affect the behavior of a 'slides' document.
 * @param center whether slides should be centered vertically
 * @param showControls whether navigation controls should be shown
 * @param transitionStyle global transition style between slides
 * @param transitionSpeed global transition speed between slides
 * @return a wrapped [SlidesConfigurationInitializer] node
 */
@OnlyForDocumentType(DocumentType.SLIDES)
@Name("slides")
fun setSlidesConfiguration(
    center: Boolean? = null,
    @Name("controls") showControls: Boolean? = null,
    @Name("transition") transitionStyle: Transition.Style? = null,
    @Name("speed") transitionSpeed: Transition.Speed = Transition.Speed.DEFAULT,
): NodeValue =
    SlidesConfigurationInitializer(
        center,
        showControls,
        transitionStyle?.let { Transition(it, transitionSpeed) },
    ).wrappedAsValue()

/**
 * Creates an element that, when used in a `slides` document,
 * shows its content when the user attempts to go to the next slide.
 * Multiple fragments in the same slide are shown in order on distinct user interactions.
 * @param behavior visibility type of the fragment and how it reacts to user interactions
 * @param content content to show/hide
 * @return the fragment node
 */
@OnlyForDocumentType(DocumentType.SLIDES)
fun fragment(
    behavior: SlidesFragment.Behavior = SlidesFragment.Behavior.SHOW,
    content: MarkdownContent,
) = SlidesFragment(behavior, content.children).wrappedAsValue()
