package com.quarkdown.core.ast.quarkdown.inline

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A keyboard shortcut or key combination, rendered as a sequence of key labels.
 * Each [Part] represents a single key or modifier in the combination.
 * @param parts the ordered list of keys and modifiers in the combination
 */
class Keybinding(
    val parts: List<Part>,
) : Node {
    /**
     * A single part of a keybinding, representing either a regular key or a modifier key.
     */
    sealed interface Part {
        /**
         * The display name of this part.
         */
        val displayName: String

        /**
         * The display name of this part on macOS. By default, this is the same as [displayName].
         */
        val macDisplayName: String get() = displayName
    }

    /**
     * A regular (non-modifier) key.
     * @param name the display name of this key, as-is
     */
    data class Key(
        val name: String,
    ) : Part {
        override val displayName: String
            get() = name
    }

    /**
     * `Ctrl` on Windows/Linux, `Cmd` on macOS.
     */
    data object PrimaryModifier : Part {
        override val displayName: String
            get() = "Ctrl"

        override val macDisplayName: String
            get() = "\u2318"
    }

    /**
     * `Ctrl` on Windows/Linux/macOS.
     */
    data object CtrlModifier : Part {
        override val displayName: String
            get() = "Ctrl"

        override val macDisplayName: String
            get() = "\u2303"
    }

    /**
     * `Alt` on Windows/Linux, `Option` on macOS.
     */
    data object AltModifier : Part {
        override val displayName: String
            get() = "Alt"

        override val macDisplayName: String
            get() = "\u2325"
    }

    /**
     * `Shift` on Windows/Linux/macOS.
     */
    data object ShiftModifier : Part {
        override val displayName: String
            get() = "Shift"

        override val macDisplayName: String
            get() = "\u21E7"
    }

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
