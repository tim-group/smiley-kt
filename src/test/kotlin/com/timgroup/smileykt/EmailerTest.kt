package com.timgroup.smileykt

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.cast
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.present
import org.junit.Test
import java.util.*
import javax.mail.Address
import javax.mail.Message
import javax.mail.internet.InternetAddress

class EmailerTest {
    private val senderAddress = randomAddress()
    private val testSession = buildTestJavaMailSession(Properties().apply {
        setProperty("mail.smtp.from", senderAddress)
    })

    private fun sentMessage() = JavaMailTestTransport.sentMessage(testSession)

    @Test
    fun `sends an email`() {
        val to = randomAddress()
        val subject = randomString()
        val body = randomString()
        val emailer = Emailer(testSession)
        emailer.sendHtmlEmail(subject, body, to)
        assertThat(sentMessage(), present(
                has(JavaMailTestTransport.MessagePacket::recipients, equalTo(setOf<Address>(InternetAddress(to))))
                and has(JavaMailTestTransport.MessagePacket::message,
                        has(Message::getSubject, equalTo(subject))
                        and has(Message::getAllRecipients, arrayContainingInOrder<Address>(InternetAddress(to)))
                        and has(Message::getContentType, equalTo("text/html; charset=\"utf-8\""))
                        and has(Message::getContent, cast(equalTo(body))))
        ))
    }
}

fun <T> arrayContainingInOrder(vararg elements: T): Matcher<Array<out T>> {
    return object : Matcher.Primitive<Array<out T>>() {
        private val list = elements.toList()

        override val description = "array containing $list"

        override fun invoke(actual: Array<out T>): MatchResult {
            val actualList = actual.toList()
            if (actualList.size != list.size) {
                return MatchResult.Mismatch("had ${actualList.size} elements: $actualList")
            }
            for (i in 0 until list.size) {
                val actualElement = actualList[i]
                val expectedElement = list[i]
                if (actualElement != expectedElement) {
                    return MatchResult.Mismatch("element $i was $actualElement")
                }
            }
            return MatchResult.Match
        }
    }
}
