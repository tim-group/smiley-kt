import java.net.URI

plugins {
    kotlin("jvm") version "1.2.40" apply false

    id("com.timgroup.jarmangit") version "1.1.86" apply false
    id("com.github.johnrengelman.shadow") version "2.0.3" apply false
    id("com.timgroup.productstore") version "1.0.3" apply false
}

val buildNumber: String? by extra(System.getenv("ORIGINAL_BUILD_NUMBER") ?: System.getenv("BUILD_NUMBER"))
val githubUrl: URI by extra(URI("https://github.com/tim-group/smiley-kt"))

allprojects {
    group = "com.timgroup"
    if (buildNumber != null) version = "1.0.$buildNumber"
}

val jacksonVersion by extra("2.9.4")
val metricsVersion by extra("4.0.2")
val jettyVersion by extra("9.4.9.v20180320")
val kotlinCoroutinesVersion by extra("0.22.5")
