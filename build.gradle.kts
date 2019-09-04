import java.net.URI

buildscript {
    repositories {
        jcenter()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-frontend-plugin:0.0.45")
    }
}

plugins {
    kotlin("jvm") version "1.3.50" apply false

    id("com.timgroup.jarmangit") version "1.1.113" apply false
    id("com.github.johnrengelman.shadow") version "5.1.0" apply false
    id("com.timgroup.productstore") version "1.0.18" apply false
    id("com.timgroup.webpack") version "1.0.61" apply false

    id("org.jetbrains.dokka") version "0.9.16" apply false
}

val buildNumber: String? by extra(System.getenv("ORIGINAL_BUILD_NUMBER") ?: System.getenv("BUILD_NUMBER"))
val githubUrl: URI by extra(URI("https://github.com/tim-group/smiley-kt"))

allprojects {
    group = "com.timgroup"
    if (buildNumber != null) version = "1.0.$buildNumber"
}
