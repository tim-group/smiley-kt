import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.launch
import org.w3c.dom.Document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import kotlin.browser.document

fun main(args: Array<String>) {
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

external interface JsDate {
    fun getYear(): Int
    fun getMonth(): Int
    fun getDate(): Int
}

data class LocalDate(val year: Int, val month: Int, val day: Int) {
    override fun toString() = "${year.pad(4)}-${month.pad(2)}-${day.pad(2)}"
}

fun JsDate.toLocalDate() = LocalDate(
        year = getYear() + 1900,
        month = getMonth() + 1,
        day = getDate()
)

fun today() = js("new Date()").unsafeCast<JsDate>().toLocalDate()

private fun EventTarget.onClick(block: suspend CoroutineScope.() -> Unit) {
    addEventListener("click", { e: Event ->
        e.preventDefault()
        launch(block = block)
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
