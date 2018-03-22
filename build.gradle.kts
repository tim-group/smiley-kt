import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.timgroup.gradle.productstore.ProductStorePublication
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import java.net.URI

plugins {
    application
    kotlin("jvm") version "1.2.30"
    id("com.timgroup.jarmangit") version "1.1.86"
    id("com.github.johnrengelman.shadow") version "2.0.2"
    id("com.timgroup.productstore") version "1.0.3"
}

val buildNumber: String? by extra(System.getenv("ORIGINAL_BUILD_NUMBER") ?: System.getenv("BUILD_NUMBER"))
val githubUrl: URI by extra(URI("https://github.com/tim-group/smiley-kt"))

group = "com.timgroup"
if (buildNumber != null) version = "1.0.${buildNumber}"

application {
    mainClassName = "com.timgroup.smileykt.Launcher"
}

tasks {
    "run"(JavaExec::class) {
        args("config.properties")
    }

    "test" {
        dependsOn(":webui:assemble")
    }

    "distZip" {
        enabled = false
    }

    "distTar" {
        enabled = false
    }

    val sourcesJar by creating(Jar::class) {
        classifier = "sources"
        from(java.sourceSets["main"].allSource)
    }

    val shadowJar by getting(ShadowJar::class) {
        manifest {
            attributes(mapOf("X-Java-Version" to "9", "Main-Class" to application.mainClassName))
        }
    }

    "assemble" {
        dependsOn(sourcesJar)
        dependsOn(shadowJar)
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.encoding = "UTF-8"
    options.isIncremental = true
    options.isDeprecation = true
    options.compilerArgs.add("-parameters")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

repositories {
    jcenter()
    maven(url = "http://repo.net.local/nexus/content/groups/public")
}

val jacksonVersion by extra("2.9.4")
val metricsVersion by extra("3.2.6")
val jettyVersion by extra("9.4.8.v20171121")
val kotlinCoroutinesVersion by extra("0.22.3")

dependencies {
    compile("com.timgroup:Tucker:1.0.1496") // autobump
    compile("com.timgroup:tim-logger:1.5.1086") // autobump
    compile("com.timgroup:tim-structured-events:0.4.1237") // autobump
    compile("com.timgroup:eventstore-api:0.0.1578") // autobump
    compile("com.timgroup:eventstore-filesystem:0.0.1578") // autobump
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    compile("io.dropwizard.metrics:metrics-core:$metricsVersion")
    compile("io.dropwizard.metrics:metrics-jvm:$metricsVersion")
    compile("io.dropwizard.metrics:metrics-graphite:$metricsVersion")
    compile("org.eclipse.jetty:jetty-server:$jettyVersion")
    compile("org.eclipse.jetty:jetty-servlet:$jettyVersion")
    compile("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    compile("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:$jacksonVersion")
    compile("com.google.guava:guava:24.1-jre")
    compile("org.jboss.resteasy:resteasy-jaxrs:3.1.2.Final")
    compile("javax.mail:mail:1.4.6")

    testCompile(kotlin("test-junit"))
    testCompile("com.natpryce:hamkrest:1.4.2.2")
    testCompile("org.araqnid:hamkrest-json:1.0.3")
    testCompile("com.timgroup:tim-structured-events-testing:0.4.1237") // autobump
    testCompile("com.timgroup:eventstore-memory:0.0.1578") // autobump
    testCompile("org.apache.httpcomponents:httpclient:4.5.5")
    testCompile("com.timgroup:clocks-testing:1.0.1080") // autobump

    runtime("ch.qos.logback:logback-classic:1.2.3")
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

publishing {
    (publications) {
        "productStore"(ProductStorePublication::class) {
            application = "smiley-kt"
            artifact(tasks["shadowJar"])
            from(components["java"])
        }
    }
}