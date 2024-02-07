plugins {
    kotlin("jvm") version "1.8.21"
    application
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.apache.logging.log4j:log4j-core:2.22.1")
}

application {
    mainClass.set("eu.iamgio.quarkdown.QuarkdownKt")
}
