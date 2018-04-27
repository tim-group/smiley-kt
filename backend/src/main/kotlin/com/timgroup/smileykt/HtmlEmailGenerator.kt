package com.timgroup.smileykt

import com.timgroup.smileykt.common.Emotion
import java.net.URI
import java.time.LocalDate

class HtmlEmailGenerator(private val serverUri: URI) {
    fun emailFor(email: String, date: LocalDate): String {
        return buildString {
            append("""
                <body>
                <p>Hello $email</p>

                <p>How are you feeling today?</p>

                <p>
            """.trimIndent())

            Emotion.values().map { emotion -> emotion.name }.forEach { emotion ->
                append("""
                    <a href="${serverUri.resolve("submit_happiness?date=$date&email=$email&emotion=$emotion")}" title="${emotion.toLowerCase()}"><img src="cid:${emotion.toLowerCase()}-face" alt="${emotion.toLowerCase()}"/></a>
                """.trimIndent())
            }

            append("""
                </p>
                </body>
            """.trimIndent())
        }
    }
}
