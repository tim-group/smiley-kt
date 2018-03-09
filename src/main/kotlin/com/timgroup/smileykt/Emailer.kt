package com.timgroup.smileykt

import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class Emailer {
    val properties = Properties().apply {
        setProperty("mail.smtp.host", "localhost")
    }
    val session = Session.getDefaultInstance(properties)

    fun sendHtmlEmail(subject: String, htmlBody: String, toAddress: String) {
        val message = MimeMessage(session)
        message.addRecipient(Message.RecipientType.TO, InternetAddress(toAddress))
        message.subject = subject
        message.setContent(htmlBody, "text/html")
        Transport.send(message)
    }
}