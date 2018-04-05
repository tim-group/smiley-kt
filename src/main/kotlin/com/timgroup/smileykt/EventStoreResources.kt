package com.timgroup.smileykt

import com.timgroup.eventstore.api.EventRecord
import com.timgroup.eventstore.api.EventSource
import com.timgroup.eventstore.api.ResolvedEvent
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream
import org.apache.commons.compress.archivers.cpio.CpioConstants
import java.time.Instant
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.StreamingOutput

@Path("eventstore")
class EventStoreResources(eventSource: EventSource) {
    private val positionCodec = eventSource.positionCodec()
    private val eventReader = eventSource.readAll()

    @GET
    @Path("archive")
    @Produces("application/x-cpio")
    fun dumpAsArchive() = StreamingOutput { out ->
        var outputPosition = 0
        val entryMode = CpioConstants.C_ISREG.toLong() or "644".toLong(8)
        CpioArchiveOutputStream(out).use { cpio ->
            var lastEvent: ResolvedEvent? = null
            eventReader.readAllForwards().use { eventStream ->
                eventStream.forEachOrdered { re ->
                    val baseFilename = formatBaseFilename(++outputPosition, re.eventRecord())
                    val dataEntry = CpioArchiveEntry("$baseFilename.data.txt").apply {
                        size = re.eventRecord().data().size.toLong()
                        lastModified = re.eventRecord().timestamp()
                        mode = entryMode
                    }
                    cpio.putArchiveEntry(dataEntry)
                    cpio.write(re.eventRecord().data())
                    cpio.closeArchiveEntry()
                    if (re.eventRecord().metadata().isNotEmpty()) {
                        val metadataEntry = CpioArchiveEntry("$baseFilename.metadata.txt").apply {
                            size = re.eventRecord().metadata().size.toLong()
                            lastModified = re.eventRecord().timestamp()
                            mode = entryMode
                        }
                        cpio.putArchiveEntry(metadataEntry)
                        cpio.write(re.eventRecord().metadata())
                        cpio.closeArchiveEntry()
                    }
                }
            }
            val (position, timestamp) =
                if (lastEvent != null) {
                    lastEvent.position() to lastEvent.eventRecord().timestamp()
                }
                else {
                    eventReader.emptyStorePosition() to Instant.EPOCH
                }
            val positionContent = positionCodec.serializePosition(position).toByteArray()
            val positionEntry = CpioArchiveEntry("position.txt").apply {
                size = positionContent.size.toLong()
                lastModified = timestamp
                mode = entryMode
            }
            cpio.putArchiveEntry(positionEntry)
            cpio.write(positionContent)
            cpio.closeArchiveEntry()
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
