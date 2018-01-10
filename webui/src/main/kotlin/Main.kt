import kotlinx.coroutines.experimental.launch
import org.w3c.dom.Document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import kotlin.browser.document

fun main(args: Array<String>) {
    console.log("FIRE IT UP")

    document.getElementById("submit")!!.addEventListener("click", { e: Event ->
        e.preventDefault()

        val data = HappinessData(document.inputElementValue("email"), document.inputElementValue("emotion"))
        console.log("submit form", data)
        launch {
            val status = postJSON("/happiness", data)
            console.log("posted input; status=$status")
            for (id in listOf("email", "emotion")) {
                (document.getElementById(id) as HTMLInputElement).value = ""
            }
        }
    })
}

private fun Document.inputElementValue(id: String): String = ((getElementById(id) ?: throw IllegalArgumentException("No such element: $id")) as HTMLInputElement).value

data class HappinessData(val email: String, val emotion: String)
