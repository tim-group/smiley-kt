plugins {
    application
    kotlin("jvm") version "1.2.10"
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

dependencies {
    compile("com.timgroup:Tucker:1.0.1493") // autobump
    compile("com.timgroup:tim-logger:1.5.1083") // autobump
    compile("com.timgroup:tim-structured-events:0.4.1235") // autobump
    compile("com.timgroup:eventstore-api:0.0.1539") // autobump
    compile("com.timgroup:eventstore-filesystem:0.0.1539") // autobump
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    compile("io.dropwizard.metrics:metrics-core:3.2.3")
    compile("io.dropwizard.metrics:metrics-jvm:3.2.3")
    compile("io.dropwizard.metrics:metrics-graphite:3.2.3")
    compile("org.eclipse.jetty:jetty-server:9.4.8.v20171121")
    compile("org.eclipse.jetty:jetty-servlet:9.4.8.v20171121")
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.2")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.2")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.2")

    testCompile(kotlin("test-junit"))
    testCompile("com.natpryce:hamkrest:1.4.2.2")
    testCompile("org.araqnid:hamkrest-json:1.0.3")
    testCompile("com.timgroup:tim-structured-events-testing:0.4.1235") // autobump
    testCompile("com.timgroup:eventstore-memory:0.0.1539") // autobump
    testCompile("org.apache.httpcomponents:httpclient:4.4.1")
    testCompile("com.timgroup:clocks-testing:1.0.1080") // autobump

    runtime("ch.qos.logback:logback-classic:1.2.3")
}
