rootProject.name = "markdown"

include(":docs")

pluginManagement {
  repositories {
    maven("https://cache-redirector.jetbrains.com/plugins.gradle.org/m2")
    maven("https://cache-redirector.jetbrains.com/repo1.maven.org/maven2")
  }
  plugins {
    kotlin("multiplatform") version "2.0.0" apply false
    id("org.jetbrains.dokka") version "1.8.20" apply false
  }
}
