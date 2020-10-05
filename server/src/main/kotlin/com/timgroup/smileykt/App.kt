package com.timgroup.smileykt

import com.google.common.util.concurrent.MoreExecutors.directExecutor
import com.google.common.util.concurrent.ServiceManager
import com.timgroup.eventstore.api.EventSource
import com.timgroup.metrics.Metrics
import com.timgroup.structuredevents.EventSink
import com.timgroup.structuredevents.standardevents.ApplicationStarted
import com.timgroup.tucker.info.Component
import com.timgroup.tucker.info.component.JvmVersionComponent
import java.net.URI
import java.time.Clock
import java.time.ZoneId
import java.time.ZoneOffset

class App(
        port: Int,
        clock: Clock,
        eventSource: EventSource,
        users: Set<UserDefinition>,
        emailer: Emailer,
        frontEndUri: URI,
        backEndUri: URI,
        metrics: Metrics,
        eventSink: EventSink
) {
    private val statusPage = AppStatus("smiley-kt", clock,
            basicComponents = listOf(
                JvmVersionComponent(),
                Component.supplyInfo("kotlinVersion", "Kotlin Version") { KotlinVersion.CURRENT.toString() })
    )
    private val jettyService = JettyService(port, statusPage, metrics, listOf(
            HappinessResources(eventSource, frontEndUri),
            EventStoreResources(eventSource),
            ProxiedOpenIdAuthResources()
    ))
    private val invitationService = UserInvitationService(
            UserInvitationsRepository(eventSource),
            clock,
            users,
            HtmlEmailGenerator(backEndUri),
            emailer
    )
    private val serviceManager = ServiceManager(listOf(jettyService, invitationService)).apply {
        addListener(object : ServiceManager.Listener() {
            override fun healthy() {
                eventSink.sendEvent(ApplicationStarted.withVersionFromAndParameters(this@App.javaClass, emptyMap<String, Any>()))
            }
        }, directExecutor())
    }

    fun start() {
        serviceManager.startAsync().awaitHealthy()
    }

    fun stop() {
        serviceManager.stopAsync().awaitStopped()
    }

    val port
        get() = jettyService.port
}

data class UserDefinition(val emailAddress: String, val timeZone: ZoneId)

fun parseUserDefinitions(input: String): Set<UserDefinition> {
    val parts = input.trim().split(Regex("(,|\\s)\\s*"))
    var timeZone: ZoneId = ZoneOffset.UTC
    val users = mutableSetOf<UserDefinition>()
    for (part in parts) {
        val timezoneMatch = Regex("\\[(.+)]").matchEntire(part)
        if (timezoneMatch != null) {
            timeZone = ZoneId.of(timezoneMatch.groupValues[1])
        }
        else {
            users.add(UserDefinition(emailAddress = part, timeZone = timeZone))
        }
    }
    return users
}
