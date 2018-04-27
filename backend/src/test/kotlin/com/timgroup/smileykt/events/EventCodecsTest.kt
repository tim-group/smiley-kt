package com.timgroup.smileykt.events

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.cast
import com.natpryce.hamkrest.equalTo
import com.timgroup.eventstore.api.EventRecord
import com.timgroup.eventstore.api.StreamId
import com.timgroup.smileykt.Emotion
import org.araqnid.hamkrest.json.bytesEquivalentTo
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class EventCodecsTest {

    @Test
    fun `serialises a HappinessReceived event`() {
        val event = HappinessReceived(
                email = "user@acuris.com",
                date = LocalDate.of(2018, 1, 5),
                emotion = Emotion.NEUTRAL
        )
        val newEvent = EventCodecs.serializeEvent(event)

        assertThat(newEvent.type(), equalTo("HappinessReceived"))
        assertThat(newEvent.data(), bytesEquivalentTo("""{
            email:"user@acuris.com",
            date:"2018-01-05",
            emotion:"NEUTRAL"
         }"""))
    }

    @Test
    fun `deserialises a json bytes into the HappinessReceived event`() {
        val event = HappinessReceived(
                email = "user@acuris.com",
                date = LocalDate.of(2018, 1, 5),
                emotion = Emotion.NEUTRAL
        )

        val deserialized = EventCodecs.deserializeEvent(eventRecord("HappinessReceived", """{
            "email":"user@acuris.com",
            "date":"2018-01-05",
            "emotion":"NEUTRAL"
         }"""))

        assertThat(deserialized, cast(equalTo(event)))
    }

    @Test
    fun `serialises a InvitationEmailSent event`() {
        val event = InvitationEmailSent(
                recipient = "user@acuris.com",
                date = LocalDate.of(2018, 1, 5)
        )
        val newEvent = EventCodecs.serializeEvent(event)

        assertThat(newEvent.type(), equalTo("InvitationEmailSent"))
        assertThat(newEvent.data(), bytesEquivalentTo("""{
            recipient:"user@acuris.com",
            date:"2018-01-05"
         }"""))
    }

    @Test
    fun `deserialises a json bytes into the InvitationEmailSent event`() {
        val event = InvitationEmailSent(
                recipient = "user@acuris.com",
                date = LocalDate.of(2018, 1, 5)
        )

        val deserialized = EventCodecs.deserializeEvent(eventRecord("InvitationEmailSent", """{
            "recipient":"user@acuris.com",
            "date":"2018-01-05"
         }"""))

        assertThat(deserialized, cast(equalTo(event)))
    }

    private fun eventRecord(type: String, data: String): EventRecord {
        return EventRecord.eventRecord(Instant.EPOCH,
                StreamId.streamId("any", "any"),
                0L,
                type,
                data.toByteArray(),
                byteArrayOf())
    }
}
