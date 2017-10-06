package com.timgroup.smileykt

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

        val port = properties.getProperty("port").toInt()
        println("start on port $port")
    }
}