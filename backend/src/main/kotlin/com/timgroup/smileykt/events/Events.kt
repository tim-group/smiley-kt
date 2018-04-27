package com.timgroup.smileykt.events

import com.timgroup.smileykt.Emotion
import java.time.LocalDate

sealed class Event
data class HappinessReceived(val email: String, val date: LocalDate, val emotion: Emotion) : Event()
data class InvitationEmailSent(val recipient: String, val date: LocalDate) : Event()
