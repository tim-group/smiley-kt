package com.timgroup.smileykt

import com.timgroup.clocks.testing.ManualClock
import com.timgroup.eventstore.memory.InMemoryEventSource
import com.timgroup.eventstore.memory.JavaInMemoryEventStore
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.conn.routing.HttpRoute
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHttpResponse
import org.apache.http.util.EntityUtils
import org.junit.rules.ExternalResource
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.CopyOnWriteArrayList

class ServerRule : ExternalResource() {
    lateinit var app: App

    val clock = ManualClock(Instant.parse("2017-12-08T12:13:05Z"), ZoneOffset.UTC)
    val eventSource = InMemoryEventSource(JavaInMemoryEventStore(clock))
    val emailSink = EmailSink(clock)
    val users = setOf(UserDefinition("abc@example.com", timeZone = ZoneOffset.UTC))
    val frontEndUri = URI("https://smiley.example.com/")

    override fun before() {
        app = App(0, clock, eventSource, users, emailSink, frontEndUri)
        app.start()
    }

    override fun after() {
        app.stop()
        httpClient.close()
    }

    val port
        get() = app.port

    val httpClient = HttpClients.custom()
            .setRoutePlanner { target, _, _ ->
                val serverTarget = HttpHost("localhost", port)
                check(target == null || target == serverTarget) { "Off-server access to $target not permitted" }
                HttpRoute(serverTarget)
            }
            .build()

    val httpContext = HttpClientContext()

    fun execute(request: HttpUriRequest): HttpResponse {
        return httpClient.execute(request, { rawResponse ->
            BasicHttpResponse(rawResponse.statusLine).apply {
                rawResponse.allHeaders.forEach {
                    addHeader(it)
                }
                if (rawResponse.entity != null) {
                    entity = ByteArrayEntity(EntityUtils.toByteArray(rawResponse.entity), ContentType.get(rawResponse.entity))
                }
            }
        }, httpContext)
    }

    class EmailSink(private val clock: Clock) : Emailer {
        private val emailsSent = CopyOnWriteArrayList<Email>()

        val emails: List<EmailSink.Email>
            get() = emailsSent

        override fun sendHtmlEmail(subject: String, htmlBody: String, toAddress: String) {
            emailsSent += Email(Instant.now(clock), subject, htmlBody, toAddress)
        }

        data class Email(val date: Instant, val subject: String, val htmlBody: String, val toAddress: String)
    }
}
