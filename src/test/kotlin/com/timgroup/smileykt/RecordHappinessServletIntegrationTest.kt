package com.timgroup.smileykt

import org.apache.http.HttpEntity
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class RecordHappinessServletIntegrationTest {
    @get:Rule
    val server = ServerRule()

    @Test
    fun `gets empty happiness`() {
        server.execute(HttpGet("/happiness")).apply {
            assertEquals(200, statusLine.statusCode)
            assertEquals("", entity.readText())
        }
    }

    @Test
    fun `records happiness`() {
        server.execute(HttpPost("/happiness").apply {
            entity = mapOf(
                    "email" to "test@example.com",
                    "happiness" to "very happy").toFormEntity()
        }).apply {
            assertEquals(200, statusLine.statusCode)
        }

        server.execute(HttpGet("/happiness")).apply {
            assertEquals(200, statusLine.statusCode)
            assertEquals("test@example.com very happy\n", entity.readText())
        }
    }

    private fun Map<String, String>.toFormEntity() =
            UrlEncodedFormEntity(entries.map { (key, value) -> BasicNameValuePair(key, value) }, Charsets.UTF_8)

    private fun HttpEntity.readText() = EntityUtils.toString(this)
}
