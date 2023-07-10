import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.configureBintrayPublicationIfNecessary
import org.jetbrains.configureSonatypePublicationIfNecessary
import org.jetbrains.registerPublicationFromKotlinPlugin
import org.jetbrains.signPublicationsIfNecessary
import java.io.ByteArrayOutputStream

plugins {
    kotlin("multiplatform") apply true
    id("org.jetbrains.dokka") apply true
    `maven-publish`
    signing
}

group = "org.jetbrains"
val baseVersion = project.property("version").toString()
version = if (project.property("snapshot")?.toString()?.toBoolean() != false) {
    baseVersion.substringBefore("-").split('.').let { (major, minor, patch) ->
        "$major.$minor.${patch.toInt() + 1}-SNAPSHOT"
    }
} else {
    baseVersion
}


repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations {
            all {
                kotlinOptions.jvmTarget = "1.8"
            }
            val main by getting

            val test by getting
        }

        testRuns["test"].executionTask.configure {
            useJUnit {
                excludeCategories("org.intellij.markdown.ParserPerformanceTest")
            }
        }
    }
    js(BOTH) {
        nodejs {}
    }
    linuxX64()
    mingwX64()
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    watchosSimulatorArm64()
    tvosSimulatorArm64()
    ios()

    sourceSets {
        val commonMain by getting {

        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val fileBasedTest by creating {
            dependsOn(commonTest)
        }
        val jvmMain by getting {

        }
        val jvmTest by getting {
            dependsOn(fileBasedTest)
        }
        val jsMain by getting {

        }
        val jsTest by getting {
            dependsOn(fileBasedTest)
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        listOf("linuxX64", "mingwX64", "macosX64", "macosArm64", "ios", "iosSimulatorArm64",
            "watchosSimulatorArm64", "tvosSimulatorArm64").forEach { target ->
            getByName("${target}Main").dependsOn(nativeMain)
        }

        val nativeTest by creating {
            dependsOn(commonTest)
        }

        listOf("linuxX64", "mingwX64", "macosX64", "macosArm64").forEach { target ->
            val sourceSet = getByName("${target}Test")
            sourceSet.dependsOn(nativeTest)
            sourceSet.dependsOn(fileBasedTest)
        }
        val iosTest by getting {
        }
    }
}


tasks {
    register<Test>("performanceTest") {
        val testCompilation = kotlin.jvm().compilations["test"]

        group = "verification"
        testClassesDirs = testCompilation.output.classesDirs
        classpath = testCompilation.runtimeDependencyFiles
        dependsOn("compileTestKotlinJvm")
        useJUnit {
            includeCategories("org.intellij.markdown.ParserPerformanceTest")
        }
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
        }
    }


    task("downloadCommonmark", type = Exec::class) {
        group = "Code Generation"
        description = "Clone the CommonMark repo locally"
        onlyIf { !File("commonmark-spec").exists() }
        executable("git")
        args("clone", "https://github.com/commonmark/commonmark-spec")
    }

    task("downloadGfm", type = Exec::class) {
        group = "Code Generation"
        description = "Clone the GFM repo locally"
        onlyIf { !File("cmark-gfm").exists() }
        executable("git")
        args("clone", "https://github.com/github/cmark-gfm")
    }

    task("generateCommonMarkTest", type = Exec::class) {
        group = "Code Generation"
        description = "Generate unit tests for the CommonMark spec"
        dependsOn("downloadCommonmark")
        executable("python")
        workingDir("commonmark-spec")
        args("test/spec_tests.py", "--dump-tests")
        val output = ByteArrayOutputStream()
        standardOutput = output
        doLast {
            val tests = String(output.toByteArray())
            generateSpecTest(
                    tests,
                    "CommonMarkSpecTest",
                    "org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor"
            )
        }
    }

    task("generateGfmTest", type = Exec::class) {
        group = "Code Generation"
        description = "Generate unit tests for the GFM spec"
        dependsOn("downloadGfm")
        executable("python")
        workingDir("cmark-gfm/test")
        args("spec_tests.py", "--dump-tests")
        val output = ByteArrayOutputStream()
        standardOutput = output
        doLast {
            val tests = String(output.toByteArray())
            generateSpecTest(
                    tests,
                    "GfmSpecTest",
                    "org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor"
            )
        }
    }

    task("generateAllTests") {
        group = "Code Generation"
        description = "Generate unit tests for the all markdown specs"
        dependsOn("generateCommonMarkTest", "generateGfmTest")
    }
}

val dokkaOutputDir = project.buildDir.resolve("dokkaHtml")

subprojects {
    tasks.withType<org.jetbrains.dokka.gradle.DokkaTask> {
        outputDirectory.set(dokkaOutputDir)
    }
}

tasks.register<Jar>("javadocJar") {
    dependsOn(":docs:dokkaHtml")
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
}

val publicationsToArtifacts = mapOf(
        "kotlinMultiplatform" to "markdown",
        "jvm" to "markdown-jvm",
        "js" to "markdown-js",
        "linuxX64" to "markdown-linuxx64",
        "mingwX64" to "markdown-mingwx64",
        "macosX64" to "markdown-macosx64",
        "macosArm64" to "markdown-macosarm64",
        "iosX64" to "markdown-iosx64",
        "iosArm64" to "markdown-iosarm64",
        "iosSimulatorArm64" to "markdown-iossimulatorarm64",
        "watchosSimulatorArm64" to "markdown-watchossimulatorarm64",
        "tvosSimulatorArm64" to "markdown-tvossimulatorarm64",
        "metadata" to "markdown-metadata"
)

publicationsToArtifacts.forEach { publicationName, artifactId ->
    registerPublicationFromKotlinPlugin(publicationName, artifactId)
}
signPublicationsIfNecessary(*publicationsToArtifacts.keys.toTypedArray())
configureSonatypePublicationIfNecessary()
configureBintrayPublicationIfNecessary()
