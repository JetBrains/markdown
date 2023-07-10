rootProject.name = "markdown"

include(":docs")

pluginManagement {
  plugins {
    kotlin("multiplatform") version "1.7.22" apply false
    id("org.jetbrains.dokka") version "1.4.32" apply false
  }
}
