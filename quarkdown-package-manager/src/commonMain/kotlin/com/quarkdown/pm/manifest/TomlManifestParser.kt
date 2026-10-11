package com.quarkdown.pm.manifest

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import com.quarkdown.pm.model.PackageManifest
import kotlinx.serialization.SerializationException

/**
 * [ManifestParser] for the `atom.toml` format, where package metadata lives in the `[atom]` table.
 * Unknown keys are ignored so that manifests written for newer Quarkdown versions still load.
 */
object TomlManifestParser : ManifestParser {
    private val toml =
        Toml(
            inputConfig = TomlInputConfig(ignoreUnknownNames = true),
        )

    override fun parse(source: String): PackageManifest =
        try {
            toml.decodeFromString(PackageManifest.serializer(), source)
        } catch (e: SerializationException) {
            throw ManifestParseException("Invalid package manifest: ${e.message}", e)
        }
}
