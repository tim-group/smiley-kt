package com.timgroup.smileykt

import com.google.common.util.concurrent.AbstractScheduledService
import org.slf4j.LoggerFactory
import java.time.Clock
import java.util.concurrent.TimeUnit

class UserInvitationService(
        private val userInvitationsRepository: UserInvitationsRepository,
        clock: Clock,
        users: Set<UserDefinition>,
        private val emailGenerator: HtmlEmailGenerator,
        private val emailer: Emailer
) : AbstractScheduledService() {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val trigger = InvitationTrigger(userInvitationsRepository, clock, users)

    override fun scheduler(): Scheduler = Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.MINUTES)

    override fun runOneIteration() {
        trigger.launch().forEach { (emailAddress, date) ->
            val emailContent = emailGenerator.emailFor(emailAddress, date)
            emailer.sendHtmlEmail("Tell us your feeling for today", emailContent, emailAddress)
            userInvitationsRepository.registerInvitationSent(emailAddress, date)
            logger.info("Sent invitation to $emailAddress for $date")
        }
    }
}
