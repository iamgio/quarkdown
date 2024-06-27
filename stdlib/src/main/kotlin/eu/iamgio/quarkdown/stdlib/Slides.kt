package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.SlidesConfigurationInitializer
import eu.iamgio.quarkdown.document.slides.Transition
import eu.iamgio.quarkdown.function.reflect.Name
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.wrappedAsValue

/**
 * `Slides` stdlib module exporter.
 * This module handles slides properties.
 */
val Slides: Module =
    setOf(
        ::setSlidesConfiguration,
    )

/**
 * Sets global properties that affect the behavior of a 'slides' document.
 * @param center whether slides should be centered vertically
 * @param showControls whether navigation controls should be shown
 * @param transitionStyle global transition style between slides
 * @param transitionSpeed global transition speed between slides
 * @return a wrapped [SlidesConfigurationInitializer] node
 */
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
