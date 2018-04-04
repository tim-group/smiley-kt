package com.timgroup.smileykt

import com.timgroup.eventstore.api.EventRecord
import com.timgroup.eventstore.api.EventSource
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream
import java.time.Instant
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.StreamingOutput

@Path("eventstore")
class EventStoreResources(eventSource: EventSource) {
    private val eventReader = eventSource.readAll()

    @GET
    @Path("archive")
    @Produces("application/x-cpio")
    fun dumpAsArchive() = StreamingOutput { out ->
        var outputPosition = 0
        CpioArchiveOutputStream(out).use { cpio ->
            eventReader.readAllForwards().use { eventStream ->
                eventStream.forEachOrdered { re ->
                    val baseFilename = formatBaseFilename(++outputPosition, re.eventRecord())
                    val dataFilename = "$baseFilename.data.txt"
                    val dataEntry = CpioArchiveEntry(dataFilename).apply {
                        size = re.eventRecord().data().size.toLong()
                        lastModified = re.eventRecord().timestamp()
                    }
                    cpio.putArchiveEntry(dataEntry)
                    cpio.write(re.eventRecord().data())
                    cpio.closeArchiveEntry()
                    if (re.eventRecord().metadata().isNotEmpty()) {
                        val metadataEntry = CpioArchiveEntry(dataFilename).apply {
                            size = re.eventRecord().metadata().size.toLong()
                            lastModified = re.eventRecord().timestamp()
                        }
                        cpio.putArchiveEntry(metadataEntry)
                        cpio.write(re.eventRecord().metadata())
                        cpio.closeArchiveEntry()
                    }
                }
            }
        }
    }

    private var CpioArchiveEntry.lastModified: Instant
        get() = Instant.ofEpochSecond(time)
        set(instant) {
            time = instant.epochSecond
        }

    private fun formatBaseFilename(position: Int, eventRecord: EventRecord) = buildString {
        append("%08x".format(position))
        append('.')
        append(eventRecord.timestamp())
        append('.')
        appendEncoded(eventRecord.streamId().category())
        append('.')
        appendEncoded(eventRecord.streamId().id())
        append('.')
        append(eventRecord.eventNumber())
        append('.')
        appendEncoded(eventRecord.eventType())
    }

    private fun Appendable.appendEncoded(str: CharSequence) {
        str.forEach { chr ->
            if (chr != '%' && chr != '.' && chr.toInt() in 33 until 127) {
                append(chr)
            }
            else {
                append('%')
                append(String.format("%04x", chr.toInt()))
            }
        }
    }
}
