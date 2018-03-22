package com.timgroup.smileykt

import com.timgroup.eventstore.api.EventSource
import com.timgroup.eventstore.api.StreamId.streamId
import com.timgroup.smileykt.events.EventCodecs
import com.timgroup.smileykt.events.HappinessReceived
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.ws.rs.BadRequestException
import javax.ws.rs.Consumes
import javax.ws.rs.FormParam
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam

@Path("")
class HappinessResources(eventSource: EventSource) {

    private val eventCategoryReader = eventSource.readCategory()
    private val eventStreamWriter = eventSource.writeStream()

    @Path("happiness")
    @POST
    @Consumes("application/json")
    fun recordHappinessFromJson(happinessObj: Happiness) {
        recordHappiness(HappinessReceived(email = happinessObj.email, date = happinessObj.date, emotion = happinessObj.emotion))
    }

    @Path("happiness")
    @POST
    @Consumes("application/x-www-form-urlencoded")
    fun recordHappinessFromForm(@FormParam("email") email: String?, @FormParam("emotion") emotionString: String?, @FormParam( "date") formDate: String?) {
        if (emotionString == null) throw BadRequestException("'emotion' not specified")
        if (email == null) throw BadRequestException("'email' not specified")
        if (formDate == null) throw BadRequestException("'date' not specified")

        val date = try {
            LocalDate.parse(formDate)
        } catch (e: DateTimeParseException) {
            throw BadRequestException(e)
        }
        val emotion = Emotion.valueOfOrNull(emotionString) ?: throw BadRequestException("Unknown emotion $emotionString")
        recordHappiness(HappinessReceived(email, date, emotion))
    }

    @Path("happiness")
    @GET
    @Produces("text/plain")
    fun getHappiness(): String {
        val stringWriter = StringWriter()
        val writer = PrintWriter(stringWriter)
        eventCategoryReader.readCategoryForwards("happiness").use { stream ->
            val emotions = mutableTableOf<String, LocalDate, Emotion>()
            stream.forEach { resolvedEvent ->
                val event = EventCodecs.deserializeEvent(resolvedEvent.eventRecord())
                when (event) {
                    is HappinessReceived -> emotions.put(event.email, event.date, event.emotion)
                }
            }
            emotions.forEach { email, date, emotion ->
                writer.println("$date $email $emotion")
            }
        }
        return stringWriter.toString()
    }

    @Path("submit_happiness")
    @GET
    fun submitHappiness(@QueryParam("email") email: String?, @QueryParam("emotion") emotionString: String?, @QueryParam( "date") formDate: String?) {
        if (emotionString == null) throw BadRequestException("'emotion' not specified")
        if (email == null) throw BadRequestException("'email' not specified")
        if (formDate == null) throw BadRequestException("'date' not specified")

        val date = try {
            LocalDate.parse(formDate)
        } catch (e: DateTimeParseException) {
            throw BadRequestException(e)
        }
        val emotion = Emotion.valueOfOrNull(emotionString) ?: throw BadRequestException("Unknown emotion $emotionString")
        recordHappiness(HappinessReceived(email, date, emotion))
    }

    private fun recordHappiness(happinessEvent: HappinessReceived) {
        eventStreamWriter.write(
                streamId("happiness", happinessEvent.email),
                listOf(EventCodecs.serializeEvent(happinessEvent))
        )
    }

    data class Happiness(val email: String, val emotion: Emotion, val date: LocalDate)
}

