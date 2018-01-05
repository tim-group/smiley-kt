package com.timgroup.smileykt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javax.servlet.ServletInputStream

private val mapper = jacksonObjectMapper()

fun decode(inputStream: ServletInputStream): RecordHappinessServlet.Happiness {
    return mapper.readValue(inputStream)
}

fun Emotion.toByteArray() = this.toString().toByteArray()