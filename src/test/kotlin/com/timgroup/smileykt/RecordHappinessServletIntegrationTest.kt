package com.timgroup.smileykt

import com.timgroup.eventstore.api.EventRecord.eventRecord
import com.timgroup.eventstore.api.NewEvent.newEvent
import com.timgroup.eventstore.api.StreamId.streamId
import org.apache.http.HttpStatus
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicNameValuePair
import org.junit.Rule
import org.junit.Test
import java.util.stream.Collectors.toList
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
                newEvent("HappinessReceived", Emotion.HAPPY.toByteArray())
        ))
        server.execute(HttpGet("/happiness")).apply {
            assertEquals(HttpStatus.SC_OK, statusLine.statusCode)
            assertEquals("test@example.com HAPPY\n", entity.readText())
        }
    }

    @Test
    fun `aggregates happiness of single user`() {
        server.eventSource.writeStream().write(streamId("happiness", "test@example.com"), listOf(
                newEvent("HappinessReceived", Emotion.HAPPY.toByteArray()),
                newEvent("HappinessReceived", Emotion.SAD.toByteArray())
        ))
        server.eventSource.writeStream().write(streamId("happiness", "zzzz@example.com"), listOf(
                newEvent("HappinessReceived", Emotion.HAPPY.toByteArray())
        ))
        server.execute(HttpGet("/happiness")).apply {
            assertEquals(HttpStatus.SC_OK, statusLine.statusCode)
            assertEquals("test@example.com SAD\nzzzz@example.com HAPPY", entity.readText()!!.sortLines())
        }
    }

    @Test
    fun `records happiness`() {
        server.execute(HttpPost("/happiness").apply {
            entity = mapOf(
                    "email" to "test@example.com",
                    "emotion" to "ECSTATIC").toFormEntity()
        }).apply {
            assertEquals(HttpStatus.SC_NO_CONTENT, statusLine.statusCode)
        }

        val streamId = streamId("happiness", "test@example.com")
        assertEquals(listOf(happinessReceivedEvent("test@example.com", Emotion.ECSTATIC)),
        server.eventSource.readStream().readStreamForwards(streamId).map { re -> re.eventRecord() }.collect(toList()))

        server.execute(HttpGet("/happiness")).apply {
            assertEquals(HttpStatus.SC_OK, statusLine.statusCode)
            assertEquals("test@example.com ECSTATIC\n", entity.readText())
        }
    }

    @Test
    fun `records happiness by posting JSON`() {
        server.execute(HttpPost("/happiness").apply {
            entity = StringEntity("""{"email":"test@example.com", "emotion":"ECSTATIC"}""", ContentType.APPLICATION_JSON)
        }).apply {
            assertEquals(HttpStatus.SC_NO_CONTENT, statusLine.statusCode)
        }

        val streamId = streamId("happiness", "test@example.com")
        assertEquals(listOf(happinessReceivedEvent("test@example.com", Emotion.ECSTATIC)),
                server.eventSource.readStream().readStreamForwards(streamId).map { re -> re.eventRecord() }.collect(toList()))

        server.execute(HttpGet("/happiness")).apply {
            assertEquals(HttpStatus.SC_OK, statusLine.statusCode)
            assertEquals("test@example.com ECSTATIC\n", entity.readText())
        }
    }

    @Test
    fun `rejects happiness in unsupported format`() {
        server.execute(HttpPost("/happiness").apply {
            entity = StringEntity("""<record-happiness><email>test@example.com</email><happiness>ECSTATIC</happiness></record-happiness>""", ContentType.APPLICATION_XML)
        }).apply {
            assertEquals(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE, statusLine.statusCode)
        }
    }

    @Test
    fun `rejects happiness not in specified list in form data`() {
        server.execute(HttpPost("/happiness").apply {
            entity = mapOf(
                    "email" to "test@example.com",
                    "emotion" to "NOT AT ALL HAPPY").toFormEntity()
        }).apply {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusLine.statusCode)
        }
    }

    @Test
    fun `rejects happiness not in specified list in JSON`() {
        server.execute(HttpPost("/happiness").apply {
            entity = StringEntity("""{"email":"test@example.com", "emotion":"NOT AT ALL HAPPY"}""", ContentType.APPLICATION_JSON)
        }).apply {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusLine.statusCode)
        }
    }

    private fun Map<String, String>.toFormEntity() =
            UrlEncodedFormEntity(entries.map { (key, value) -> BasicNameValuePair(key, value) }, Charsets.UTF_8)

    private fun String.sortLines() = split("\n").filter { it.isNotBlank() }.sorted().joinToString("\n")

    private fun happinessReceivedEvent(email: String, emotion: Emotion) = eventRecord(server.clock.instant(), streamId("happiness", email), 0L, "HappinessReceived", emotion.toByteArray(), ByteArray(0))
}
