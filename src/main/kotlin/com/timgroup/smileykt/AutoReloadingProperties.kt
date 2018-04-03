package com.timgroup.smileykt

import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.Properties
import kotlin.reflect.KProperty

class AutoReloadingProperties<out T>(private val propertiesFile: Path, private val parser: (Properties) -> T) {
    private lateinit var currentData: Loaded<T>

    operator fun getValue(owner: Any?, property: KProperty<*>): T {
        sync()
        return currentData.parsed
    }

    private data class Loaded<out T>(val parsed: T, val modificationTime: Instant)

    private fun load(): T {
        return Properties().run {
            Files.newInputStream(propertiesFile).use { load(it) }
            parser(this)
        }
    }

    private fun sync() {
        val currentModificationTime = Files.getLastModifiedTime(propertiesFile).toInstant()
        if (this::currentData.isInitialized && currentData.modificationTime >= currentModificationTime) {
            return
        }
        currentData = Loaded(load(), currentModificationTime)
    }
}
