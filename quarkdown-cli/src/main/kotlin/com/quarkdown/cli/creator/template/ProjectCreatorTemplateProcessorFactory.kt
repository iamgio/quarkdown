package com.quarkdown.cli.creator.template

import com.quarkdown.core.template.TemplateProcessor

/**
 * Factory that creates a [TemplateProcessor] that helps generate the main file of a new Quarkdown project.
 * @see TemplateProcessor
 */
interface ProjectCreatorTemplateProcessorFactory {
    /**
     * The name of the file to be created from this template processor, without extension.
     * If `null`, it defaults to the main file.
     */
    val targetFileName: String?
        get() = null

    /**
     * @return the [TemplateProcessor] that processes the main file of a new Quarkdown project
     */
    fun create(): TemplateProcessor
}
