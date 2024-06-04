package eu.iamgio.quarkdown.document.slides

/**
 * An animated transition between two slides.
 * @param style transition type
 * @param speed speed of the transition
 */
data class Transition(val style: Style, val speed: Speed = Speed.DEFAULT) {
    /**
     * Transition types.
     */
    enum class Style {
        NONE,
        FADE,
        SLIDE,
        ZOOM,
    }

    /**
     * Transition speeds.
     */
    enum class Speed {
        DEFAULT,
        FAST,
        SLOW,
    }
}
