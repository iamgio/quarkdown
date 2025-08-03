@file:Suppress("FunctionName")

package com.quarkdown.core.graph

import com.quarkdown.core.graph.adapter.JGraphAdapter

/**
 * @return a new directed [Graph]
 */
fun <T> DirectedGraph(): Graph<T> = JGraphAdapter(emptyGraphSupplier = { JGraphAdapter.directed() })
