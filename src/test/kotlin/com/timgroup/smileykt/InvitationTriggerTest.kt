package com.timgroup.smileykt

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.timgroup.clocks.testing.ManualClock
import com.timgroup.eventstore.api.StreamId.streamId
import com.timgroup.eventstore.memory.InMemoryEventSource
import com.timgroup.eventstore.memory.JavaInMemoryEventStore
import com.timgroup.smileykt.InvitationTrigger.InvitationToSend
import com.timgroup.smileykt.events.EventCodecs
import com.timgroup.smileykt.events.InvitationEmailSent
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class InvitationTriggerTest {
    @Test
    fun `does not immediately send email to new user on startup`() {
        val clock = ManualClock(Instant.parse("2017-12-08T12:13:05Z"), ZoneOffset.UTC)
        val eventSource = InMemoryEventSource(JavaInMemoryEventStore(clock))
        val trigger = InvitationTrigger(UserInvitationsRepository(eventSource), clock, setOf(
                UserDefinition("abc@example.com")
        ))

        assertThat(trigger.launch(), equalTo(emptyList()))
    }

    @Test
    fun `sends email to new user after 5pm on first day`() {
        val clock = ManualClock(Instant.parse("2017-12-08T12:13:05Z"), ZoneOffset.UTC)
        val eventSource = InMemoryEventSource(JavaInMemoryEventStore(clock))
        val trigger = InvitationTrigger(UserInvitationsRepository(eventSource), clock, setOf(
                UserDefinition("abc@example.com")
        ))

        clock.advanceTo(Instant.parse("2017-12-08T18:00:00Z"))

        assertThat(trigger.launch(), equalTo(listOf(
                InvitationToSend("abc@example.com", LocalDate.parse("2017-12-08"))
        )))
    }

    @Test
    fun `does not send email after email previously sent to user on first day`() {
        val clock = ManualClock(Instant.parse("2017-12-08T12:13:05Z"), ZoneOffset.UTC)
        val eventSource = InMemoryEventSource(JavaInMemoryEventStore(clock))
        val trigger = InvitationTrigger(UserInvitationsRepository(eventSource), clock, setOf(
                UserDefinition("abc@example.com")
        ))

        clock.advanceTo(Instant.parse("2017-12-08T18:00:00Z"))

        eventSource.writeStream().write(streamId("invitations", "abc@example.com"), listOf(
                EventCodecs.serializeEvent(InvitationEmailSent("abc@example.com", LocalDate.parse("2017-12-08")))
        ))

        assertThat(trigger.launch(), equalTo(emptyList()))
    }

    @Test
    fun `sends email to user after 5pm on next day after previous invitation`() {
        val clock = ManualClock(Instant.parse("2017-12-08T12:13:05Z"), ZoneOffset.UTC)
        val eventSource = InMemoryEventSource(JavaInMemoryEventStore(clock))
        eventSource.writeStream().write(streamId("invitations", "abc@example.com"), listOf(
                EventCodecs.serializeEvent(InvitationEmailSent("abc@example.com", LocalDate.parse("2017-12-07")))
        ))
        val trigger = InvitationTrigger(UserInvitationsRepository(eventSource), clock, setOf(
                UserDefinition("abc@example.com")
        ))

        clock.advanceTo(Instant.parse("2017-12-08T18:00:00Z"))

        assertThat(trigger.launch(), equalTo(listOf(
                InvitationToSend("abc@example.com", LocalDate.parse("2017-12-08"))
        )))
    }
}
