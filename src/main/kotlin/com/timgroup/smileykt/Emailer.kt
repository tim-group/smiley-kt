package com.timgroup.smileykt

import org.slf4j.LoggerFactory
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

interface Emailer {
    fun sendHtmlEmail(subject: String, htmlBody: String, toAddress: String)
}

object DummyEmailer : Emailer {
    private val logger = LoggerFactory.getLogger(DummyEmailer::class.java)

    override fun sendHtmlEmail(subject: String, htmlBody: String, toAddress: String) {
        val output = """
            |Subject: $subject
            |To: $toAddress
            |Content-Type: text/html
        """.trimMargin() + "\n\n" + htmlBody + "\n"

        output.lineSequence().forEach { logger.info(it) }
    }
}

class JavaMailEmailer(private val session: Session = defaultSession) : Emailer {
    companion object {
        val defaultSession: Session = Session.getDefaultInstance(Properties().apply {
            setProperty("mail.smtp.host", "localhost")
        })
    }

    override fun sendHtmlEmail(subject: String, htmlBody: String, toAddress: String) {
        val message = MimeMessage(session)
        message.addRecipient(Message.RecipientType.TO, InternetAddress(toAddress))
        message.setSubject(subject, "utf-8")
        message.setContent(htmlBody, "text/html; charset=\"utf-8\"")
        Transport.send(message)
    }
}