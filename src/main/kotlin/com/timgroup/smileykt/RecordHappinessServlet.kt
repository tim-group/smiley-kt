package com.timgroup.smileykt

import com.fasterxml.jackson.databind.JsonMappingException
import com.timgroup.eventstore.api.EventSource
import com.timgroup.eventstore.api.StreamId.streamId
import com.timgroup.smileykt.events.EventCodecs
import com.timgroup.smileykt.events.HappinessReceived
import java.time.Clock
import java.time.LocalDate
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RecordHappinessServlet(eventSource: EventSource, private val clock: Clock) : HttpServlet() {

    private val eventCategoryReader = eventSource.readCategory()
    private val eventStreamWriter = eventSource.writeStream()

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        when (req.contentType.toMimeType()) {
            "application/x-www-form-urlencoded" -> {
                val email: String = req.getParameter("email") ?: return resp.sendError(400, "Email must be supplied")
                val emotionString: String = req.getParameter("emotion") ?: return resp.sendError(400, "Happiness must be supplied")
                val emotion = Emotion.valueOfOrNull(emotionString) ?: return resp.sendError(400, "Unknown emotion " + emotionString)
                recordHappiness(Happiness(email, emotion))
                resp.status = HttpServletResponse.SC_NO_CONTENT
            }
            "application/json" -> {
                try {
                    recordHappiness(decode(req.inputStream))
                } catch (e: JsonMappingException) {
                    return resp.sendError(400, e.message)
                }
                resp.status = HttpServletResponse.SC_NO_CONTENT
            }
            else -> resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE)
        }
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/plain"
        resp.writer.use { writer ->
            eventCategoryReader.readCategoryForwards("happiness").use { stream ->
                val emotions = mutableMapOf<String, MutableMap<LocalDate, Emotion>>()
                stream.forEach { resolvedEvent ->
                    val (email, date, emotion) = EventCodecs.deserializeEvent(resolvedEvent.eventRecord())
                    emotions.computeIfAbsent(email, { mutableMapOf() })[date] = emotion
                }
                emotions.forEach { (email, emotionsByDate) ->
                    emotionsByDate.forEach { (date, emotion) ->
                        writer.println("$date $email $emotion")
                    }
                }
            }
        }
    }

    private fun String.toMimeType() = split(";")[0].toLowerCase()

    private fun recordHappiness(happinessObj: Happiness) {
        eventStreamWriter.write(
                streamId("happiness", happinessObj.email),
                listOf(EventCodecs.serializeEvent(HappinessReceived(happinessObj.email, LocalDate.now(clock), happinessObj.emotion)))
        )
    }

    data class Happiness(val email: String, val emotion: Emotion)
}

