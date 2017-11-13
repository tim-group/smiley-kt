package com.timgroup.smileykt

import com.timgroup.eventstore.api.EventSource
import org.eclipse.jetty.server.NetworkConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.Slf4jRequestLog
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.resource.PathResource
import org.eclipse.jetty.util.resource.ResourceCollection
import java.nio.file.Paths

class JettyService(port: Int, appStatus: AppStatus, eventSource: EventSource) {
    private val server = Server(port).apply {
        requestLog = Slf4jRequestLog()
        handler = ServletContextHandler().apply {
            val recordHappinessServlet = RecordHappinessServlet(eventSource)
            addServlet(ServletHolder(appStatus.createServlet()), "/info/*")
            addServlet(DefaultServlet::class.java, "/*")
            addServlet(ServletHolder(recordHappinessServlet), "/happiness")
            baseResource = ResourceCollection(
                    PathResource(Paths.get("src/main/web")),
                    PathResource(Paths.get("webui/build/classes/kotlin/main"))
            )
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
