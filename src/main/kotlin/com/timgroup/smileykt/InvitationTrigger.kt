package com.timgroup.smileykt

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class InvitationTrigger(
        private val userInvitationsRepository: UserInvitationsRepository,
        private val clock: Clock,
        private val users: Set<UserDefinition>
) {
    private val initialDate: LocalDate = LocalDateTime.now(clock).toInvitationDate()

    fun launch(launcher: (LocalDate, String) -> Unit) {
        val now = Instant.now(clock)
        users.forEach { user ->
            val localDateTime = now.atZone(ZoneId.systemDefault()).toLocalDateTime()
            val nextInvitationDate = localDateTime.toInvitationDate()
            val lastInvitationDate = userInvitationsRepository.latestInvitationSentTo(user.emailAddress) ?: initialDate
            if (nextInvitationDate > lastInvitationDate) {
                launcher(nextInvitationDate, user.emailAddress)
            }
        }
    }
}

internal val invitationTime: LocalTime = LocalTime.parse("17:00")

fun LocalDateTime.toInvitationDate(): LocalDate {
    val time = toLocalTime()
    val date = toLocalDate()

    return if (time < invitationTime)
        date.minusDays(1)
    else
        date
}