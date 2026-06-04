package org.jetbrains

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.signing.SigningExtension

fun Project.registerPublicationFromKotlinPlugin(publicationName: String, artifactId: String) {
    configure<PublishingExtension> {
        publications {
            (findByName(publicationName) as? MavenPublication)?.apply {
                this.artifactId = artifactId

                artifact(tasks["javadocJar"])
                configurePom()
            }
        }
    }
}

fun Project.configureBintrayPublicationIfNecessary() {
    if (publicationChannels.any { it.isBintrayRepository }) {
        configure<PublishingExtension> {
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
    }
}

fun Project.signPublicationsIfNecessary(vararg publications: String) {
    signPublicationsIfKeyPresent(*publications)
}

fun Project.configureSonatypePublicationIfNecessary() {
    // Release flow: publish all publications into a local directory, which is then
    // zipped (packSonatypeCentralBundle) and uploaded to the Central Portal
    // (publishMavenToCentralPortal). No remote Maven repository is used.
    val artifactsDir = layout.buildDirectory.dir("artifacts/maven").get().asFile.toURI()
    configure<PublishingExtension> {
        repositories {
            maven {
                name = "artifacts"
                url = artifactsDir
            }
        }
    }
}

private fun Project.signPublicationsIfKeyPresent(vararg publications: String) {
    val signingKeyId = System.getenv("SIGN_KEY_ID")
    val signingKey = System.getenv("SIGN_KEY")
    val signingKeyPassphrase = System.getenv("SIGN_KEY_PASSPHRASE")

    if (!signingKey.isNullOrBlank()) {
        extensions.configure<SigningExtension>("signing") {
            if (signingKeyId?.isNotBlank() == true) {
                useInMemoryPgpKeys(signingKeyId, signingKey, signingKeyPassphrase)
            } else {
                useInMemoryPgpKeys(signingKey, signingKeyPassphrase)
            }
            publications.forEach { publicationName ->
                extensions.findByType(PublishingExtension::class)!!.publications.findByName(publicationName)?.let {
                    sign(it)
                }
            }
        }
    }
}


private fun MavenPublication.configurePom() {
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
        url.set("https://github.com/JetBrains/markdown")
        scm {
            url.set("https://github.com/JetBrains/markdown")
            connection.set("scm:git:git://github.com/JetBrains/markdown.git")
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
