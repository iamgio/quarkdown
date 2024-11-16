package eu.iamgio.quarkdown.parser.walker.funcall

import eu.iamgio.quarkdown.parser.walker.WalkerParser

class FunctionCallWalkerParser(source: CharSequence, allowsBody: Boolean) : WalkerParser<WalkedFunctionCall>(
    source,
    FunctionCallGrammar(allowsBody),
)
