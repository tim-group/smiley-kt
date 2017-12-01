plugins {
    application
    kotlin("jvm") version "1.1.61"
}

apply {
    plugin("com.timgroup.autobumper")
    plugin("com.timgroup.jarmangit")
}

application {
    mainClassName = "com.timgroup.smileykt.Launcher"
}

(tasks["run"] as JavaExec).args("config.properties")

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
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

repositories {
    maven(url = "http://repo.net.local/nexus/content/groups/public")
}

dependencies {
    compile("com.timgroup:Tucker:autobump")
    compile("com.timgroup:tim-logger:autobump")
    compile("com.timgroup:tim-structured-events:autobump")
    compile("com.timgroup:eventstore-api_2.12:autobump")
    compile("com.timgroup:eventstore-filesystem_2.12:autobump")
    compile(kotlin("stdlib"))
    compile(kotlin("stdlib-jre8"))
    compile("io.dropwizard.metrics:metrics-core:3.2.3")
    compile("io.dropwizard.metrics:metrics-jvm:3.2.3")
    compile("io.dropwizard.metrics:metrics-graphite:3.2.3")
    compile("org.eclipse.jetty:jetty-server:9.4.7.v20170914")
    compile("org.eclipse.jetty:jetty-servlet:9.4.7.v20170914")
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.2")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.2")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.2")

    testCompile("junit:junit:4.12")
    testCompile("org.hamcrest:hamcrest-core:1.3")
    testCompile("org.hamcrest:hamcrest-library:1.3")
    testCompile("com.timgroup:tim-structured-events-testing:autobump")
    testCompile("com.timgroup:eventstore-memory_2.12:autobump")
    testCompile("org.apache.httpcomponents:httpclient:4.4.1")
    testCompile(kotlin("test-junit"))

    runtime("ch.qos.logback:logback-classic:1.2.2")
}

buildscript {
    repositories {
        maven(url = "http://repo.net.local/nexus/content/groups/public")
    }

    dependencies {
        classpath("com.timgroup:gradle-autobumper:1.1.+")
        classpath("com.timgroup:gradle-jarmangit:1.1.+")
    }
}
