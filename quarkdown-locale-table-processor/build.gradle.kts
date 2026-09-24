extra["noRuntime"] = true

plugins {
    id("quarkdown.jvm")
}

dependencies {
    implementation(libs.ksp.symbol.processing.api)
}

tasks.test {
    useJUnitPlatform()
}
