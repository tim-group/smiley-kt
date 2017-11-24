package com.timgroup.smileykt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javax.servlet.ServletInputStream
import com.fasterxml.jackson.module.kotlin.readValue

private val mapper = jacksonObjectMapper()

fun decode(inputStream: ServletInputStream): Happiness {
    // TODO - don't understand how this extension function is working...
    return mapper.readValue(inputStream)
}