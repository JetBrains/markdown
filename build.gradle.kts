import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.io.ByteArrayOutputStream

val kotlin_version = "1.4.10"
val markdown_version = "0.2.0.pre-${findProperty("buildNumber") ?: "SNAPSHOT"}"

buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }

    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
    }
}

plugins {
    kotlin("multiplatform") version "1.4.10"
    `maven-publish`
}

group = "org.jetbrains"
version = markdown_version

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

task("performanceTest", type = Test::class) {
    val testCompilation = kotlin.jvm().compilations["test"]

    group = "verification"
    setTestClassesDirs(testCompilation.output.classesDirs)
    setClasspath(testCompilation.runtimeDependencyFiles)
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

task("specRunnerJar", type = Jar::class) {
    from(kotlin.sourceSets["commonMain"].kotlin.classesDirectory)
    from(kotlin.sourceSets["jvmMain"].kotlin.classesDirectory)
    archiveName = "markdown-test.jar"
    manifest {
        attributes(
            mapOf(
                "Main-Class" to "org.intellij.markdown.SpecRunner",
                "Class-Path" to "TODO()" //configurations.testRuntime.join(" ")
            )
        )
    }
}

tasks {
    val runSpec by registering(Exec::class) {
        group = "verification"
        dependsOn("downloadCommonmark", "specRunnerJar")
        executable("python3")
        workingDir("commonmark-spec")
        args("test/spec_tests.py", "-p", "../run_html_gen.sh")
    }
}

publishing {
    publications.apply {
        (findByName("kotlinMultiplatform") as MavenPublication).apply {
            artifactId = "markdown"
            setUpPublication()
        }
        (findByName("jvm") as MavenPublication).apply {
            artifactId = "markdown-jvm"
            setUpPublication()
        }
        (findByName("js") as MavenPublication).apply {
            artifactId = "markdown-js"
            setUpPublication()
        }
        (findByName("metadata") as MavenPublication).apply {
            artifactId = "markdown-metadata"
            setUpPublication()
        }
    }
    repositories {
        maven {
            val userOrg = "jetbrains"
            val repo = "markdown"
            val name = "markdown"
            setUrl("https://api.bintray.com/maven/$userOrg/$repo/$name/;publish=0")
            credentials {
                username = findProperty("bintrayUser").toString()
                password = findProperty("bintrayKey").toString()
            }
        }
    }
}

fun MavenPublication.setUpPublication() {
    groupId = "org.jetbrains"
    version = markdown_version

    pom {
        name.set("markdown")
        description.set("Markdown parser in Kotlin")
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        url.set("https://github.com/valich/intellij-markdown")
        scm {
            url.set("https://github.com/valich/intellij-markdown")
        }
        developers {
            developer {
                id.set("valich")
                name.set("Valentin Fondaratov")
                email.set("fondarat@gmail.com")
                organization.set("JetBrains")
                organizationUrl.set("https://jetbrains.com")
            }
        }
    }
}
