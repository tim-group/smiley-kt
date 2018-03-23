package com.timgroup.smileykt

import com.google.common.util.concurrent.ServiceManager
import com.timgroup.eventstore.api.EventSource
import com.timgroup.tucker.info.Component
import com.timgroup.tucker.info.component.JvmVersionComponent
import java.net.URI
import java.time.Clock

class App(port: Int, clock: Clock, eventSource: EventSource, users: Set<UserDefinition>) {
    private val statusPage = AppStatus("smiley-kt", clock, basicComponents = listOf(
            JvmVersionComponent(),
            Component.supplyInfo("kotlinVersion", "Kotlin Version") { KotlinVersion.CURRENT.toString() }
    ))
    private val jettyService = JettyService(port, statusPage, eventSource)
    private val invitationService = UserInvitationService(
            UserInvitationsRepository(eventSource),
            clock,
            users,
            HtmlEmailGenerator(URI("http://smiley.timgroup.com/"))
    )
    private val serviceManager = ServiceManager(listOf(jettyService, invitationService))

    fun start() {
        serviceManager.startAsync().awaitHealthy()
    }

    fun stop() {
        serviceManager.stopAsync().awaitStopped()
    }

    val port
        get() = jettyService.port
}

data class UserDefinition(val emailAddress: String)
