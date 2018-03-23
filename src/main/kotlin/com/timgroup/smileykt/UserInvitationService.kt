package com.timgroup.smileykt

import com.google.common.util.concurrent.AbstractScheduledService
import java.time.Clock
import java.util.concurrent.TimeUnit

class UserInvitationService(
        userInvitationsRepository: UserInvitationsRepository,
        clock: Clock,
        users: Set<UserDefinition>,
        private val emailGenerator: HtmlEmailGenerator
) : AbstractScheduledService() {
    private val trigger = InvitationTrigger(userInvitationsRepository, clock, users)

    override fun scheduler(): Scheduler = Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.MINUTES)

    override fun runOneIteration() {
        trigger.launch().forEach { (emailAddress, date) ->
            val emailContent = emailGenerator.emailFor(emailAddress, date)
            println("send invitation for $date to $emailAddress -\n$emailContent\n\n")
        }
    }
}
