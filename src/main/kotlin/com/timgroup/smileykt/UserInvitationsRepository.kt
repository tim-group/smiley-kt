package com.timgroup.smileykt

import com.timgroup.eventstore.api.EventSource
import com.timgroup.eventstore.api.NoSuchStreamException
import com.timgroup.eventstore.api.StreamId
import com.timgroup.eventstore.api.StreamId.streamId
import com.timgroup.smileykt.events.EventCodecs
import com.timgroup.smileykt.events.InvitationEmailSent
import java.time.LocalDate

class UserInvitationsRepository(eventSource: EventSource) {
    private val streamReader = eventSource.readStream()
    private val streamWriter = eventSource.writeStream()

    fun latestInvitationSentTo(emailAddress: String): LocalDate? {
        val resolvedEvent = try {
            streamReader.readLastEventInStream(streamId(emailAddress))
        } catch (e: NoSuchStreamException) {
            return null
        }

        val event = EventCodecs.deserializeEvent(resolvedEvent.eventRecord())

        return (event as InvitationEmailSent).date
    }

    fun registerInvitationSent(emailAddress: String, date: LocalDate) {
        streamWriter.write(streamId(emailAddress), listOf(
                EventCodecs.serializeEvent(InvitationEmailSent(emailAddress, date))
        ))
    }

    private fun streamId(emailAddress: String): StreamId = streamId("invitations", emailAddress)
}