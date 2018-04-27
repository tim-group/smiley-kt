package com.timgroup.smileykt

import com.google.common.collect.ImmutableList
import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.timgroup.clocks.testing.ManualClock
import com.timgroup.eventstore.api.StreamId
import com.timgroup.eventstore.memory.InMemoryEventSource
import com.timgroup.eventstore.memory.JavaInMemoryEventStore
import com.timgroup.smileykt.events.Event
import com.timgroup.smileykt.events.EventCodecs
import com.timgroup.smileykt.events.InvitationEmailSent
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.stream.Stream

class UserInvitationsRepositoryTest {
    @Test
    fun `remembers emails sent to users`() {
        val clock = ManualClock(Instant.parse("2017-12-08T12:13:05Z"), ZoneOffset.UTC)
        val eventSource = InMemoryEventSource(JavaInMemoryEventStore(clock))

        eventSource.writeStream().write(StreamId.streamId("invitations", "def@example.com"), listOf(
                EventCodecs.serializeEvent(InvitationEmailSent("def@example.com", LocalDate.parse("2018-03-01")))
        ))

        eventSource.writeStream().write(StreamId.streamId("invitations", "ghi@example.com"), listOf(
                EventCodecs.serializeEvent(InvitationEmailSent("ghi@example.com", LocalDate.parse("2018-03-01"))),
                EventCodecs.serializeEvent(InvitationEmailSent("ghi@example.com", LocalDate.parse("2018-03-02")))
        ))

        val repo = UserInvitationsRepository(eventSource)

        assertThat(repo.latestInvitationSentTo("abc@example.com"), absent())
        assertThat(repo.latestInvitationSentTo("def@example.com"), equalTo(LocalDate.parse("2018-03-01")))
        assertThat(repo.latestInvitationSentTo("ghi@example.com"), equalTo(LocalDate.parse("2018-03-02")))
    }

    @Test
    fun `registers email sent to users`() {
        val clock = ManualClock(Instant.parse("2017-12-08T12:13:05Z"), ZoneOffset.UTC)
        val eventSource = InMemoryEventSource(JavaInMemoryEventStore(clock))

        val repo = UserInvitationsRepository(eventSource)

        repo.registerInvitationSent("abc@example.com", LocalDate.parse("2018-03-01"))

        assertThat(eventSource.readAll().readAllForwards().toListAndClose().map { it.eventRecord().streamId() to EventCodecs.deserializeEvent(it.eventRecord()) },
                equalTo(listOf<Pair<StreamId, Event>>(
                        StreamId.streamId("invitations", "abc@example.com") to InvitationEmailSent("abc@example.com", LocalDate.parse("2018-03-01"))
                )))
    }

    private fun <T> Stream<T>.toListAndClose(): List<T> = use { it.collect(ImmutableList.toImmutableList()) }
}
