@file:QModule

package com.quarkdown.stdlib

import com.quarkdown.core.function.value.DictionaryValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule
import org.kodein.emoji.Emoji
import org.kodein.emoji.EmojiTemplateCatalog
import org.kodein.emoji.list

private val allEmojis by lazy { Emoji.list() }

private val emojiCatalog by lazy { EmojiTemplateCatalog(allEmojis) }

/**
 * Inserts an emoji by its shortcode, e.g., `smile`.
 *
 * An emoji can be described with:
 *
 * - A simple short-code: `.emoji {wink}` produces 😉
 * - A short-code with one skin tone: `.emoji {waving-hand~medium-dark}` produces 👋🏾
 * - A short-code with two skin tones: `.emoji {people-holding-hands~medium-light,medium-dark}` produces 🧑🏼‍🤝‍🧑🏾
 *
 * A complete list of shortcodes can be found at [quarkdown.com/docs/emoji-list](https://quarkdown.com/docs/emoji-list).
 *
 * Note: the first call to this function initializes the emoji catalog, which may take a moment.
 * Subsequent calls will be faster.
 *
 * @param shortcode the shortcode of the emoji to insert (without colons)
 * @return the emoji as a string, or the shortcode as plain text, surrounded by colons, if not found
 * @wiki emojis
 */
@QFunction
fun emoji(shortcode: String) = emojiCatalog.replaceShortcodes(":$shortcode:").wrappedAsValue()

/**
 * Provides a dictionary of all available emojis, mapping the emoji character to its shortest shortcode.
 * ```
 * .foreach {.allemojis}
 *     emoji shortcode:
 *     The emoji .emoji has the shortcode .shortcode
 * ```
 *
 * > Did you know?
 * > [quarkdown.com/docs/emoji-list](https://quarkdown.com/docs/emoji-list) is generated from this function!
 *
 * @return a dictionary where keys are emoji characters and values are their shortest shortcodes
 * @wiki emojis
 */
@QFunction
@Name("allemojis")
fun allEmojis(): DictionaryValue<StringValue> =
    allEmojis
        .associate { it.details.string to (it.details.aliases.minByOrNull { alias -> alias.length } ?: "").wrappedAsValue() }
        .toMutableMap()
        .let(::DictionaryValue)
