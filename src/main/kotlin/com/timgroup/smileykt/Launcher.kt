package com.timgroup.smileykt

import com.timgroup.eventstore.filesystem.FlatFilesystemEventSource
import com.timgroup.logger.FilebeatAppender
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

        setUpTimezone()
        setUpLogging(properties)

        val metrics = createMetrics(properties)

        val port = properties.getStringValue("port").toInt()

        val eventsDirectory = Paths.get(properties.getStringValue("events.directory"))
        if (!Files.isDirectory(eventsDirectory)) {
            Files.createDirectories(eventsDirectory)
        }

        val users = parseUserDefinitions(properties.getStringValue("users"))

        val frontEndUri = URI(properties.getStringValue("frontEndUri"))

        val clock = Clock.systemDefaultZone()

        val emailer = if (properties.contains("mail.smtp.from")) DummyEmailer
        else JavaMailEmailer(javax.mail.Session.getInstance(properties))

        App(
                port,
                clock,
                FlatFilesystemEventSource(eventsDirectory, clock, ".txt"),
                users,
                emailer,
                frontEndUri,
                metrics
        ).run {
            start()
            Runtime.getRuntime().addShutdownHook(Thread({ stop() }, "shutdown"))
        }
    }

    private fun setUpTimezone() {
        System.setProperty("user.timezone", "GMT")
        TimeZone.setDefault(null)
    }

    private fun setUpLogging(config: Properties) {
        FilebeatAppender.configureLoggingProperties(config, Launcher::class.java)
    }
}

internal fun Properties.getStringValue(name: String): String = getProperty(name) ?: throw RuntimeException("Property '$name' not specified")
