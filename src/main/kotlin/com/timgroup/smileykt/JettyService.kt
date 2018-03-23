package com.timgroup.smileykt

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.util.concurrent.AbstractIdleService
import com.timgroup.eventstore.api.EventSource
import org.eclipse.jetty.server.NetworkConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.Slf4jRequestLog
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.FilterHolder
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.jboss.resteasy.plugins.server.servlet.Filter30Dispatcher
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap
import org.jboss.resteasy.spi.ResteasyDeployment
import java.util.*
import javax.servlet.DispatcherType
import javax.servlet.ServletContextEvent

class JettyService(port: Int,
                   appStatus: AppStatus,
                   eventSource: EventSource) : AbstractIdleService() {
    private val jacksonObjectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())

    private val server = Server(port).apply {
        requestLog = Slf4jRequestLog()
        handler = ServletContextHandler().apply {
            addServlet(ServletHolder(appStatus.createServlet()), "/info/*")
            addServlet(ServletHolder(DefaultServlet::class.java).apply {
                setInitParameter("precompressed", "true")
                setInitParameter("etags", "true")
            }, "/")
            addFilter(FilterHolder(Filter30Dispatcher()), "/*", EnumSet.of(DispatcherType.REQUEST))
            addEventListener(object : ResteasyBootstrap() {
                override fun contextInitialized(event: ServletContextEvent) {
                    super.contextInitialized(event)
                    val deployment = event.servletContext.getAttribute(ResteasyDeployment::class.java.name) as ResteasyDeployment
                    deployment.providerFactory.register(JacksonJsonProvider(jacksonObjectMapper))
                    deployment.registry.addSingletonResource(HappinessResources(eventSource))
                }
            })
            if (javaClass.getResource("/www/.MANIFEST") != null)
                baseResource = embeddedResourcesFromManifest("www/", javaClass.classLoader).asDocumentRoot()
            else
                resourceBase = "webui/build/web"
        }
    }

    override fun startUp() {
        server.start()
    }

    override fun shutDown() {
        server.stop()
    }

    val port: Int
        get() {
            check(server.isRunning)
            return (server.connectors[0] as NetworkConnector).localPort
        }
}
