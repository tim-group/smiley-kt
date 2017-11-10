package com.timgroup.smileykt

import com.timgroup.eventstore.api.NewEvent.newEvent
import com.timgroup.eventstore.api.StreamId.streamId
import org.apache.http.HttpEntity
import org.apache.http.HttpStatus
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class RecordHappinessServletIntegrationTest {
    @get:Rule
    val server = ServerRule()

    @Test
    fun `gets empty happiness`() {
        server.execute(HttpGet("/happiness")).apply {
            assertEquals(HttpStatus.SC_OK, statusLine.statusCode)
            assertEquals("", entity.readText())
        }
    }

    @Test
    fun `gets happiness`() {
        server.eventSource.writeStream().write(streamId("happiness", "test@example.com"), listOf(
                newEvent("HappinessReceived", "happy".toByteArray())
        ))
        server.execute(HttpGet("/happiness")).apply {
            assertEquals(HttpStatus.SC_OK, statusLine.statusCode)
            assertEquals("test@example.com happy\n", entity.readText())
        }
    }

    @Ignore
    @Test
    fun `aggregates happiness of single user`() {
        server.eventSource.writeStream().write(streamId("happiness", "test@example.com"), listOf(
                newEvent("HappinessReceived", "happy".toByteArray()),
                newEvent("HappinessReceived", "sad".toByteArray())
        ))
        server.eventSource.writeStream().write(streamId("happiness", "zzzz@example.com"), listOf(
                newEvent("HappinessReceived", "happy".toByteArray())
        ))
        server.execute(HttpGet("/happiness")).apply {
            assertEquals(HttpStatus.SC_OK, statusLine.statusCode)
            assertEquals("test@example.com sad\nzzzz@example.com happy\n", entity.readText().sortLines())
        }
    }

    @Test
    fun `records happiness`() {
        server.execute(HttpPost("/happiness").apply {
            entity = mapOf(
                    "email" to "test@example.com",
                    "happiness" to "very happy").toFormEntity()
        }).apply {
            assertEquals(HttpStatus.SC_NO_CONTENT, statusLine.statusCode)
        }

        // FIXME
//        assertEquals(listOf<ResolvedEvent>(),
//                server.eventSource.readStream().readStreamForwards(streamId("happiness", "test@example.com")).collect(toList()))

        server.execute(HttpGet("/happiness")).apply {
            assertEquals(HttpStatus.SC_OK, statusLine.statusCode)
            assertEquals("test@example.com very happy\n", entity.readText())
        }
    }

    @Test
    fun `records happiness by posting JSON`() {
        server.execute(HttpPost("/happiness").apply {
            entity = StringEntity("""{"email":"test@example.com", "happiness":"very happy"}""", ContentType.APPLICATION_JSON)
        }).apply {
            assertEquals(HttpStatus.SC_NO_CONTENT, statusLine.statusCode)
        }

        // FIXME
//        assertEquals(listOf<ResolvedEvent>(),
//                server.eventSource.readStream().readStreamForwards(streamId("happiness", "test@example.com")).collect(toList()))

        server.execute(HttpGet("/happiness")).apply {
            assertEquals(HttpStatus.SC_OK, statusLine.statusCode)
            assertEquals("test@example.com very happy\n", entity.readText())
        }
    }

    @Test
    fun `rejects happiness in unsupported format`() {
        server.execute(HttpPost("/happiness").apply {
            entity = StringEntity("""<record-happiness><email>test@example.com</email><happiness>very happy</happiness></record-happiness>""", ContentType.APPLICATION_XML)
        }).apply {
            assertEquals(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE, statusLine.statusCode)
        }
    }

    private fun Map<String, String>.toFormEntity() =
            UrlEncodedFormEntity(entries.map { (key, value) -> BasicNameValuePair(key, value) }, Charsets.UTF_8)

    private fun HttpEntity.readText() = EntityUtils.toString(this)
    private fun String.sortLines() = split("\n").filter { it.isNotBlank() }.sorted().joinToString("\n")
}
