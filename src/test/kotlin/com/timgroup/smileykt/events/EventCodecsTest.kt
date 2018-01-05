package com.timgroup.smileykt.events

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.timgroup.smileykt.Emotion
import org.araqnid.hamkrest.json.bytesEquivalentTo
import org.junit.Test
import java.time.LocalDate

class EventCodecsTest {

    @Test
    fun `serialises a HappinessReceived event`() {
        val happinessReceived = HappinessReceived(
                email = "user@acuris.com",
                date = LocalDate.of(2018, 1, 5),
                emotion = Emotion.INDIFFERENT
        )
        val serialized = EventCodecs.serialize(happinessReceived)

        assertThat(serialized, bytesEquivalentTo("""{
            email:"user@acuris.com",
            date:"2018-01-05",
            emotion:"INDIFFERENT"
         }"""))
    }

    @Test
    fun `desrialises a json bytes into the HappinessReceived event`() {
        val happinessReceived = HappinessReceived(
                email = "user@acuris.com",
                date = LocalDate.of(2018, 1, 5),
                emotion = Emotion.INDIFFERENT
        )

        val deserialized = EventCodecs.deserialize("""{
            "email":"user@acuris.com",
            "date":"2018-01-05",
            "emotion":"INDIFFERENT"
         }""".toByteArray())

        assertThat(deserialized, equalTo(happinessReceived))
    }
}