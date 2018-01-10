import org.w3c.dom.Document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document

fun main(args: Array<String>) {
    console.log("FIRE IT UP")

    document.getElementById("submit")!!.addEventListener("click", { e: Event ->
        e.preventDefault()

        val data = HappinessData(document.inputElementValue("email"), document.inputElementValue("happiness"))
        console.log("submit form", data)
        val xhr = XMLHttpRequest()
        xhr.open("POST", "/happiness")
        xhr.setRequestHeader("Content-Type", "application/json")
        xhr.send(JSON.stringify(data))
    })
}

private fun Document.inputElementValue(id: String): String = ((getElementById(id) ?: throw IllegalArgumentException("No such element: $id")) as HTMLInputElement).value

data class HappinessData(val email: String, val happiness: String)
