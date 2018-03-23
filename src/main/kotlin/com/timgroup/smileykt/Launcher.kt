package com.timgroup.smileykt

import com.timgroup.eventstore.filesystem.FlatFilesystemEventSource
import java.net.URI
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

        val port = properties.getStringValue("port").toInt()

        val eventsDirectory = Paths.get(properties.getStringValue("events.directory"))
        if (!Files.isDirectory(eventsDirectory)) {
            Files.createDirectories(eventsDirectory)
        }

        val users = properties.getStringValue("users").run {
            split(Regex("(,|\\s)\\s*")).map {
                UserDefinition(emailAddress = it)
            }
        }.toSet()

        val frontEndUri = properties.getStringValue("frontEndUri").let { URI(it) }

        val clock = Clock.systemDefaultZone()
        App(
                port,
                clock,
                FlatFilesystemEventSource(eventsDirectory, clock, ".txt"),
                users,
                DummyEmailer,
                frontEndUri
        ).run {
            start()
            Runtime.getRuntime().addShutdownHook(Thread({ stop() }, "shutdown"))
        }
    }

    private fun Properties.getStringValue(name: String): String = getProperty(name) ?: throw RuntimeException("Property '$name' not specified")
}
