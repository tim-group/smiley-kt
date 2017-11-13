import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document

fun main(args: Array<String>) {
    document.getElementById("submit")!!.addEventListener("click", { e: Event ->
        e.preventDefault()
        val email = (document.getElementById("email") as HTMLInputElement).value
        val happiness = (document.getElementById("happiness") as HTMLInputElement).value
        val data = HappinessData(email, happiness)
        console.log("submit form", data)
        val xhr = XMLHttpRequest()
        xhr.open("POST", "/happiness")
        xhr.setRequestHeader("Content-Type", "application/json")
        xhr.send(JSON.stringify(data))
    })
}

data class HappinessData(val email: String, val happiness: String)
