package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.function.value.StringValue

fun test() = StringValue("Test from function!!!")

fun greet(name: String) = StringValue("Hello $name")
