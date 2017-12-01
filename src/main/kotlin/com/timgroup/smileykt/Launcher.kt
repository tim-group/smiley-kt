package com.timgroup.smileykt

import com.timgroup.eventstore.filesystem.FlatFilesystemEventSource
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Clock
import java.util.*

object Launcher {

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 1) {
            System.err.println("Syntax: java Launcher config.properties")
            System.exit(1)
        }

        val properties = Properties().apply {
            Files.newInputStream(Paths.get(args[0])).use { load(it) }
        }

        System.setProperty("log.directory", "log")

        val port = properties.getProperty("port").toInt()

        val eventsDirectory = Paths.get(properties.getProperty("events.directory"))
        if (!Files.isDirectory(eventsDirectory)) {
            Files.createDirectories(eventsDirectory)
        }

        val clock = Clock.systemDefaultZone()
        App(port, clock, FlatFilesystemEventSource(eventsDirectory, clock, ".txt")).run {
            start()
            Runtime.getRuntime().addShutdownHook(Thread({ stop() }, "shutdown"))
        }
    }
}