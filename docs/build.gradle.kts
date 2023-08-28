import org.jetbrains.dokka.Platform
import org.jetbrains.dokka.gradle.DokkaTask

/**
 * This is a subproject due to a Gradle bug that substitutes Dokka's dependency on `org.jetbrains:markdown`
 * to this local project when applied in the root project
 */

plugins {
    id("org.jetbrains.dokka") apply true
}

repositories {
    mavenCentral()
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        val common by registering {
            sourceRoot("../src/commonMain")
            platform = Platform.common
        }
        register("jvm") {
            sourceRoot("../src/jvmMain")
            dependsOn(common.name)
            platform = Platform.jvm
        }
        register("js") {
            sourceRoot("../src/jsMain")
            dependsOn(common.name)
            platform = Platform.js
        }
    }
}
