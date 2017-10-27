package com.timgroup.smileykt

import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.message.BasicNameValuePair
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class RecordHappinessServletIntegrationTest {
    @get:Rule
    val server = ServerRule()

    @Test
    fun `records happiness`() {
        server.execute(HttpPost("/record-happiness").apply {
            entity = mapOf(
                    "email" to "test@example.com",
                    "happiness" to "very happy").toFormEntity()
        })

        assertEquals(200, server.response.statusLine.statusCode)
    }

    private fun Map<String, String>.toFormEntity() =
            UrlEncodedFormEntity(entries.map { (key, value) -> BasicNameValuePair(key, value) }, Charsets.UTF_8)
}
