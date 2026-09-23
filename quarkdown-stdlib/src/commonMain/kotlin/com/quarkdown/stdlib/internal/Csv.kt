package com.quarkdown.stdlib.internal

/**
 * Parses CSV content whose first row is the header.
 * @param content raw CSV bytes
 * @return one map per data row, associating each header to the row's cell, in column order
 * @throws com.quarkdown.core.platform.UnsupportedPlatformOperationException on platforms without a CSV parser
 */
internal expect fun parseCsvWithHeader(content: ByteArray): List<Map<String, String>>
