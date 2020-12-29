import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.configureBintrayPublicationIfNecessary
import org.jetbrains.configureSonatypePublicationIfNecessary
import org.jetbrains.registerPublicationFromKotlinPlugin

plugins {
    kotlin("multiplatform") version "1.4.10"
    id("org.jetbrains.dokka") version "1.4.20"
    id("com.jfrog.bintray")
    `maven-publish`
    signing
}

group = "org.jetbrains"
val baseVersion = project.property("version").toString()
version = if (project.property("snapshot")?.toString()?.toBoolean() != false) {
    baseVersion.substringBefore("-").split('.').let { (major, minor, patch) ->
        "$major.$minor.${patch + 1}-SNAPSHOT"
    }
} else {
    baseVersion
}


repositories {
    jcenter()
    mavenCentral()
}

kotlin {
    jvm {
        compilations {
            all {
                kotlinOptions.jvmTarget = "1.6"
            }
            val main by getting

            val test by getting

            val specRunner by compilations.creating {
                defaultSourceSet {
                    dependencies {
                        implementation(
                                main.compileDependencyFiles
                                        + main.output.classesDirs
                                        + test.output.classesDirs
                        )
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
    js(LEGACY) {
        nodejs {
            binaries.executable()
        }
    }
    sourceSets {
        val commonMain by getting {

        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {

        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting {

        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
                implementation("org.jetbrains.kotlinx:kotlinx-nodejs:0.0.7")
            }
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


    val downloadCommonmark by registering(Exec::class) {
        onlyIf { !File("CommonMark").exists() }
        executable("git")
        args("clone", "https://github.com/jgm/CommonMark")
    }

    val specRunnerJar by registering(Jar::class) {
        from(kotlin.sourceSets["commonMain"].kotlin.classesDirectory)
        from(kotlin.sourceSets["jvmMain"].kotlin.classesDirectory)
        archiveFileName.set("markdown-test.jar")
        manifest {
            attributes(
                    mapOf(
                            "Main-Class" to "org.intellij.markdown.SpecRunner",
                            "Class-Path" to "TODO()" //configurations.testRuntime.join(" ")
                    )
            )
        }
    }

    register<Exec>("runSpec") {
        group = "verification"
        dependsOn(downloadCommonmark, specRunnerJar)
        executable("python3")
        workingDir("CommonMark")
        args("test/spec_tests.py", "-p", "../run_html_gen.sh")
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
    "metadata" to "markdown-metadata"
)

publicationsToArtifacts.forEach { publicationName, artifactId ->
    registerPublicationFromKotlinPlugin(publicationName, artifactId)
    configureSonatypePublicationIfNecessary(publicationName)
}
configureBintrayPublicationIfNecessary()
