package com.timgroup.smileykt

import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
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
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class InvitationTriggerTest {
    @Test
    fun `does not immediately send email to new user on startup`() {
        val clock = ManualClock(Instant.parse("2017-12-08T12:13:05Z"), ZoneOffset.UTC)
        val eventSource = InMemoryEventSource(JavaInMemoryEventStore(clock))
        val trigger = InvitationTrigger(UserInvitationsRepository(eventSource), clock, setOf(
                UserDefinition("abc@example.com", ZoneOffset.UTC)
        ))

        assertThat(trigger.launch(), equalTo(emptyList()))
    }

    @Test
    fun `sends email to new user after 5pm on first day`() {
        val clock = ManualClock(Instant.parse("2017-12-08T12:13:05Z"), ZoneOffset.UTC)
        val eventSource = InMemoryEventSource(JavaInMemoryEventStore(clock))
        val trigger = InvitationTrigger(UserInvitationsRepository(eventSource), clock, setOf(
                UserDefinition("abc@example.com", ZoneOffset.UTC)
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
                UserDefinition("abc@example.com", ZoneOffset.UTC)
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
                UserDefinition("abc@example.com", ZoneOffset.UTC)
        ))

        clock.advanceTo(Instant.parse("2017-12-08T18:00:00Z"))

        assertThat(trigger.launch(), equalTo(listOf(
                InvitationToSend("abc@example.com", LocalDate.parse("2017-12-08"))
        )))
    }

    @Test
    fun `does not send email for Saturday or Sunday, starts sending again on Monday`() {
        val clock = ManualClock(Instant.parse("2017-12-08T12:00:00Z"), ZoneOffset.UTC) // Friday before send time
        val eventSource = InMemoryEventSource(JavaInMemoryEventStore(clock))
        val trigger = InvitationTrigger(UserInvitationsRepository(eventSource), clock, setOf(
                UserDefinition("abc@example.com", ZoneOffset.UTC)
        ))

        clock.advanceTo(Instant.parse("2017-12-08T17:05:00Z")) // after send time

        val sent = arrayListOf<InvitationToSend>()
        for (n in 0..3) {
            sent += trigger.launch()
            clock.bump(1, ChronoUnit.DAYS)
        }

        assertThat(sent, equalTo(listOf(
                InvitationToSend("abc@example.com", LocalDate.parse("2017-12-08")), // Friday
                InvitationToSend("abc@example.com", LocalDate.parse("2017-12-11")) // Monday
        )))
    }

    @Test
    fun `does not send email for certain well-known dates`() {
        val clock = ManualClock(Instant.parse("2013-12-24T12:00:00Z"), ZoneOffset.UTC) // Tuesday before send time
        val eventSource = InMemoryEventSource(JavaInMemoryEventStore(clock))
        val trigger = InvitationTrigger(UserInvitationsRepository(eventSource), clock, setOf(
                UserDefinition("abc@example.com", ZoneOffset.UTC)
        ))

        clock.advanceTo(Instant.parse("2013-12-24T17:05:00Z")) // after send time

        val sent = arrayListOf<InvitationToSend>()
        for (n in 0..3) {
            sent += trigger.launch()
            clock.bump(1, ChronoUnit.DAYS)
        }

        assertThat(sent, equalTo(listOf(
                InvitationToSend("abc@example.com", LocalDate.parse("2013-12-24")), // Tuesday
                InvitationToSend("abc@example.com", LocalDate.parse("2013-12-27")) // Thursday
        )))
    }

    @Test
    fun `sends email to new user after 5pm of their local time`() {
        val clock = ManualClock(Instant.parse("2017-12-08T12:13:05Z"), ZoneOffset.UTC)
        val eventSource = InMemoryEventSource(JavaInMemoryEventStore(clock))
        val trigger = InvitationTrigger(UserInvitationsRepository(eventSource), clock, setOf(
                UserDefinition("abc@example.com", ZoneId.of("Europe/Vienna"))
        ))

        val sent = mutableListOf<Pair<Instant, InvitationToSend>>()

        for (n in 0..23) {
            trigger.launch().forEach {
                sent += Instant.now(clock).truncatedTo(ChronoUnit.HOURS) to it
            }
            clock.bump(1, ChronoUnit.HOURS)
        }

        assertThat(sent.take(1), equalTo(listOf(
                Instant.parse("2017-12-08T16:00:00Z") to InvitationToSend("abc@example.com", LocalDate.parse("2017-12-08"))
        )))
    }

    private fun <K, V> multimapOf(vararg pairs: Pair<K, V>): Multimap<K, V> {
        return ImmutableMultimap.builder<K, V>().run {
            pairs.forEach { (key, value) ->
                put(key, value)
            }
            build()
        }
    }
}
