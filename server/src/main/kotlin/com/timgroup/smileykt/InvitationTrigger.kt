package com.timgroup.smileykt

import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.MonthDay
import java.time.ZonedDateTime
import java.util.*

class InvitationTrigger(
        private val userInvitationsRepository: UserInvitationsRepository,
        private val clock: Clock,
        private val users: Set<UserDefinition>
) {
    private val initialDate: LocalDate = ZonedDateTime.now(clock).toInvitationDate()

    fun launch(): List<InvitationToSend> {
        val now = Instant.now(clock)
        val output = arrayListOf<InvitationToSend>()
        users.forEach { user ->
            val zonedDateTime = now.atZone(user.timeZone)
            val nextInvitationDate = zonedDateTime.toInvitationDate()
            val lastInvitationDate = userInvitationsRepository.latestInvitationSentTo(user.emailAddress) ?: initialDate
            if (nextInvitationDate > lastInvitationDate && filter(nextInvitationDate)) {
                output.add(InvitationToSend(user.emailAddress, nextInvitationDate))
            }
        }
        return output
    }

    private fun filter(date: LocalDate): Boolean {
        if (date.dayOfWeek in nonWorkingDaysOfWeek)
            return false

        if (MonthDay.from(date) in nonWorkingDaysOfYear)
            return false

        return true
    }

    data class InvitationToSend(val emailAddress: String, val date: LocalDate)
}

internal val nonWorkingDaysOfWeek: Set<DayOfWeek> = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
internal val nonWorkingDaysOfYear: Set<MonthDay> = listOf("--12-25", "--12-26").map(MonthDay::parse).toSet()
internal val invitationTime: LocalTime = LocalTime.parse("17:00")

internal fun ZonedDateTime.toInvitationDate(): LocalDate {
    val time = toLocalTime()
    val date = toLocalDate()

    return if (time < invitationTime)
        date.minusDays(1)
    else
        date
}