import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.configureBintrayPublicationIfNecessary
import org.jetbrains.configureSonatypePublicationIfNecessary
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.registerPublicationFromKotlinPlugin
import org.jetbrains.signPublicationsIfNecessary
import java.io.ByteArrayOutputStream

plugins {
    kotlin("multiplatform") apply true
    id("org.jetbrains.dokka") apply true
    `maven-publish`
    signing
}

fun Project.obtainProjectVersion(): String {
    val baseVersion = property("version").toString()
    val isSnapshot = property("snapshot")?.toString()?.toBoolean() != false
    if (!isSnapshot) {
        return baseVersion
    }
    val (major, minor, patch) = baseVersion.substringBefore("-").split('.')
    return "$major.$minor.${patch.toInt() + 1}-SNAPSHOT"
}

group = "org.jetbrains"
version = obtainProjectVersion()

repositories {
    mavenCentral()
}

kotlin {
    targets.all {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    languageVersion.set(KotlinVersion.KOTLIN_2_0)
                    apiVersion.set(KotlinVersion.KOTLIN_2_0)
                }
            }
        }
    }
    jvm {
        compilations {
            all {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_1_8)
                    }
                }
            }
        }

        testRuns["test"].executionTask.configure {
            useJUnit {
                excludeCategories("org.intellij.markdown.ParserPerformanceTest")
            }
        }
    }
    js(IR) {
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
    }
    linuxX64()
    linuxArm64()
    mingwX64()
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    watchosSimulatorArm64()
    tvosSimulatorArm64()
    ios()

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.experimental.ExperimentalNativeApi")
            }
        }
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val fileBasedTest by creating {
            dependsOn(commonTest)
        }
        val jvmTest by getting {
            dependsOn(fileBasedTest)
        }
        val jsTest by getting {
            dependsOn(fileBasedTest)
        }
        val wasmJsTest by getting {
            dependsOn(fileBasedTest)
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val nativeTest by creating {
            dependsOn(fileBasedTest)
        }
        val nativeSourceSets = listOf(
            "linuxX64",
            "linuxArm64",
            "mingwX64",
            "macosX64",
            "macosArm64",
            "ios",
            "iosSimulatorArm64",
            "watchosSimulatorArm64",
            "tvosSimulatorArm64"
        ).map { "${it}Main" }
        for (set in nativeSourceSets) {
            named(set) {
                dependsOn(nativeMain)
            }
        }
        val nativeTestSourceSets = listOf(
            "linuxX64",
            "linuxArm64",
            "mingwX64",
            "macosX64",
            "macosArm64"
        ).map { "${it}Test" }
        for (set in nativeTestSourceSets) {
            named(set) {
                dependsOn(nativeTest)
                dependsOn(fileBasedTest)
            }
        }
    }
}

// Need to compile using a canary version of Node due to
// https://youtrack.jetbrains.com/issue/KT-63014
rootProject.the<NodeJsRootExtension>().apply {
    version = "22.0.0-nightly202404032241e8c5b3"
    downloadBaseUrl = "https://nodejs.org/download/nightly"
}

tasks.withType<KotlinNpmInstallTask>().configureEach {
    args.add("--ignore-engines")
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

    val downloadCommonmark by registering(Exec::class) {
        group = "Code Generation"
        description = "Clone the CommonMark repo locally"
        onlyIf { !File("commonmark-spec").exists() }
        executable("git")
        args("clone", "https://github.com/commonmark/commonmark-spec")
    }
    val downloadGfm by registering(Exec::class) {
        group = "Code Generation"
        description = "Clone the GFM repo locally"
        onlyIf { !File("cmark-gfm").exists() }
        executable("git")
        args("clone", "https://github.com/github/cmark-gfm")
    }
    val generateCommonMarkTest by registering(Exec::class) {
        group = "Code Generation"
        description = "Generate unit tests for the CommonMark spec"
        dependsOn(downloadCommonmark)
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
    val generateGfmTest by registering(Exec::class) {
        group = "Code Generation"
        description = "Generate unit tests for the GFM spec"
        dependsOn(downloadGfm)
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
    val generateAllTests by registering {
        group = "Code Generation"
        description = "Generate unit tests for the all markdown specs"
        dependsOn(generateCommonMarkTest, generateGfmTest)
    }
}

val dokkaOutputDir: File
    get() = project.buildDir.resolve("dokkaHtml")

subprojects {
    tasks.withType<DokkaTask> {
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
    "wasmJs" to "markdown-wasm-js",
    "linuxX64" to "markdown-linuxx64",
    "linuxArm64" to "markdown-linuxarm64",
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

for ((publication, artifact) in publicationsToArtifacts) {
    registerPublicationFromKotlinPlugin(publication, artifact)
}
signPublicationsIfNecessary(*publicationsToArtifacts.keys.toTypedArray())
configureSonatypePublicationIfNecessary()
configureBintrayPublicationIfNecessary()

tasks.withType<AbstractPublishToMaven>().configureEach {
    val signingTasks = tasks.withType<Sign>()
    mustRunAfter(signingTasks)
}
