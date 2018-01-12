import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.launch
import org.w3c.dom.Document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import kotlin.browser.document

fun main(args: Array<String>) {
    document.getElementById("submit")!!.onClick {
        postJSON("/happiness",
                HappinessData(document.inputElementValue("email"), document.inputElementValue("emotion")))
        for (id in listOf("email", "emotion")) {
            (document.getElementById(id) as HTMLInputElement).value = ""
        }
    }
}

private fun EventTarget.onClick(block: suspend CoroutineScope.() -> Unit) {
    addEventListener("click", { e: Event ->
        e.preventDefault()
        launch(Unconfined, block = block)
    })
}

private fun Document.inputElementValue(id: String): String = ((getElementById(id) ?: throw IllegalArgumentException("No such element: $id")) as HTMLInputElement).value

data class HappinessData(val email: String, val emotion: String)
