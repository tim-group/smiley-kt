package com.timgroup.smileykt

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

class ServerRule : ExternalResource() {
    lateinit var app: App

    override fun before() {
        app = App(0)
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

    lateinit var response: HttpResponse

    fun execute(request: HttpUriRequest): HttpResponse {
        httpClient.execute(request, { rawResponse ->
            response = BasicHttpResponse(rawResponse.statusLine).apply {
                rawResponse.allHeaders.forEach {
                    addHeader(it)
                }
                if (rawResponse.entity != null) {
                    entity = ByteArrayEntity(EntityUtils.toByteArray(rawResponse.entity), ContentType.get(rawResponse.entity))
                }
            }
        }, httpContext)
        return response
    }
}