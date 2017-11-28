package com.timgroup.smileykt

import com.timgroup.eventstore.api.EventSource
import com.timgroup.eventstore.api.NewEvent.newEvent
import com.timgroup.eventstore.api.StreamId.streamId
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RecordHappinessServlet(eventSource: EventSource) : HttpServlet() {

    private val eventCategoryReader = eventSource.readCategory()
    private val eventStreamWriter = eventSource.writeStream()

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        when (req.contentType.toMimeType()) {
            "application/x-www-form-urlencoded" -> {
                val email: String = req.getParameter("email") ?: return resp.sendError(400, "Email must be supplied")
                val happiness: String = req.getParameter("happiness") ?: return resp.sendError(400, "Happiness must be supplied")
                recordHappiness(Happiness(email, happiness))
                resp.status = HttpServletResponse.SC_NO_CONTENT
            }
            "application/json" -> {
                recordHappiness(decode(req.inputStream))
                resp.status = HttpServletResponse.SC_NO_CONTENT
            }
            else -> resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE)
        }
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/plain"
        resp.writer.use { writer ->
            eventCategoryReader.readCategoryForwards("happiness").use { stream ->
                stream
                        .map { resolvedEvent ->
                            Happiness(resolvedEvent.eventRecord().streamId().id(),
                                    String(resolvedEvent.eventRecord().data()))
                        }
                        .forEach { (email, happiness) ->
                            writer.println("$email $happiness")
                        }
            }
        }
    }

    private fun String.toMimeType() = split(";")[0].toLowerCase()

    private fun recordHappiness(happinessObj: Happiness) {
        eventStreamWriter.write(streamId("happiness", happinessObj.email),
                listOf(newEvent("HappinessReceived", happinessObj.happiness.toByteArray())))
    }

}
