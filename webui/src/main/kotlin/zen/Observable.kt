package zen

import kotlin.js.Promise

@JsModule("zen-observable")
external class Observable<out T>(subscriber: Subscriber<T>) {
    fun subscribe(observer: Observer<T>): Subscription
    fun subscribe(onNext: (T) -> Unit, onError: (Throwable) -> Unit = definedExternally, onComplete: () -> Unit = definedExternally): Subscription
    fun forEach(consumer: (T) -> Unit): Promise<*>
    fun filter(predicate: (T) -> Boolean): Observable<T>
    fun <U> map(mapper: (T) -> U): Observable<U>
    fun <U> reduce(reducer: (U, T) -> U, initialValue: U = definedExternally): Observable<U>
//    fun concat(vararg others: Observable<@UnsafeVariance T>): Observable<T>

    companion object {
        fun <T> of(vararg value: T): Observable<T>
        fun from(value: Any): Observable<*>
    }
}

//fun <T> concat(first: Observable<T>, vararg others: Observable<T>) = first.concat(*others)

typealias Subscriber<T> = (ObserverRef<T>) -> Subscription

external interface Subscription {
    fun unsubscribe()
}

fun subscription(unsubscribeAction: () -> Unit) = object : Subscription {
    override fun unsubscribe() {
        unsubscribeAction()
    }
}

external interface Observer<in T> {
    fun next(value: T)
    fun error(exception: Throwable)
    fun complete()
}

external interface ObserverRef<in T> : Observer<T> {
    val closed: Boolean
}
