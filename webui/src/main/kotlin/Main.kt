import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.launch
import org.w3c.dom.Document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import kotlin.browser.document

fun main(args: Array<String>) {
    document.getElementById("submit")!!.onClick {
        val data = HappinessData(document.inputElementValue("email"), document.inputElementValue("emotion"))
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

data class HappinessData(val email: String, val emotion: String)
