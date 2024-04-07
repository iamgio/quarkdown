package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.function.value.StringValue

fun test(x: Int = 0) = StringValue("Test $x from function!!!")

fun greet(name: String) = StringValue("Hello $name")
