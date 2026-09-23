package com.quarkdown.core.localization

import com.quarkdown.core.RUNTIME_ERROR_EXIT_CODE
import com.quarkdown.core.pipeline.error.PipelineException

/**
 * An exception thrown when a localization-related error occurs.
 * @see com.quarkdown.core.context.Context.localize
 */
open class LocalizationException(
    message: String,
) : PipelineException(message, RUNTIME_ERROR_EXIT_CODE)

/**
 * An exception thrown when a localization key is not found within a localization table.
 * @see LocalizationEntries
 */
class LocalizationKeyNotFoundException(
    tableName: String,
    locale: Locale,
    key: String,
) : LocalizationException(
        "Could not find localization key \"$key\" in table \"$tableName\" for locale ${locale.tag}",
    )

/**
 * An exception thrown when a locale is not found within a localization table.
 * @see LocalizationTable
 */
class LocalizationLocaleNotFoundException(
    tableName: String,
    locale: Locale,
) : LocalizationException(
        "Could not find locale ${locale.tag} in table \"$tableName\"",
    )

/**
 * An exception thrown when a localization table is not found.
 * @see LocalizationTables
 * @see LocalizationTable
 */
class LocalizationTableNotFoundException(
    tableName: String,
) : LocalizationException(
        "Could not find localization table \"$tableName\"",
    )

/**
 * An exception thrown when localization based on the currently set locale is being attempted, but the locale is not set.
 * @see com.quarkdown.core.document.DocumentInfo.locale
 */
class LocaleNotSetException :
    LocalizationException(
        "Trying to localize from a document that does not have a locale set. Tip: .doclang {locale}",
    )
