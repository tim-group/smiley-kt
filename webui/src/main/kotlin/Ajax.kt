import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest
import kotlin.coroutines.experimental.suspendCoroutine

suspend fun postJSON(url: String, data: Any): Int {
    return suspendCoroutine { continuation ->
        val xhr = XMLHttpRequest()
        xhr.onreadystatechange = { _: Event ->
            if (xhr.readyState == XMLHttpRequest.DONE) {
                val status = xhr.status.toInt()
                if (status in 200 until 300) {
                    continuation.resume(status)
                }
                else {
                    continuation.resumeWithException(RuntimeException("Failed to post to $url: ${xhr.status} ${xhr.statusText}"))
                }
            }
        }
        xhr.open("POST", url)
        xhr.setRequestHeader("Content-Type", "application/json")
        xhr.send(JSON.stringify(data))
    }
}
