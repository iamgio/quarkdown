plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation("org.assertj:assertj-core:3.26.3")
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    implementation("org.apache.commons:commons-text:1.13.0")
    implementation("com.github.ajalt.colormath:colormath:3.6.1")
    implementation("com.github.fracpete:romannumerals4j:0.0.1")
}
