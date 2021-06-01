import org.jetbrains.dokka.Platform

/**
 * This is a subproject due to a Gradle bug that substitutes Dokka's dependency on `org.jetbrains:markdown`
 * to this local project when applied in the root project
 */

plugins {
    id("org.jetbrains.dokka")
}

repositories {
    mavenCentral()
}

tasks {
    dokkaHtml {
        dokkaSourceSets {
            val common by registering {
                sourceRoot("../src/commonMain")
                platform.set(Platform.common)
            }

            register("jvm") {
                sourceRoot("../src/jvmMain")
                dependsOn(common)
                platform.set(Platform.jvm)
            }

            register("js") {
                sourceRoot("../src/jsMain")
                dependsOn(common)
                platform.set(Platform.js)
            }
        }
    }
}
