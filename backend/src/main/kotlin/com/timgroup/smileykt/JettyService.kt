package com.timgroup.smileykt

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.jetty9.InstrumentedConnectionFactory
import com.codahale.metrics.jetty9.InstrumentedHandler
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.util.concurrent.AbstractIdleService
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.NetworkConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.Slf4jRequestLog
import org.eclipse.jetty.server.handler.HandlerWrapper
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.FilterHolder
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.jboss.resteasy.plugins.server.servlet.Filter30Dispatcher
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap
import org.jboss.resteasy.spi.ResteasyDeployment
import java.util.EnumSet
import javax.servlet.DispatcherType
import javax.servlet.ServletContextEvent

class JettyService(port: Int,
                   appStatus: AppStatus,
                   metrics: MetricRegistry,
                   jaxrsResources: Collection<Any>) : AbstractIdleService() {
    private val server = run {
        val jacksonObjectMapper = jacksonObjectMapper()
                .registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        val threadPool = InstrumentedQueuedThreadPool(metrics).apply {
            name = "Jetty"
        }

        val httpConfiguration = HttpConfiguration().apply {
            requestHeaderSize = 16384
        }

        Server(threadPool).apply {
            addConnector(ServerConnector(this, InstrumentedConnectionFactory(HttpConnectionFactory(httpConfiguration), metrics.timer("jetty-http.connections"))).apply {
                this.port = port
            })
            requestLog = Slf4jRequestLog().apply {
                ignorePaths = arrayOf("/info/*", "/favicon.ico")
            }
            handler = ServletContextHandler().apply {
                gzipHandler = GzipHandler()
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
                        jaxrsResources.forEach { deployment.registry.addSingletonResource(it) }
                    }
                })
                if (javaClass.getResource("/www/.MANIFEST") != null)
                    baseResource = embeddedResourcesFromManifest("www/", javaClass.classLoader).asDocumentRoot()
                else
                    resourceBase = "../webui/build/web"
            }.wrapWith(InstrumentedHandler(metrics, "jetty"))
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

    private fun Handler.wrapWith(wrapper: HandlerWrapper): Handler {
        wrapper.handler = this
        return wrapper
    }
}
