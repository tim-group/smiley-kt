import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document
import kotlin.js.json

fun main(args: Array<String>) {
    document.getElementById("submit")!!.addEventListener("click", { e: Event ->
        e.preventDefault()
        val email = (document.getElementById("email") as HTMLInputElement).value
        val happiness = (document.getElementById("happiness") as HTMLInputElement).value
        val doc = JSON.stringify(json("email" to email, "happiness" to happiness))
        console.log("submit form", doc);
        val xhr = XMLHttpRequest()
        xhr.open("POST", "/happiness")
        xhr.setRequestHeader("Content-Type", "application/json")
        xhr.send(doc)
    })
}
