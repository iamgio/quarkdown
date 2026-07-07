@file:QModule

package com.quarkdown.stdlib

import com.quarkdown.core.ast.MarkdownContent
import com.quarkdown.core.ast.base.block.list.ListBlock
import com.quarkdown.core.ast.quarkdown.block.FileTree
import com.quarkdown.core.ast.quarkdown.inline.Keybinding
import com.quarkdown.core.function.reflect.annotation.Body
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.core.util.trimEntries
import com.quarkdown.processor.annotation.Name
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule
import com.quarkdown.stdlib.internal.fileTreeFromList

/**
 * Creates a visual file tree from a Markdown list.
 * Each inline item is rendered as a file, and each nested list as a directory.
 *
 * To explicitly mark an item as a directory, add a trailing slash (`/`).
 * This is useful for empty directories that have no nested children.
 * The slash marker is not shown in the rendered output.
 *
 * Example:
 * ```
 * .filetree
 *     - src
 *       - main.ts
 *       - utils.ts
 *     - target/
 *     - README.md
 * ```
 * @param content body content containing a Markdown list that defines the file tree structure
 * @return the generated [FileTree] node
 */
@QFunction
@Name("filetree")
fun fileTree(
    @Body content: MarkdownContent,
): NodeValue {
    val rawListNode =
        content.children.firstOrNull() as? ListBlock
            ?: throw IllegalArgumentException("Content of file tree must be a list.")

    return FileTree(fileTreeFromList(rawListNode)).wrappedAsValue()
}

/**
 * Displays a keyboard shortcut or key combination.
 * Keys are displayed with platform-appropriate symbols: on macOS,
 * modifier keys use their native symbols (e.g. `⌘` for Command, `⌥` for Option).
 *
 * Keys are separated by `+`, `,`, or `-` delimiters.
 *
 * Recognized modifier aliases:
 * - Primary modifier (`Ctrl`/`⌘`): `cmd`, `command`, `meta`, `mod`
 * - Ctrl: `ctrl`, `control`
 * - Alt/Option: `alt`, `option`
 * - Shift: `shift`
 *
 * Since `+`, `,`, and `-` are used as delimiters,
 * their literal key equivalents are `plus`, `comma`, and `dash`.
 *
 * Any other key name is displayed as-is, capitalized.
 *
 * Example:
 * ```markdown
 * Press .keybinding {Ctrl+Shift+K} to delete the line.
 *
 * Press .keybinding {Cmd+C} to copy.
 *
 * Press .keybinding {Ctrl+plus} to zoom in.
 * ```
 *
 * @param input the key combination string, with keys separated by `+`, `,`, or `-`
 * @return a wrapped [Keybinding] node
 * @throws IllegalArgumentException if the input does not contain any key
 */
@QFunction
fun keybinding(input: String): NodeValue {
    val keys = input.split(Regex("[+,-]")).trimEntries()

    require(keys.isNotEmpty()) { "At least one key must be specified." }

    val parts: List<Keybinding.Part> =
        keys.map {
            when (it.lowercase()) {
                "cmd", "command", "meta", "mod" -> Keybinding.PrimaryModifier
                "ctrl", "control" -> Keybinding.CtrlModifier
                "alt", "option" -> Keybinding.AltModifier
                "shift" -> Keybinding.ShiftModifier
                "plus" -> Keybinding.Key("+")
                "comma" -> Keybinding.Key(",")
                "dash", "minus" -> Keybinding.Key("-")
                "dot", "period" -> Keybinding.Key(".")
                else -> Keybinding.Key(it.replaceFirstChar(Char::titlecase))
            }
        }
    return Keybinding(parts).wrappedAsValue()
}
