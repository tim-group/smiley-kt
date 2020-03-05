package com.timgroup.smileykt

import com.timgroup.smileykt.common.Emotion
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import java.net.URI
import java.time.LocalDate

class HtmlEmailGenerator(private val serverUri: URI) {
    fun emailFor(email: String, date: LocalDate) = createHTML(true).body {
        p { +"Hello $email" }
        p { +"How are you feeling today?" }
        p {
            Emotion.values().forEach { emotion ->
                a(href = serverUri.resolve("submit_happiness?date=$date&email=$email&emotion=${emotion.name}").toString()) {
                    attributes["title"] = emotion.name.toLowerCase()
                    img(src = "cid:${emotion.name.toLowerCase()}-face", alt=emotion.name.toLowerCase())
                }
            }
        }
    }
}
