import zen.Observable

fun main() {
    console.log("Kotlin Main function")
    val obs = Observable.of(1, 2, 3)
    console.log("obs=$obs")
    obs.subscribe(
            { console.log("got $it") },
            { console.warn("failed", it) },
            { console.log("done") }
    )
}
