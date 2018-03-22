package com.timgroup.smileykt.events

import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.timgroup.eventstore.api.EventRecord
import com.timgroup.eventstore.api.NewEvent
import com.timgroup.smileykt.Emotion
import java.time.LocalDate

object EventCodecs {
    private val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    fun serializeEvent(data: Event): NewEvent {
        return NewEvent.newEvent(data.javaClass.simpleName, objectMapper.writeValueAsBytes(data))
    }

    fun deserializeEvent(eventRecord: EventRecord): Event {
        return readerFor(eventRecord.eventType()).readValue(eventRecord.data())
    }

    private fun readerFor(eventType: String): ObjectReader {
        val type = when (eventType) {
            "HappinessReceived" -> HappinessReceived::class
            "InvitationEmailSent" -> InvitationEmailSent::class
            else -> throw IllegalArgumentException("Unsupported event type: $eventType")
        }
        return objectMapper.readerFor(type.java)
    }
}

sealed class Event
data class HappinessReceived(val email: String, val date: LocalDate, val emotion: Emotion) : Event()
data class InvitationEmailSent(val recipient: String, val date: LocalDate) : Event()
