package com.timgroup.smileykt

import java.net.URI
import java.time.LocalDate

class HtmlEmailGenerator(val serverUri: URI) {
    fun emailFor(email: String, date: LocalDate): String {
        return """
            <p>Hello $email</p>

            <ul>
              <li><a href="${serverUri.resolve("submit_happiness?date=$date&email=$email&emotion=HAPPY")}" title="happy"><img src="happy.gif" alt="happy"/></a></li>
              <li><a href="${serverUri.resolve("submit_happiness?date=$date&email=$email&emotion=NEUTRAL")}" title="neutral"><img src="neutral.gif" alt="neutral"/></a></li>
              <li><a href="${serverUri.resolve("submit_happiness?date=$date&email=$email&emotion=SAD")}" title="sad"><img src="sad.gif" alt="sad"/></a></li>
            </ul>
        """.trimIndent()
    }

}