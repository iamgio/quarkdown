plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "quarkdown"

include("core")
include("cli")
include("stdlib")
include("test")
include("libs")
include("server")
include("interaction")
include("pdf")
