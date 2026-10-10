package com.quarkdown.pm.model

data class PackageManifest(
    val atom: AtomManifest,
)

/**
 *
 */
data class AtomManifest(
    val name: String,
    val version: String,
    val description: String? = null,
    val authors: List<String> = emptyList(),
    val minQuarkdownVersion: String? = null,
    val categories: List<String> = emptyList(),
    val repository: String? = null,
    val license: String? = null,
)
