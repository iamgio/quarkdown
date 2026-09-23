@file:Suppress("FunctionName")

package com.quarkdown.core.graph

/**
 * @return a new empty directed [Graph]
 */
fun <T> DirectedGraph(): Graph<T> = PersistentDirectedGraph()
