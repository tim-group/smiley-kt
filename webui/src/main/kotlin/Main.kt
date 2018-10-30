import com.timgroup.smileykt.common.Emotion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.dom.append
import kotlinx.html.option
import org.w3c.dom.Document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import kotlin.browser.document
import kotlin.js.Date

fun main(args: Array<String>) {
    document.getElementById("emotion")?.append {
        Emotion.values().forEach { emotion ->
            option {
                attributes["value"] = emotion.name
                +emotion.name.toTitleCase()
            }
        }
    }
    document.getElementById("submit")?.onClick {
        val data = HappinessData(
                date = today().toString(),
                email = document.inputElementValue("email"),
                emotion = document.inputElementValue("emotion")
        )
        try {
            postJSON("/happiness", data)
            for (id in listOf("email", "emotion")) {
                document.resetInputElement(id)
            }
        } catch (e: RuntimeException) {
            console.error("failed to post JSON", e)
        }
    }
}

fun String.toTitleCase(): String {
    val l = toLowerCase()
    return l.substring(0, 1).toUpperCase() + l.substring(1)
}

data class LocalDate(val year: Int, val month: Int, val day: Int) {
    override fun toString() = "${year.pad(4)}-${month.pad(2)}-${day.pad(2)}"
}

fun Date.toLocalDate() = LocalDate(
        year = getFullYear(),
        month = getMonth() + 1,
        day = getDate()
)

fun today() = Date().toLocalDate()

private fun EventTarget.onClick(block: suspend CoroutineScope.() -> Unit) {
    addEventListener("click", { e: Event ->
        e.preventDefault()
        GlobalScope.launch(block = block)
    })
}

private fun Document.inputElementValue(id: String): String {
    val element = getElementById(id) ?: throw IllegalArgumentException("No such element: $id")
    return when (element) {
        is HTMLInputElement -> element.value
        is HTMLSelectElement -> element.value
        else -> throw IllegalArgumentException("Unhandled input element: $id: $element")
    }
}

private fun Document.resetInputElement(id: String) {
    val element = getElementById(id) ?: throw IllegalArgumentException("No such element: $id")
    when (element) {
        is HTMLInputElement -> element.value = ""
        is HTMLSelectElement -> element.value = ""
    }
}

fun Int.pad(width: Int): String {
    return buildString {
        val number = this@pad.toString()
        for (n in 0 until (width - number.length)) {
            append('0')
        }
        append(number)
    }
}

data class HappinessData(val date: String, val email: String, val emotion: String)
