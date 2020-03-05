package com.timgroup.smileykt.webui

import kotlinx.coroutines.coroutineScope
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.p
import kotlin.browser.document

suspend fun main() = coroutineScope<Unit> {
    val div = document.create.div {
        id = "kotlinMain"
    }

    document.body!!.appendChild(div)

    div.append {
        p {
            +"Content from Kotlin, $coroutineContext"
        }
    }
}
