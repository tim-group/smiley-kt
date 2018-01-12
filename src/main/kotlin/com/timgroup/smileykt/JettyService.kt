package com.timgroup.smileykt

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.timgroup.eventstore.api.EventSource
import org.eclipse.jetty.server.NetworkConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.Slf4jRequestLog
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.FilterHolder
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.resource.PathResource
import org.jboss.resteasy.plugins.server.servlet.Filter30Dispatcher
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap
import org.jboss.resteasy.spi.ResteasyDeployment
import java.nio.file.Paths
import java.time.Clock
import java.util.*
import javax.servlet.DispatcherType
import javax.servlet.ServletContextEvent

class JettyService(port: Int, appStatus: AppStatus, eventSource: EventSource, clock: Clock) {
    private val server = Server(port).apply {
        requestLog = Slf4jRequestLog()
        handler = ServletContextHandler().apply {
            addServlet(ServletHolder(appStatus.createServlet()), "/info/*")
            addServlet(DefaultServlet::class.java, "/*")
            addFilter(FilterHolder(Filter30Dispatcher()), "/*", EnumSet.of(DispatcherType.REQUEST))
            addEventListener(object : ResteasyBootstrap() {
                override fun contextInitialized(event: ServletContextEvent) {
                    super.contextInitialized(event)
                    val deployment = event.servletContext.getAttribute(ResteasyDeployment::class.java.name) as ResteasyDeployment
                    deployment.providerFactory.register(JacksonJsonProvider(jacksonObjectMapper()))
                    deployment.registry.addSingletonResource(RecordHappinessResources(eventSource, clock))
                }
            })
            baseResource = PathResource(Paths.get("webui/build/web"))
        }
    }

    fun start() {
        server.start()
    }

    fun stop() {
        server.stop()
    }

    val port: Int
        get() {
            check(server.isRunning)
            return (server.connectors[0] as NetworkConnector).localPort
        }
}
