package com.timgroup.smileykt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.timgroup.eventstore.api.EventSource
import com.timgroup.eventstore.api.NewEvent.newEvent
import com.timgroup.eventstore.api.StreamId.streamId
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

data class HappinessObj(val email: String, val happiness: String)

class RecordHappinessServlet(eventSource: EventSource) : HttpServlet() {

    val eventCategoryReader = eventSource.readCategory()
    val eventStreamWriter = eventSource.writeStream()

    val mapper = jacksonObjectMapper()

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        when (req.contentType.toMimeType()) {
            "application/x-www-form-urlencoded" -> {
                val email: String = req.getParameter("email") ?: return resp.sendError(400, "Email must be supplied")
                val happiness: String = req.getParameter("happiness") ?: return resp.sendError(400, "Happiness must be supplied")
                recordHappiness(HappinessObj(email, happiness))
                resp.status = HttpServletResponse.SC_NO_CONTENT
            }
            "application/json" -> {
                recordHappiness(mapper.readValue(req.inputStream))
                resp.status = HttpServletResponse.SC_NO_CONTENT
            }
            else -> resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE)
        }
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/plain"
        resp.writer.use { writer ->
            eventCategoryReader.readCategoryForwards("happiness")
                    .map { resolvedEvent ->
                        HappinessObj(resolvedEvent.eventRecord().streamId().id(),
                                String(resolvedEvent.eventRecord().data()))
                    }
                    .forEach { (email, happiness) ->
                        writer.println("$email $happiness")
                    }
        }
    }

    private fun String.toMimeType(): String {
        val sepOffset = indexOf(';')
        if (sepOffset < 0)
            return toLowerCase()
        else
            return substring(0, sepOffset).toLowerCase()
    }

    private fun recordHappiness(happinessObj: HappinessObj) {
        eventStreamWriter.write(streamId("happiness", happinessObj.email),
                listOf(newEvent("HappinessReceived", happinessObj.happiness.toByteArray())))
    }
}
