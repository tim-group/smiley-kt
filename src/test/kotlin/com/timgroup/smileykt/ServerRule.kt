package com.timgroup.smileykt

import org.junit.rules.ExternalResource

class ServerRule : ExternalResource() {
    lateinit var app: App

    override fun before() {
        app = App(0)
        app.start()
    }

    override fun after() {
        app.stop()
    }

    val port
        get() = app.port
}