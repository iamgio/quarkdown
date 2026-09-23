extra["noRuntime"] = true

plugins {
    id("quarkdown.jvm")
}

dependencies {
    api(project(":quarkdown-core"))
    api(kotlin("test"))
    api("org.assertj:assertj-core:3.27.6")
}
