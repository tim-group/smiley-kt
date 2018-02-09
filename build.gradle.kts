import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    application
    kotlin("jvm") version "1.2.21"
    id("com.timgroup.jarmangit") version "1.1.84"
}

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

    "assemble" {
        dependsOn(sourcesJar)
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

dependencies {
    compile("com.timgroup:Tucker:1.0.1495") // autobump
    compile("com.timgroup:tim-logger:1.5.1084") // autobump
    compile("com.timgroup:tim-structured-events:0.4.1235") // autobump
    compile("com.timgroup:eventstore-api:0.0.1565") // autobump
    compile("com.timgroup:eventstore-filesystem:0.0.1565") // autobump
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
    compile("com.google.guava:guava:24.0-jre")
    compile("org.jboss.resteasy:resteasy-jaxrs:3.1.2.Final")

    testCompile(kotlin("test-junit"))
    testCompile("com.natpryce:hamkrest:1.4.2.2")
    testCompile("org.araqnid:hamkrest-json:1.0.3")
    testCompile("com.timgroup:tim-structured-events-testing:0.4.1235") // autobump
    testCompile("com.timgroup:eventstore-memory:0.0.1565") // autobump
    testCompile("org.apache.httpcomponents:httpclient:4.4.1")
    testCompile("com.timgroup:clocks-testing:1.0.1080") // autobump

    runtime("ch.qos.logback:logback-classic:1.2.3")
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}
