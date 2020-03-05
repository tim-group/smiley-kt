package com.timgroup.smileykt

import com.google.common.io.ByteStreams
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.hasSize
import com.timgroup.eventstore.api.NewEvent.newEvent
import com.timgroup.eventstore.api.StreamId.streamId
import com.timgroup.eventstore.memory.JavaInMemoryEventStore
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream
import org.apache.http.HttpStatus
import org.apache.http.StatusLine
import org.apache.http.client.methods.HttpGet
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import kotlin.text.Charsets.UTF_8

class EventStoreResourcesIntegrationTest {
    @get:Rule
    val server = ServerRule()

    @Test
    fun `produces cpio archive of events`() {
        server.eventSource.writeStream().write(streamId("test", "test"), listOf(
                newEvent("SomeEvent", "{ }".toByteArray()),
                newEvent("SomeEvent", "{ }".toByteArray(), "{ }".toByteArray())
        ))
        server.execute(HttpGet("/eventstore/archive")).apply {
            assertThat(statusLine, has(StatusLine::getStatusCode, equalTo(HttpStatus.SC_OK)))

            val entries = CpioArchiveInputStream(entity.content).use { cpio ->
                cpio.map { entry ->
                    val data = ByteArray(entry.size.toInt())
                    ByteStreams.readFully(cpio, data)
                    CpioEntry(entry.name, entry.mode, entry.lastModifiedDate.toInstant(), data.toString(UTF_8))
                }
            }

            assertThat(entries[0], has(CpioEntry::name, equalTo("00000001.2017-12-08T12:13:05Z.test.test.0.SomeEvent.data.txt"))
                                    and has(CpioEntry::modeInOctal, equalTo("100644"))
                                    and has(CpioEntry::timestamp, equalTo(Instant.parse("2017-12-08T12:13:05Z"))))
            assertThat(entries[1], has(CpioEntry::name, equalTo("00000002.2017-12-08T12:13:05Z.test.test.1.SomeEvent.data.txt")))
            assertThat(entries[2], has(CpioEntry::name, equalTo("00000002.2017-12-08T12:13:05Z.test.test.1.SomeEvent.metadata.txt")))
            assertThat(entries[3], has(CpioEntry::name, equalTo("position.txt"))
                                    and has(CpioEntry::modeInOctal, equalTo("100644"))
                                    and !has(CpioEntry::timestamp, equalTo(Instant.EPOCH))
                                    and !has(CpioEntry::text, equalTo(EmptyStorePosition)))
            assertThat(entries, hasSize(equalTo(4)))
        }
    }

    @Test
    fun `produces empty archive of events`() {
        server.execute(HttpGet("/eventstore/archive")).apply {
            assertThat(statusLine, has(StatusLine::getStatusCode, equalTo(HttpStatus.SC_OK)))

            val entries = CpioArchiveInputStream(entity.content).use { cpio ->
                cpio.map { entry ->
                    val data = ByteArray(entry.size.toInt())
                    ByteStreams.readFully(cpio, data)
                    CpioEntry(entry.name, entry.mode, entry.lastModifiedDate.toInstant(), data.toString(UTF_8))
                }
            }

            assertThat(entries[0], has(CpioEntry::name, equalTo("position.txt"))
                    and has(CpioEntry::modeInOctal, equalTo("100644"))
                    and has(CpioEntry::timestamp, equalTo(Instant.EPOCH))
                    and has(CpioEntry::text, equalTo(EmptyStorePosition)))
            assertThat(entries, hasSize(equalTo(1)))
        }
    }

    data class CpioEntry(val name: String, val mode: Long, val timestamp: Instant, val text: String) {
        val modeInOctal: String = mode.toString(8)
    }

    private inline fun <T> CpioArchiveInputStream.map(fn: (CpioArchiveEntry) -> T): List<T> {
        val list = mutableListOf<T>()
        var entry: CpioArchiveEntry? = nextCPIOEntry
        while (entry != null) {
            list += fn(entry)
            entry = nextCPIOEntry
        }
        return list
    }

    private val EmptyStorePosition: String
        get() = JavaInMemoryEventStore(Clock.systemUTC()).run { storePositionCodec().serializePosition(emptyStorePosition()) }
}
