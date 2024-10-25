plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation("org.assertj:assertj-core:3.26.3")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.commons:commons-text:1.11.0")
    implementation("com.github.ajalt.colormath:colormath:3.6.0")
    implementation("com.github.fracpete:romannumerals4j:0.0.1")
}
