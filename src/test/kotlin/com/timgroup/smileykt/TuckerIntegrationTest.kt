package com.timgroup.smileykt

import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.ContentType
import org.apache.http.util.EntityUtils
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class TuckerIntegrationTest {
    @get:Rule
    val server = ServerRule()

    @Test
    fun `shows application health`() {
        val response = server.execute(HttpGet("/info/health"))
        assertEquals(200, response.statusLine.statusCode)
        assertEquals("text/plain", ContentType.get(response.entity).mimeType)
        assertEquals("healthy", EntityUtils.toString(response.entity))
    }

    @Test
    fun `shows application stoppable`() {
        val response = server.execute(HttpGet("/info/stoppable"))
        assertEquals(200, response.statusLine.statusCode)
        assertEquals("text/plain", ContentType.get(response.entity).mimeType)
        assertEquals("safe", EntityUtils.toString(response.entity))
    }

    @Test
    fun `shows application version`() {
        val response = server.execute(HttpGet("/info/version"))
        assertEquals(200, response.statusLine.statusCode)
        assertEquals("text/plain", ContentType.get(response.entity).mimeType)
    }

    @Test
    fun `shows application status`() {
        val response = server.execute(HttpGet("/info/status.json"))
        assertEquals(200, response.statusLine.statusCode)
        assertEquals("application/json", ContentType.get(response.entity).mimeType)
    }
}

