package com.timgroup.smileykt

import com.timgroup.eventstore.api.EventSource
import com.timgroup.eventstore.api.StreamId.streamId
import com.timgroup.smileykt.events.EventCodecs
import com.timgroup.smileykt.events.HappinessReceived
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.ws.rs.BadRequestException
import javax.ws.rs.Consumes
import javax.ws.rs.FormParam
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces

@Path("/happiness")
class RecordHappinessResources(eventSource: EventSource, private val clock: Clock) {

    private val eventCategoryReader = eventSource.readCategory()
    private val eventStreamWriter = eventSource.writeStream()

    @POST
    @Consumes("application/json")
    fun recordHappinessFromJson(happinessObj: Happiness) {
        recordHappiness(happinessObj)
    }

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
        recordHappiness(Happiness(email, emotion, date))
    }

    @GET
    @Produces("text/plain")
    fun getHappiness(): String {
        val stringWriter = StringWriter()
        val writer = PrintWriter(stringWriter)
        eventCategoryReader.readCategoryForwards("happiness").use { stream ->
            val emotions = mutableTableOf<String, LocalDate, Emotion>().apply {
                stream.forEach { resolvedEvent ->
                    val (email, date, emotion) = EventCodecs.deserializeEvent(resolvedEvent.eventRecord())
                    put(email, date, emotion)
                }
            }
            emotions.forEach { email, date, emotion ->
                writer.println("$date $email $emotion")
            }
        }
        return stringWriter.toString()
    }

    private fun recordHappiness(happinessObj: Happiness) {
        eventStreamWriter.write(
                streamId("happiness", happinessObj.email),
                listOf(EventCodecs.serializeEvent(HappinessReceived(happinessObj.email, happinessObj.date, happinessObj.emotion)))
        )
    }

    data class Happiness(val email: String, val emotion: Emotion, val date: LocalDate)
}

