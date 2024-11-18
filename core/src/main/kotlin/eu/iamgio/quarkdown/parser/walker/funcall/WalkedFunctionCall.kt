package eu.iamgio.quarkdown.parser.walker.funcall

/**
 *
 */
data class WalkedArgument(val name: String?, val value: String, val isBody: Boolean)

data class WalkedFunctionCall(
    val name: String,
    val arguments: List<WalkedArgument>,
    val bodyArgument: WalkedArgument?,
)
