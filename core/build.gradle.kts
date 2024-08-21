plugins {
    kotlin("jvm") version "1.8.21"
}

dependencies {
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.commons:commons-text:1.11.0")
}
