package com.timgroup.smileykt

import com.timgroup.eventstore.api.EventSource
import com.timgroup.tucker.info.component.JarVersionComponent
import com.timgroup.tucker.info.status.StatusPageGenerator

class App(port: Int, eventSource: EventSource) {
    private val statusPage = StatusPageGenerator("smiley-kt", JarVersionComponent(App::class.java))
    private val jettyService = JettyService(port, statusPage, eventSource)

    fun start() {
        jettyService.start()
    }

    fun stop() {
        jettyService.stop()
    }

    val port
        get() = jettyService.port
}