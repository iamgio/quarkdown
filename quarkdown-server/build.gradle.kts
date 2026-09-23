plugins {
    id("quarkdown.jvm")
}

dependencies {
    implementation(project(":quarkdown-core"))
    implementation(project(":quarkdown-template"))
    implementation(project(":quarkdown-interaction"))

    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.sse)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation("org.slf4j:slf4j-simple:2.0.19")
}
