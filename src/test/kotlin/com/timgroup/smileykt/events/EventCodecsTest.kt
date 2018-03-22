package com.timgroup.smileykt.events

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.cast
import com.natpryce.hamkrest.equalTo
import com.timgroup.smileykt.Emotion
import org.araqnid.hamkrest.json.bytesEquivalentTo
import org.junit.Test
import java.time.LocalDate

class EventCodecsTest {

    @Test
    fun `serialises a HappinessReceived event`() {
        val event = HappinessReceived(
                email = "user@acuris.com",
                date = LocalDate.of(2018, 1, 5),
                emotion = Emotion.INDIFFERENT
        )
        val serialized = EventCodecs.serialize(event)

        assertThat(serialized, bytesEquivalentTo("""{
            email:"user@acuris.com",
            date:"2018-01-05",
            emotion:"INDIFFERENT"
         }"""))
    }

    @Test
    fun `deserialises a json bytes into the HappinessReceived event`() {
        val event = HappinessReceived(
                email = "user@acuris.com",
                date = LocalDate.of(2018, 1, 5),
                emotion = Emotion.INDIFFERENT
        )

        val deserialized = EventCodecs.deserialize("HappinessReceived", """{
            "email":"user@acuris.com",
            "date":"2018-01-05",
            "emotion":"INDIFFERENT"
         }""".toByteArray())

        assertThat(deserialized, cast(equalTo(event)))
    }

    @Test
    fun `serialises a InvitationEmailSent event`() {
        val event = InvitationEmailSent(
                recipient = "user@acuris.com",
                date = LocalDate.of(2018, 1, 5)
        )
        val serialized = EventCodecs.serialize(event)

        assertThat(serialized, bytesEquivalentTo("""{
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

        val deserialized = EventCodecs.deserialize("InvitationEmailSent", """{
            "recipient":"user@acuris.com",
            "date":"2018-01-05"
         }""".toByteArray())

        assertThat(deserialized, cast(equalTo(event)))
    }
}