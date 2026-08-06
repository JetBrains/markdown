import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.configureBintrayPublicationIfNecessary
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.registerPublicationFromKotlinPlugin
import org.jetbrains.signPublicationsIfNecessary
import java.io.ByteArrayOutputStream
import java.util.Base64
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.configureSonatypePublicationIfNecessary
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.squareup.okhttp3:okhttp:4.12.0")
    }
}

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
    maven("https://cache-redirector.jetbrains.com/repo1.maven.org/maven2")
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

// --- Sonatype Central Portal publishing (see https://central.sonatype.org/publish/publish-portal-api/) ---

fun base64Auth(userName: String, accessToken: String): String =
    Base64.getEncoder().encode("$userName:$accessToken".toByteArray()).toString(Charsets.UTF_8)

fun deployToCentralPortal(
    bundleFile: File,
    uriBase: String,
    isUserManaged: Boolean,
    deploymentName: String,
    userName: String,
    accessToken: String
): String {
    val publishingType = if (isUserManaged) "USER_MANAGED" else "AUTOMATIC"
    val uri = uriBase.trimEnd('/') + "/api/v1/publisher/upload?name=$deploymentName&publishingType=$publishingType"
    val base64Auth = base64Auth(userName, accessToken)

    println("Sending request to $uri...")

    val client = OkHttpClient()
    val request = Request.Builder()
        .url(uri)
        .header("Authorization", "Bearer $base64Auth")
        .post(
            MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("bundle", bundleFile.name, bundleFile.asRequestBody())
                .build()
        )
        .build()
    val response = client.newCall(request).execute()

    val statusCode = response.code
    println("Upload status code: $statusCode")
    val uploadResult = response.body!!.string()
    println("Upload result: $uploadResult")
    if (statusCode == 201) {
        return uploadResult
    } else {
        error("Upload error to Central repository. Status code $statusCode.")
    }
}

fun waitForUploadToSucceed(
    uriBase: String,
    deploymentId: String,
    isUserManaged: Boolean,
    userName: String,
    accessToken: String,
    maxTimeout: Duration,
    minTimeBetweenAttempts: Duration
) {
    val uri = uriBase.trimEnd('/') + "/api/v1/publisher/status?id=$deploymentId"
    val base64Auth = base64Auth(userName, accessToken)

    var timeSpent = Duration.ZERO
    var attemptNumber = 1
    var terminatingState = false

    println("Polling for deployment status for $maxTimeout: $uri")

    while (timeSpent < maxTimeout) {
        val remainingTime = maxTimeout - timeSpent
        println("Polling attempt ${attemptNumber++}, remaining time $remainingTime.")

        val client = OkHttpClient().newBuilder()
            .callTimeout(remainingTime.toJavaDuration())
            .build()

        val beforeMs = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url(uri)
                .header("Authorization", "Bearer $base64Auth")
                .post("".toRequestBody())
                .build()
            val response = client.newCall(request).execute()
            val code = response.code
            if (code != 200) {
                error("Response code $code: ${response.body?.string()}")
            }

            val jsonResult = JsonSlurper().parse(response.body?.bytes() ?: error("Empty response body.")) as Map<*, *>
            val state = jsonResult["deploymentState"]
            println("Current state: $state.")

            when (state) {
                "PENDING", "VALIDATING", "PUBLISHING" -> {}
                "VALIDATED" -> {
                    terminatingState = true

                    if (isUserManaged) {
                        println("Validated successfully.")
                        return
                    }

                    error("State error: deployment is not user managed, but signals it requires a UI interaction.")
                }
                "PUBLISHED" -> {
                    terminatingState = true

                    if (!isUserManaged) {
                        println("Published successfully.")
                        return
                    }

                    error("State error: deployment is user managed, but signals it has been published.")
                }
                "FAILED" -> {
                    terminatingState = true

                    val errors = jsonResult["errors"]
                    val errorsAsString = JsonBuilder(errors).toPrettyString()
                    error("Deployment failed. Errors: $errorsAsString")
                }
                else -> logger.warn("Unknown deployment state: $state")
            }
        } catch (e: Exception) {
            if (terminatingState) {
                throw e
            }

            logger.warn("Error during HTTP request: ${e.message}")
        } finally {
            val afterMs = System.currentTimeMillis()
            var attemptTime = (afterMs - beforeMs).coerceAtLeast(0L).milliseconds
            if (attemptTime < minTimeBetweenAttempts) {
                val sleepTime = minTimeBetweenAttempts - attemptTime
                Thread.sleep(sleepTime.inWholeMilliseconds)
                attemptTime = minTimeBetweenAttempts
            }

            timeSpent += attemptTime
        }
    }
}

val packSonatypeCentralBundle by tasks.registering(Zip::class) {
    group = "publishing"
    dependsOn("publishAllPublicationsToArtifactsRepository")
    from(layout.buildDirectory.dir("artifacts/maven"))
    archiveFileName.set("bundle.zip")
    destinationDirectory.set(layout.buildDirectory)
}

tasks.register("publishMavenToCentralPortal") {
    group = "publishing"
    dependsOn(packSonatypeCentralBundle)
    doLast {
        val uriBase = "https://central.sonatype.com"
        val userName = System.getenv("SONATYPE_USER") ?: error("SONATYPE_USER is not set")
        val accessToken = System.getenv("SONATYPE_PASSWORD") ?: error("SONATYPE_PASSWORD is not set")
        val isUserManaged = true

        val deploymentId = deployToCentralPortal(
            bundleFile = packSonatypeCentralBundle.get().archiveFile.get().asFile,
            uriBase = uriBase,
            isUserManaged = isUserManaged,
            deploymentName = "markdown-$version",
            userName = userName,
            accessToken = accessToken
        )
        waitForUploadToSucceed(
            uriBase = uriBase,
            deploymentId = deploymentId,
            isUserManaged = isUserManaged,
            userName = userName,
            accessToken = accessToken,
            maxTimeout = 60.minutes,
            minTimeBetweenAttempts = 5.seconds
        )
    }
}
