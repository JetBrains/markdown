package org.jetbrains

import org.gradle.api.Project

internal enum class PublicationChannel {
    Bintray,
    MavenCentral,
    MavenCentralSnapshot;

    val isBintrayRepository
        get() = this == Bintray

    val isMavenRepository
        get() = when (this) {
            MavenCentral, MavenCentralSnapshot -> true
            else -> false
        }

    companion object {
        fun fromPropertyString(value: String) = when (value) {
            "bintray" -> Bintray
            "maven-central" -> MavenCentral
            "maven-central-snapshot" -> MavenCentralSnapshot
            else -> throw IllegalArgumentException("Unknown publication_channel=$value")
        }
    }
}

internal val Project.publicationChannels: Set<PublicationChannel>
    get() = properties["publication_channels"]
            ?.toString()
            ?.split(",")
            ?.map { channel -> PublicationChannel.fromPropertyString(channel) }
            ?.toSet()
            .orEmpty()
