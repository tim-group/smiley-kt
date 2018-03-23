import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.security.MessageDigest
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.nio.file.Files.getFileAttributeView
import java.nio.file.Files.newInputStream
import java.nio.file.Files.newOutputStream
import java.nio.file.attribute.BasicFileAttributeView

// see https://github.com/gradle/kotlin-dsl/blob/master/samples/hello-js/build.gradle.kts for inspiration
// see also https://github.com/Kotlin/kotlinx.coroutines/tree/master/js/example-frontend-js
// and https://kotlinlang.org/docs/reference/javascript-dce.html

val kotlinCoroutinesVersion: String by rootProject.extra

plugins {
    id("kotlin-dce-js")
}

repositories {
    maven(url = "http://repo.net.local/nexus/content/groups/public")
}

dependencies {
    "compile"("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$kotlinCoroutinesVersion")
    "compile"("org.jetbrains.kotlinx:kotlinx-html-js:0.6.8")
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

tasks {
    "compileKotlin2Js"(Kotlin2JsCompile::class) {
        kotlinOptions {
            main = "call"
            sourceMap = true
            sourceMapEmbedSources = "always"
        }
    }


    val assembleWeb by creating(Sync::class) {
        group = "build"
        description = "Assemble the web application"
        includeEmptyDirs = false
        from("$buildDir/kotlin-js-min/main")
        from("src/main/web")
        into("$buildDir/web")
        dependsOn("runDceKotlinJs")
    }

    val resourceManifest by creating {
        val outputDir = file("$buildDir/resource_manifest")
        outputs.dir(outputDir)
        inputs.files(assembleWeb)

        data class StaticResource(val file: File, val name: String)

        fun ByteArray.toHexString(): String {
            val hexdigits = "0123456789abcdef"
            val output = CharArray(this.size * 2)
            this.forEachIndexed { index: Int, byte: Byte ->
                output[2 * index] = hexdigits[(byte.toInt() and 0xf0) shr 4]
                output[2 * index + 1] = hexdigits[byte.toInt() and 0x0f]
            }
            return String(output)
        }

        doLast {
            outputDir.mkdirs()
            val staticResources = assembleWeb.outputs.files.flatMap { topDir ->
                fileTree(topDir)
                        .map { resourceFile -> StaticResource(file = resourceFile, name = resourceFile.toString().substring(topDir.toString().length + 1)) }
                        .sortedBy { it.name }
            }
            val lines = staticResources.map { (file, name) ->
                val digester = MessageDigest.getInstance("SHA-256")
                newInputStream(file.toPath()).use { input ->
                    val buf = ByteArray(8192)
                    while (true) {
                        val got = input.read(buf)
                        if (got > 0) {
                            digester.update(buf, 0, got)
                        }
                        else {
                            break
                        }
                    }
                }
                val fileAttributes = getFileAttributeView(file.toPath(), BasicFileAttributeView::class.java).readAttributes()
                "${digester.digest().toHexString()} $name ${fileAttributes.size()} ${fileAttributes.lastModifiedTime().toMillis()}"
            }
            val manifestPath = outputDir.toPath().resolve(".MANIFEST")
            PrintWriter(OutputStreamWriter(newOutputStream(manifestPath))).use { output ->
                lines.forEach(output::println)
            }
            project.logger.info("Wrote $manifestPath")
        }
    }

    "assemble" {
        dependsOn(assembleWeb)
        dependsOn(resourceManifest)
    }
}
