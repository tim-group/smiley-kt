package com.timgroup.smileykt

import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.InternetHeaders
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

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

        private fun resourceUri(name: String): URI {
            return JavaMailEmailer::class.java.getResource(name)?.toURI() ?: throw IllegalArgumentException("Resource not found: $name")
        }
    }

    override fun sendHtmlEmail(subject: String, htmlBody: String, toAddress: String) {
        val multipartBody = MimeMultipart("related")

        multipartBody.addTextPart(htmlBody, "text/html")
        attachments.forEach { it.addTo(multipartBody) }

        val message = MimeMessage(session)
        message.addRecipient(Message.RecipientType.TO, InternetAddress(toAddress))
        message.setSubject(subject, "utf-8")
        message.setContent(multipartBody)
        Transport.send(message)
    }

    private val attachments = Emotion.values()
            .map { emotion -> emotion.name.toLowerCase() }
            .map { mood -> AttachmentResource(resourceUri("$mood-face.png"), "$mood-face", "image/png") }

    private data class AttachmentResource(val uri: URI, val id: String, val mimeType: String) {
        private val content = uri.toURL().openStream().use { it.readBytes() }
        private val base64Content = Base64.getMimeEncoder().encode(content)

        fun addTo(mimeMultipart: MimeMultipart) {
            mimeMultipart.addAttachment(base64Content, id, mimeType)
        }
    }
}

internal fun MimeMultipart.addTextPart(content: String, baseMimeType: String) {
    addBodyPart(MimeBodyPart(InternetHeaders().apply {
        setHeader("Content-Type","$baseMimeType; charset=\"utf-8\"")
    }, content.toByteArray()))
}

internal fun MimeMultipart.addAttachment(content: ByteArray, id: String, baseMimeType: String) {
    addBodyPart(MimeBodyPart(InternetHeaders().apply {
        setHeader("Content-Type",baseMimeType)
        setHeader("Content-ID", "<$id>")
        setHeader("Content-Transfer-Encoding", "base64")
    }, content))
}

object JavaMailDemo {
    @JvmStatic
    fun main(args: Array<String>) {
        JavaMailEmailer(Session.getInstance(System.getProperties())).sendHtmlEmail("this is a test of the JavaMailEmailer", """
            <p> This is a test of the JavaMailEmailer </p>

            <p> Try including an attachment: </p>

            <p> <img src="cid:happy-face" alt="happy" /> </p>
        """.trimIndent(), args[0])
    }
}
