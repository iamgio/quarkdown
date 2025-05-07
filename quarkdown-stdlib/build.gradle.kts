import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import java.time.Year

plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(project(":quarkdown-core"))
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.10.0")
    implementation("org.jetbrains.dokka:dokka-base:2.0.0")
    dokkaPlugin(project(":quarkdown-quarkdoc"))
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:2.0.0")
    }
}

// The following might become a Gradle plugin in the future.
tasks.dokkaHtml.configure {
    fun asset(path: String): File = project(":quarkdown-quarkdoc").projectDir.resolve("src/main/resources/$path")

    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        val year = Year.now().value
        footerMessage = "&copy; $year Quarkdown"
        customAssets = customAssets + asset("assets/images").listFiles()!!
        customStyleSheets = customStyleSheets + listOf(asset("styles/stylesheet.css"))
    }
}
