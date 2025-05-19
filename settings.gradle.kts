plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "quarkdown"

include("quarkdown-core")
include("quarkdown-html")
include("quarkdown-cli")
include("quarkdown-stdlib")
include("quarkdown-test")
include("quarkdown-libs")
include("quarkdown-server")
include("quarkdown-interaction")
include("quarkdown-pdf")
include("quarkdown-quarkdoc")
