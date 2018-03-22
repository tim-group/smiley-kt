package com.timgroup.smileykt

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.timgroup.clocks.testing.ManualClock
import com.timgroup.eventstore.memory.InMemoryEventSource
import com.timgroup.eventstore.memory.JavaInMemoryEventStore
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

        assertThat(capture(trigger), equalTo(emptyList()))
    }

    @Test
    fun `sends email to new user after 5pm on first day`() {
        val clock = ManualClock(Instant.parse("2017-12-08T12:13:05Z"), ZoneOffset.UTC)
        val eventSource = InMemoryEventSource(JavaInMemoryEventStore(clock))
        val trigger = InvitationTrigger(UserInvitationsRepository(eventSource), clock, setOf(
                UserDefinition("abc@example.com")
        ))

        clock.advanceTo(Instant.parse("2017-12-08T18:00:00Z"))

        assertThat(capture(trigger), equalTo(listOf(
                LocalDate.parse("2017-12-09") to "abc@example.com"
        )))
    }

    private fun capture(trigger: InvitationTrigger): List<Pair<LocalDate, String>> {
        val captured = mutableListOf<Pair<LocalDate, String>>()
        trigger.launch { date, emailAddress -> captured += date to emailAddress }
        return captured
    }
}
