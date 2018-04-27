package com.timgroup.smileykt

import com.google.common.io.Resources
import org.eclipse.jetty.util.resource.Resource
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.channels.ReadableByteChannel
import java.time.Instant
import kotlin.text.Charsets.UTF_8

class EmbeddedResources(private val resources: Map<String, StaticResource>) {
    operator fun get(name: String) = resources[name]?.url
    fun asDocumentRoot(): Resource = ResourceImpl("")

    private inner class ResourceImpl(private val path: String) : Resource() {
        private val resource = resources[path]

        override fun isContainedIn(r: Resource?): Boolean = false

        override fun exists(): Boolean {
            return if (isDirectory) {
                resources.keys.any { it.startsWith(path) }
            } else {
                resource != null
            }
        }

        override fun isDirectory(): Boolean = path.isEmpty() || path.endsWith("/")

        override fun lastModified(): Long = resource?.lastModified?.toEpochMilli() ?: -1

        override fun length(): Long = resource?.length ?: -1

        @Deprecated(message = "deprecated in superclass")
        override fun getURL(): URL? = resource?.url

        override fun getFile(): File? = null

        override fun getName(): String = path

        @Throws(IOException::class)
        override fun getInputStream(): InputStream? = resource?.url?.openStream()

        override fun getReadableByteChannel(): ReadableByteChannel? = null

        override fun close() { }

        override fun delete(): Boolean = false

        override fun renameTo(dest: Resource): Boolean = false

        override fun list(): Array<String> {
            if (!isDirectory) return emptyArray()
            val matches = mutableSetOf<String>()
            resources.keys.filter { it.startsWith(path) }.mapTo(matches) { name ->
                val suffix = name.substring(path.length)
                val pos = suffix.indexOf('/')
                if (pos > 0) suffix.substring(0, pos) else suffix
            }
            matches.remove(".MANIFEST")
            return matches.toTypedArray()
        }

        override fun addPath(anotherPath: String): Resource {
            val combinedPath =
                    if (anotherPath.startsWith("/")) path + anotherPath.substring(1)
                    else path + anotherPath
            return ResourceImpl(combinedPath)
        }

        override fun getWeakETag(suffix: String): String {
            if (resource == null) {
                return super.getWeakETag(suffix)
            }

            return "W/\"${resource.digest}$suffix\""
        }

        override fun toString(): String = "EmbeddedResources[$path]"
    }
}

fun embeddedResourcesFromManifest(prefix: String,
                                  classLoader: ClassLoader = Thread.currentThread().contextClassLoader): EmbeddedResources {
    require(!prefix.startsWith("/"))
    require(prefix.endsWith("/"))

    val manifestResourceName = "$prefix.MANIFEST"
    val manifestResource = classLoader.getResource(manifestResourceName)
            ?: throw RuntimeException("Static resource manifest not found: $manifestResourceName")

    val lines = try {
        Resources.asByteSource(manifestResource).asCharSource(UTF_8).readLines()
    } catch (e: IOException) {
        throw RuntimeException("Unable to read resources manifest: $manifestResource", e)
    }

    val resources = lines.associate {
        val (digest, name, sizeString, lastModifiedString) = it.split(" ")
        name to StaticResource(digest = digest,
                url = classLoader.getResource(prefix + name),
                length = sizeString.toLong(),
                lastModified = Instant.ofEpochMilli(lastModifiedString.toLong()))
    }

    return EmbeddedResources(resources)
}

data class StaticResource(val digest: String, val url: URL, val length: Long, val lastModified: Instant)
