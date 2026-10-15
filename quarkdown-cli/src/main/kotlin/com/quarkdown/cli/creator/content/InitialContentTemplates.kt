package com.quarkdown.cli.creator.content

import com.quarkdown.template.TemplateProcessor

/**
 * Renders a precompiled JTE template through a copy of this processor,
 * so that the rendered content sees every placeholder value already set on the main template.
 * @param templateName name of the precompiled JTE template, e.g. `creator/initialcontent.qd.jte`
 * @return the rendered content
 */
internal fun TemplateProcessor.renderInitialContent(templateName: String): CharSequence = copy(name = templateName).process()
