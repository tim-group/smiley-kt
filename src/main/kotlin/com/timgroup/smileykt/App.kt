package com.timgroup.smileykt

import com.google.common.util.concurrent.ServiceManager
import com.timgroup.eventstore.api.EventSource
import java.time.Clock

class App(port: Int, clock: Clock, eventSource: EventSource) {
    private val statusPage = AppStatus("smiley-kt", clock)
    private val jettyService = JettyService(port, statusPage, eventSource)
    private val userInvitationsRepository = UserInvitationsRepository(eventSource)
    private val invitationService = UserInvitationService(userInvitationsRepository, clock, emptySet())
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
