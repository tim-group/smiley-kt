package com.timgroup.smileykt.events

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.timgroup.eventstore.api.EventRecord
import com.timgroup.smileykt.Emotion
import java.time.LocalDate

object EventCodecs {
    private val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    fun serialize(happinessReceived: HappinessReceived): ByteArray {
        return objectMapper.writeValueAsBytes(happinessReceived)
    }

    fun deserialize(serialized: EventRecord): HappinessReceived {
        TODO()
    }
}

data class HappinessReceived(val email: String, val date: LocalDate, val emotion: Emotion)