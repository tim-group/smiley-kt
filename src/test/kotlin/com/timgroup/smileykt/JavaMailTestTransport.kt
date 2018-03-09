package com.timgroup.smileykt

import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import javax.mail.Address
import javax.mail.Message
import javax.mail.Provider
import javax.mail.Session
import javax.mail.Transport
import javax.mail.URLName

class JavaMailTestTransport(session: Session, urlName: URLName) : Transport(session, urlName) {
    companion object {
        private val sessionMessages = WeakHashMap<Session, Queue<MessagePacket>>()

        private fun autovivifySessionMessages(session: Session): Queue<MessagePacket> {
            val queue = sessionMessages[session]
            return if (queue == null) {
                val newQueue = LinkedBlockingQueue<MessagePacket>()
                sessionMessages[session] = newQueue
                newQueue
            } else {
                queue
            }
        }

        fun sentMessage(session: Session): MessagePacket? = autovivifySessionMessages(session).poll()
    }

    private val packetSink: Queue<MessagePacket> = autovivifySessionMessages(session)

    override fun sendMessage(msg: Message, addresses: Array<out Address>) {
        packetSink.add(MessagePacket(msg, addresses.toSet()))
    }

    override fun protocolConnect(host: String?, port: Int, user: String?, password: String?): Boolean {
        return true
    }

    data class MessagePacket(val message: Message, val recipients: Set<Address>)
}

fun buildTestJavaMailSession(sessionProperties: Properties): Session {
    val props = Properties(sessionProperties)
    props.setProperty("mail.smtp.class", JavaMailTestTransport::class.qualifiedName)
    val testSession = Session.getInstance(props)
    testSession.addProvider(Provider(Provider.Type.TRANSPORT, "smtp", JavaMailTestTransport::class.qualifiedName, "testing", "0.0.0"))
    return testSession
}
