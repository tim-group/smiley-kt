package com.timgroup.smileykt

import com.timgroup.eventstore.filesystem.FlatFilesystemEventSource
import com.timgroup.logger.FilebeatAppender
import org.slf4j.LoggerFactory
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Clock
import java.time.Instant
import java.util.Properties
import java.util.TimeZone
import javax.mail.internet.InternetAddress

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

        val users = AutoReloadingUserDefinitions(Paths.get(args[0]))

        val frontEndUri = URI(properties.getStringValue("frontEndUri"))

        val clock = Clock.systemDefaultZone()

        val emailer = if (properties.contains("mail.smtp.host")) DummyEmailer
        else JavaMailEmailer(javax.mail.Session.getInstance(properties), InternetAddress(properties.getStringValue("email.from")))

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

class AutoReloadingUserDefinitions(private val propertiesFile: Path) : AbstractSet<UserDefinition>() {
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var currentData: Loaded

    override val size: Int
        get() {
            sync()
            return currentData.users.size
        }

    override fun iterator(): Iterator<UserDefinition> {
        sync()
        return currentData.users.iterator()
    }

    private data class Loaded(val users: Set<UserDefinition>, val modificationTime: Instant)

    private fun load(): Set<UserDefinition> {
        val properties = Properties().apply {
            Files.newInputStream(propertiesFile).use { load(it) }
        }
        return parseUserDefinitions(properties.getStringValue("users"))
    }

    private fun sync() {
        val currentModificationTime = Files.getLastModifiedTime(propertiesFile).toInstant()
        if (this::currentData.isInitialized && currentData.modificationTime >= currentModificationTime) {
            return
        }
        currentData = Loaded(load(), currentModificationTime)
        logger.info("Loaded users from $propertiesFile, last modified at $currentModificationTime")
    }
}

internal fun Properties.getStringValue(name: String): String = getProperty(name) ?: throw RuntimeException("Property '$name' not specified")
