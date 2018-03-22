package com.timgroup.smileykt.events

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.timgroup.eventstore.api.EventRecord
import com.timgroup.eventstore.api.NewEvent
import com.timgroup.smileykt.Emotion
import java.time.LocalDate

object EventCodecs {
    private val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    fun serialize(happinessReceived: Event): ByteArray {
        return objectMapper.writeValueAsBytes(happinessReceived)
    }

    fun serializeEvent(happinessReceived: Event): NewEvent {
        return NewEvent.newEvent("HappinessReceived", serialize(happinessReceived))
    }

    fun deserialize(serialized: ByteArray): Event {
        return objectMapper.readValue<HappinessReceived>(serialized)
    }

    fun deserializeEvent(serialized: EventRecord): Event {
        require(serialized.eventType() == "HappinessReceived")
        return deserialize(serialized.data())
    }
}

interface Event
data class HappinessReceived(val email: String, val date: LocalDate, val emotion: Emotion) : Event
