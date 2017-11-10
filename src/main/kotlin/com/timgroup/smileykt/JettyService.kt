package com.timgroup.smileykt

import com.timgroup.eventstore.api.EventSource
import com.timgroup.tucker.info.Health
import com.timgroup.tucker.info.Stoppable
import com.timgroup.tucker.info.servlet.ApplicationInformationServlet
import com.timgroup.tucker.info.status.StatusPageGenerator
import org.eclipse.jetty.server.NetworkConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.Slf4jRequestLog
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder

class JettyService(port: Int, statusPage: StatusPageGenerator, eventSource: EventSource) {
    private val server = Server(port).apply {
        requestLog = Slf4jRequestLog()
        handler = ServletContextHandler().apply {
            val tuckerServlet = ApplicationInformationServlet(statusPage, Stoppable.ALWAYS_STOPPABLE, Health.ALWAYS_HEALTHY)
            val recordHappinessServlet = RecordHappinessServlet(eventSource)
            addServlet(ServletHolder(tuckerServlet), "/info/*")
            addServlet(DefaultServlet::class.java, "/*")
            addServlet(ServletHolder(recordHappinessServlet), "/happiness")
            resourceBase = "src/main/web"
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
