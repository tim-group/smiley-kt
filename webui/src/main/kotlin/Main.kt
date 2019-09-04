import kotlinx.html.div
import kotlinx.html.dom.append
import kotlin.browser.document

fun main() {
    console.log("Kotlin Main function")

    document.body!!.append {
        div {
            attributes["id"] = "app"
            +"this is some text"
        }
    }
}
