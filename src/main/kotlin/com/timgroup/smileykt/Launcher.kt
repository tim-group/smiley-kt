package com.timgroup.smileykt

import com.timgroup.tucker.info.component.JarVersionComponent
import com.timgroup.tucker.info.status.StatusPageGenerator
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Properties

object Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 1) {
            System.err.println("Syntax: java Launcher config.properties")
            System.exit(1)
        }

        val properties = Properties()
        Files.newInputStream(Paths.get(args[0])).use { stream ->
            properties.load(stream)
        }

        val statusPage = StatusPageGenerator("smiley-kt", JarVersionComponent(Launcher::class.java))
        val port = properties.getProperty("port").toInt()
        val jettyService = JettyService(port, statusPage)
        jettyService.start()

        Runtime.getRuntime().addShutdownHook(Thread(Runnable {
            jettyService.stop()
        }, "shutdown"))
    }
}