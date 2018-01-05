package com.timgroup.smileykt.events

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.timgroup.smileykt.Emotion
import java.time.LocalDate

object EventCodecs {
    private val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    fun serialize(happinessReceived: HappinessReceived): ByteArray {
        return objectMapper.writeValueAsBytes(happinessReceived)
    }

    fun deserialize(serialized: ByteArray): HappinessReceived {
        return objectMapper.readValue(serialized)
    }
}

data class HappinessReceived(val email: String, val date: LocalDate, val emotion: Emotion)