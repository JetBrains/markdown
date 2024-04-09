rootProject.name = "markdown"

include(":docs")

pluginManagement {
  plugins {
    kotlin("multiplatform") version "2.0.0" apply false
    id("org.jetbrains.dokka") version "1.8.20" apply false
  }
}
