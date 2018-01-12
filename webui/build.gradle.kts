import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

// see https://github.com/gradle/kotlin-dsl/blob/master/samples/hello-js/build.gradle.kts for inspiration
// see also https://github.com/Kotlin/kotlinx.coroutines/tree/master/js/example-frontend-js
// and https://kotlinlang.org/docs/reference/javascript-dce.html

plugins {
    id("kotlin-dce-js")
}

repositories {
    maven(url = "http://repo.net.local/nexus/content/groups/public")
}

dependencies {
    "compile"("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:0.21")
    "compile"("org.jetbrains.kotlinx:kotlinx-html-js:0.6.8")
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

val mainSourceSet = the<JavaPluginConvention>().sourceSets["main"]!!

tasks {
    "compileKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions {
            main = "call"
        }
    }


    val assembleWeb by creating(Sync::class) {
        group = "build"
        description = "Assemble the web application"
        includeEmptyDirs = false
        from("$buildDir/classes/kotlin/main/min")
        into("$buildDir/web")
        dependsOn("runDceKotlinJs")
    }

    "assemble" {
        dependsOn(assembleWeb)
    }
}
