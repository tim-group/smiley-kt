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
    private val invitationTime: LocalTime = LocalTime.parse("17:00")
    private val initialDate: LocalDate = LocalDateTime.now(clock).let { localDateTime ->
        if (localDateTime.toLocalTime() > invitationTime) localDateTime.toLocalDate().plusDays(1) else localDateTime.toLocalDate()
    }

    fun launch(launcher: (LocalDate, String) -> Unit) {
        val now = Instant.now(clock)
        users.forEach { user ->
            val localDateTime = now.atZone(ZoneId.systemDefault()).toLocalDateTime()
            val nextInvitationDate = if (localDateTime.toLocalTime() > invitationTime) localDateTime.toLocalDate().plusDays(1) else localDateTime.toLocalDate()
            val lastInvitationDate = userInvitationsRepository.latestInvitationSentTo(user.emailAddress) ?: initialDate
            if (nextInvitationDate > lastInvitationDate) {
                launcher(nextInvitationDate, user.emailAddress)
            }
        }
    }
}