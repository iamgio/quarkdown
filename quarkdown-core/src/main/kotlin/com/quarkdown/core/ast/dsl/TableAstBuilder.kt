package com.quarkdown.core.ast.dsl

import com.quarkdown.core.ast.base.block.Table

/**
 * A builder of table content.
 * @see BlockAstBuilder.table
 */
class TableAstBuilder : AstBuilder() {
    val columns = mutableListOf<Table.Column>()

    /**
     * @see Table.Column
     */
    fun column(
        header: InlineAstBuilder.() -> Unit,
        alignment: Table.Alignment = Table.Alignment.NONE,
        block: ColumnAstBuilder.() -> Unit,
    ) {
        val columnAstBuilder = ColumnAstBuilder().apply(block)
        columns += Table.Column(alignment, Table.Cell(buildInline(header)), columnAstBuilder.cells)
    }
}

/**
 * A builder of table columns.
 * @see TableAstBuilder.column
 */
class ColumnAstBuilder {
    val cells = mutableListOf<Table.Cell>()

    /**
     * @see Table.Cell
     */
    fun cell(block: InlineAstBuilder.() -> Unit) {
        cells += Table.Cell(buildInline(block))
    }
}
