package com.timgroup.smileykt

import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class Emailer(private val session: Session = defaultSession) {
    companion object {
        val properties = Properties().apply {
            setProperty("mail.smtp.host", "localhost")
        }
        val defaultSession = Session.getDefaultInstance(properties)
    }

    fun sendHtmlEmail(subject: String, htmlBody: String, toAddress: String) {
        val message = MimeMessage(session)
        message.addRecipient(Message.RecipientType.TO, InternetAddress(toAddress))
        message.setSubject(subject, "utf-8")
        message.setContent(htmlBody, "text/html; charset=\"utf-8\"")
        Transport.send(message)
    }
}