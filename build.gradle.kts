import java.net.URI

plugins {
    kotlin("jvm") version "1.2.60" apply false

    id("com.timgroup.jarmangit") version "1.1.86" apply false
    id("com.github.johnrengelman.shadow") version "2.0.3" apply false
    id("com.timgroup.productstore") version "1.0.3" apply false

    id("org.jetbrains.dokka") version "0.9.16" apply false
}

val buildNumber: String? by extra(System.getenv("ORIGINAL_BUILD_NUMBER") ?: System.getenv("BUILD_NUMBER"))
val githubUrl: URI by extra(URI("https://github.com/tim-group/smiley-kt"))

allprojects {
    group = "com.timgroup"
    if (buildNumber != null) version = "1.0.$buildNumber"
}

val versions = mapOf(
        "jackson" to "2.9.4",
        "metrics" to "4.0.2",
        "jetty" to "9.4.11.v20180605",
        "kotlinCoroutines" to "0.24.0",
        "guava" to "26.0-jre"
)

versions.forEach { t, u -> ext[t + "Version"] = u }
