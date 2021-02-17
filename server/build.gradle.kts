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
    }

    "runShadow"(JavaExec::class) {
        args("config.properties")
    }

    "test" {
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
            url = "$githubUrl/blob/master/server/src/main/kotlin"
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
    implementation("com.timgroup:Tucker:1.0.1583") // autobump
    implementation("com.timgroup:tim-jetty:1.0.25") // autobump
    implementation("com.timgroup:tim-logger:1.5.1109") // autobump
    implementation("com.timgroup:tim-metrics:1.0.40") // autobump
    implementation("com.timgroup:tim-structured-events:0.4.1284") // autobump
    implementation("com.timgroup:eventstore-api:0.0.2102") // autobump
    implementation("com.timgroup:eventstore-filesystem:0.0.2102") // autobump
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jackson}")
    implementation("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:${Versions.jackson}")
    implementation("com.google.guava:guava:${Versions.guava}")
    implementation("org.jboss.resteasy:resteasy-jaxrs:3.1.2.Final")
    implementation("javax.mail:mail:1.4.6")
    implementation("org.apache.commons:commons-compress:1.16.1")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinCoroutines}")

    testImplementation(kotlin("test-junit"))
    testImplementation("com.natpryce:hamkrest:1.4.2.2")
    testImplementation("org.araqnid:hamkrest-json:1.0.3")
    testImplementation("com.timgroup:tim-structured-events-testing:0.4.1284") // autobump
    testImplementation("com.timgroup:eventstore-memory:0.0.2102") // autobump
    testImplementation("org.apache.httpcomponents:httpclient:4.5.5")
    testImplementation("com.timgroup:clocks-testing:1.0.1106") // autobump

    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")

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
