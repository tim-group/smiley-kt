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
import org.junit.After
import org.junit.Rule

abstract class IntegrationTest {
    @get:Rule
    val server = ServerRule()

    val httpClient = HttpClients.custom()
            .setRoutePlanner { target, _, _ ->
                val serverTarget = HttpHost("localhost", server.port)
                check(target == null || target == serverTarget) { "Off-server access to $target not permitted" }
                HttpRoute(serverTarget)
            }
            .build()

    val httpContext = HttpClientContext()

    lateinit var response: HttpResponse

    @After
    fun closeHttpClient() {
        httpClient.close()
    }

    fun execute(request: HttpUriRequest) {
        httpClient.execute(request, { rawResponse ->
            response = BasicHttpResponse(rawResponse.statusLine).apply {
                rawResponse.allHeaders.forEach {
                    addHeader(it)
                }
                entity = ByteArrayEntity(EntityUtils.toByteArray(rawResponse.entity), ContentType.get(rawResponse.entity))
            }
        }, httpContext)
    }
}
