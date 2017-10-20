package com.timgroup.smileykt

import com.timgroup.tucker.info.component.JarVersionComponent
import com.timgroup.tucker.info.status.StatusPageGenerator

class App(port: Int) {
    val statusPage = StatusPageGenerator("smiley-kt", JarVersionComponent(App::class.java))
    val jettyService = JettyService(port, statusPage)

    fun start() {
        jettyService.start()
    }

    fun stop() {
        jettyService.stop()
    }

    val port
        get() = jettyService.port
}