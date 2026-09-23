package com.quarkdown.stdlib.internal

import com.jsoizo.kotlincsv.csvReader
import com.jsoizo.kotlincsv.reader.read
import com.jsoizo.kotlincsv.reader.withHeader
import kotlinx.io.Buffer
import kotlinx.io.write

internal actual fun parseCsvWithHeader(content: ByteArray): List<Map<String, String>> =
    csvReader().read(Buffer().apply { write(content) }) { rows -> rows.withHeader().toList() }
