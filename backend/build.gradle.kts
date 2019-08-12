import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.timgroup.gradle.productstore.ProductStorePublication
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.LinkMapping
import java.net.URI
import java.net.URL

plugins {
    application
    kotlin("jvm")
    id("com.timgroup.jarmangit")
    id("com.github.johnrengelman.shadow")
    id("com.timgroup.productstore")
    id("org.jetbrains.dokka")
}

val githubUrl: URI by rootProject.extra

base {
    archivesBaseName = "smiley-kt"
}

application {
    mainClassName = "com.timgroup.smileykt.Launcher"
}

val web: Configuration by configurations.creating

tasks {
    "run"(JavaExec::class) {
        args("config.properties")
        dependsOn(":webui:assembleWeb")
    }

    "runShadow"(JavaExec::class) {
        args("config.properties")
    }

    "test" {
        dependsOn(":webui:assembleWeb")
    }

    "distZip" {
        enabled = false
    }

    "distTar" {
        enabled = false
    }

    val sourcesJar by registering(Jar::class) {
        classifier = "sources"
        from(sourceSets["main"].allSource)
    }

    val shadowJar by existing(ShadowJar::class) {
        classifier = "all"
        manifest {
            attributes(mapOf(
                    "X-Java-Version" to "11"
            ))
        }
        from(web) {
            into("www")
        }
    }

    "dokka"(DokkaTask::class) {
        moduleName = "smiley-kt"
        jdkVersion = 8
        linkMapping(delegateClosureOf<LinkMapping> {
            dir = file("src/main/kotlin").toString()
            url = "$githubUrl/blob/master/backend/src/main/kotlin"
            suffix = "#L"
        })
        externalDocumentationLink(delegateClosureOf<DokkaConfiguration.ExternalDocumentationLink.Builder> {
            url = URL("https://google.github.io/guava/releases/${Versions.guava}/api/docs/")
        })
    }

    "assemble" {
        dependsOn(sourcesJar)
        dependsOn(shadowJar)
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
    options.encoding = "UTF-8"
    options.isIncremental = true
    options.isDeprecation = true
    options.compilerArgs.add("-parameters")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xjsr305=strict"
    }
}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "TIM Group"
        ))
    }
}


repositories {
    jcenter()
    val repoUrl: String by project
    maven(url = "$repoUrl/groups/public")
}

dependencies {
    compile("com.timgroup:Tucker:1.0.1530") // autobump
    compile("com.timgroup:tim-logger:1.5.1096") // autobump
    compile("com.timgroup:tim-structured-events:0.4.1265") // autobump
    compile("com.timgroup:eventstore-api:0.0.1815") // autobump
    compile("com.timgroup:eventstore-filesystem:0.0.1815") // autobump
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    compile("io.dropwizard.metrics:metrics-core:${Versions.metrics}")
    compile("io.dropwizard.metrics:metrics-jvm:${Versions.metrics}")
    compile("io.dropwizard.metrics:metrics-graphite:${Versions.metrics}")
    compile("io.dropwizard.metrics:metrics-jetty9:${Versions.metrics}")
    compile("org.eclipse.jetty:jetty-server:${Versions.jetty}")
    compile("org.eclipse.jetty:jetty-servlet:${Versions.jetty}")
    compile("com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jackson}")
    compile("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:${Versions.jackson}")
    compile("com.google.guava:guava:${Versions.guava}")
    compile("org.jboss.resteasy:resteasy-jaxrs:3.1.2.Final")
    compile("javax.mail:mail:1.4.6")
    compile("org.apache.commons:commons-compress:1.16.1")
    compile("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.10")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinCoroutines}")

    testCompile(kotlin("test-junit"))
    testCompile("com.natpryce:hamkrest:1.4.2.2")
    testCompile("org.araqnid:hamkrest-json:1.0.3")
    testCompile("com.timgroup:tim-structured-events-testing:0.4.1265") // autobump
    testCompile("com.timgroup:eventstore-memory:0.0.1815") // autobump
    testCompile("org.apache.httpcomponents:httpclient:4.5.5")
    testCompile("com.timgroup:clocks-testing:1.0.1095") // autobump

    runtime("ch.qos.logback:logback-classic:1.2.3")

    web(project(path = ":webui", configuration = "web"))
}

publishing {
    publications {
        register<ProductStorePublication>("productStore") {
            application = "smiley-kt"
            artifact(tasks["shadowJar"])
        }
    }
}
