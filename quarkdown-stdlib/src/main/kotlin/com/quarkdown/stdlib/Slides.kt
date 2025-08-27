package com.quarkdown.stdlib

import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.quarkdown.block.SlidesFragment
import com.quarkdown.core.ast.quarkdown.block.SlidesSpeakerNote
import com.quarkdown.core.ast.quarkdown.invisible.SlidesConfigurationInitializer
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.slides.Transition
import com.quarkdown.core.function.library.loader.Module
import com.quarkdown.core.function.library.loader.moduleOf
import com.quarkdown.core.function.reflect.annotation.LikelyBody
import com.quarkdown.core.function.reflect.annotation.LikelyNamed
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
        ::speakerNote,
    )

/**
 * Sets global properties that affect the behavior of a 'slides' document.
 *
 * @param center whether slides should be centered vertically
 * @param showControls whether navigation controls should be shown
 * @param showNotes whether speaker notes should be shown when not in speaker view
 * @param transitionStyle global transition style between slides
 * @param transitionSpeed global transition speed between slides
 * @return a new [SlidesConfigurationInitializer] node
 * @wiki Slides configuration
 */
@OnlyForDocumentType(DocumentType.SLIDES)
@Name("slides")
fun setSlidesConfiguration(
    @LikelyNamed center: Boolean? = null,
    @Name("controls") showControls: Boolean? = null,
    @Name("speakernotes") showNotes: Boolean? = null,
    @Name("transition") transitionStyle: Transition.Style? = null,
    @Name("speed") transitionSpeed: Transition.Speed = Transition.Speed.DEFAULT,
): NodeValue =
    SlidesConfigurationInitializer(
        center,
        showControls,
        showNotes,
        transitionStyle?.let { Transition(it, transitionSpeed) },
    ).wrappedAsValue()

/**
 * Creates an element that, when used in a `slides` document,
 * shows its content when the user attempts to go to the next slide.
 *
 * Multiple fragments in the same slide are shown in order on distinct user interactions.
 *
 * @param behavior visibility type of the fragment and how it reacts to user interactions
 * @param content content to show/hide
 * @return a new [SlidesFragment] node
 * @wiki Slides fragment
 */
@OnlyForDocumentType(DocumentType.SLIDES)
fun fragment(
    behavior: SlidesFragment.Behavior = SlidesFragment.Behavior.SHOW,
    @LikelyBody content: MarkdownContent,
) = SlidesFragment(behavior, content.children).wrappedAsValue()

/**
 * Creates a speaker note for a `slides` document.
 *
 * Speaker notes are visible only to the presenter and not to the audience during a presentation.
 * In Reveal.js, speaker notes are shown in the speaker view (enabled by pressing `S`).
 *
 * @param content the content of the note
 * @return a new [SlidesSpeakerNote] node
 * @wiki Slides speaker note
 */
@OnlyForDocumentType(DocumentType.SLIDES)
@Name("speakernote")
fun speakerNote(
    @LikelyBody content: MarkdownContent,
): NodeValue = SlidesSpeakerNote(content.children).wrappedAsValue()
