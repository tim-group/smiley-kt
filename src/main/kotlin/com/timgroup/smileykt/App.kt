package com.timgroup.smileykt

import com.timgroup.eventstore.api.EventSource
import java.time.Clock

class App(port: Int, clock: Clock, eventSource: EventSource) {
    private val statusPage = AppStatus("smiley-kt", clock)
    private val jettyService = JettyService(port, statusPage, eventSource, clock)

    fun start() {
        jettyService.start()
    }

    fun stop() {
        jettyService.stop()
    }

    val port
        get() = jettyService.port
}