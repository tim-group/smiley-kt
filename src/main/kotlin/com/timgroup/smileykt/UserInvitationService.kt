package com.timgroup.smileykt

import com.google.common.util.concurrent.AbstractScheduledService
import java.time.Clock
import java.util.concurrent.TimeUnit

class UserInvitationService(
        userInvitationsRepository: UserInvitationsRepository,
        clock: Clock,
        users: Set<UserDefinition>
) : AbstractScheduledService() {
    private val trigger = InvitationTrigger(userInvitationsRepository, clock, users)

    override fun scheduler(): Scheduler = Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.MINUTES)

    override fun runOneIteration() {
        trigger.launch { date, emailAddress ->
            println("send invitation for $date to $emailAddress")
        }
    }
}
