import org.w3c.dom.events.Event
import org.w3c.xhr.XMLHttpRequest
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun postJSON(url: String, data: Any): Int {
    return suspendCoroutine { continuation ->
        val xhr = XMLHttpRequest().apply {
            onreadystatechange = { _: Event ->
                if (readyState == XMLHttpRequest.DONE) {
                    val statusCode = status.toInt()
                    if (statusCode in 200 until 300) {
                        continuation.resume(statusCode)
                    }
                    else {
                        continuation.resumeWithException(RuntimeException("Failed to post to $url: $status $statusText"))
                    }
                }
            }
        }
        xhr.open("POST", url)
        xhr.setRequestHeader("Content-Type", "application/json")
        xhr.send(JSON.stringify(data))
    }
}
