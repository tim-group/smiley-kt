import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

// see https://github.com/gradle/kotlin-dsl/blob/master/samples/hello-js/build.gradle.kts for inspiration

apply {
    plugin("org.jetbrains.kotlin.platform.js")
}

repositories {
    maven(url = "http://repo.net.local/nexus/content/groups/public")
}

dependencies {
    "compile"(kotlin("stdlib-js"))
}

val mainSourceSet = the<JavaPluginConvention>().sourceSets["main"]!!

tasks {
    "compileKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions {
            outputFile = "${mainSourceSet.output.resourcesDir}/output.js"
            sourceMap = true
            sourceMapEmbedSources = "always"
        }
    }

    val unpackKotlinJsStdlib by creating {
        group = "build"
        description = "Unpack the Kotlin JavaScript standard library"
        val outputDir = file("$buildDir/$name")
        val compileClasspath = configurations["compileClasspath"]
        inputs.property("compileClasspath", compileClasspath)
        outputs.dir(outputDir)
        doLast {
            val kotlinStdLibJar = compileClasspath.single {
                it.name.matches(Regex("kotlin-stdlib-js.+\\.jar"))
            }
            copy {
                includeEmptyDirs = false
                from(zipTree(kotlinStdLibJar))
                into(outputDir)
                include("**/*.js")
                exclude("META-INF/**")
            }
        }
    }

    val assembleWeb by creating(Sync::class) {
        group = "build"
        description = "Assemble the web application"
        includeEmptyDirs = false
        from(unpackKotlinJsStdlib)
        from(mainSourceSet.output) {
            exclude("**/*.kjsm")
        }
        into("$buildDir/web")
    }

    "assemble" {
        dependsOn(assembleWeb)
    }
}
