package jsext

external interface JsIterable<out T>

external interface JsIterator<out T> {
    fun next(): JsIteratorValue<T>
}

external interface JsIteratorValue<out T> {
    val done: Boolean
    val value: T
}

inline fun <T> Array<T>.asJsIterable() = unsafeCast<JsIterable<T>>()

inline fun <T> JsIterable<T>.jsIterator(): JsIterator<T> =
        asDynamic()[js("Symbol.iterator")].unsafeCast<() -> JsIterator<T>>()()

inline fun <T> JsIterable<T>.forEach(consumer: (T) -> Unit) {
    val iterator = jsIterator()
    var value = iterator.next()
    while (!value.done) {
        consumer(value.value)
        value = iterator.next()
    }
}

fun <T> JsIterator<T>.toIterator(): Iterator<T> {
    return object : AbstractIterator<T>() {
        override fun computeNext() {
            val iteratorValue = this@toIterator.next()
            if (iteratorValue.done)
                done()
            else
                setNext(iteratorValue.value)
        }
    }
}

fun <T> JsIterable<T>.toIterable(): Iterable<T> {
    return object : Iterable<T> {
        override fun iterator() = jsIterator().toIterator()
    }
}

fun <T> JsIterable<T>.toSequence(): Sequence<T> {
    return object : Sequence<T> {
        override fun iterator() = jsIterator().toIterator()
    }
}

fun <T, U> JsIterator<T>.toIterator(mapper: (T) -> U): Iterator<U> {
    return object : AbstractIterator<U>() {
        override fun computeNext() {
            val iteratorValue = this@toIterator.next()
            if (iteratorValue.done)
                done()
            else
                setNext(mapper(iteratorValue.value))
        }
    }
}
