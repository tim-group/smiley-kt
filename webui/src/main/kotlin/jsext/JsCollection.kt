package jsext

external interface JsCollection<K, V, T> : JsIterable<T> {
    val size: Int
    fun has(key: K): Boolean
    fun delete(key: K): JsCollection<K, V, T>
    fun clear(): JsCollection<K, V, T>
    fun keys(): JsIterator<K>
    fun values(): JsIterator<V>
    fun entries(): JsIterator<Tuple2<K, V>>
}

@JsName("Set")
external class JsSet<E>(fromIterable: JsIterable<E> = definedExternally) : JsCollection<E, E, E> {
    override val size: Int
    override fun has(key: E): Boolean
    fun add(value: E)
    override fun delete(key: E): JsSet<E>
    override fun clear(): JsSet<E>
    override fun keys(): JsIterator<E>
    override fun values(): JsIterator<E>
    override fun entries(): JsIterator<Tuple2<E, E>>
}

fun <T> jsSetOf(vararg elements: T): JsSet<T> = JsSet(elements.unsafeCast<JsIterable<T>>())

@JsName("Map")
external class JsMap<K, V>(fromPairs: JsIterable<Tuple2<K, V>> = definedExternally) : JsCollection<K, V, Tuple2<K, V>> {
    override val size: Int
    override fun has(key: K): Boolean
    operator fun get(key: K): V?
    operator fun set(key: K, value: V): Unit
    override fun delete(key: K): JsMap<K, V>
    override fun clear(): JsMap<K, V>
    override fun keys(): JsIterator<K>
    override fun values(): JsIterator<V>
    override fun entries(): JsIterator<Tuple2<K, V>>
}

fun <K, V> jsMapOf(vararg pairs: Pair<K, V>): JsMap<K, V> = JsMap(pairs.map { it.asJsTuple2() }.toTypedArray().asJsIterable())

external interface Tuple2<out A, out B>

inline operator fun <A, B> Tuple2<A, B>.component1(): A = asDynamic()[0].unsafeCast<A>()
inline operator fun <A, B> Tuple2<A, B>.component2(): B = asDynamic()[1].unsafeCast<B>()

fun <A, B> Pair<A, B>.asJsTuple2(): Tuple2<A, B> = arrayOf(first, second).unsafeCast<Tuple2<A, B>>()

private data class ImmutableEntry<out K, out V>(override val key: K, override val value: V) : Map.Entry<K, V>

val <K, V> JsCollection<K, V, *>.keys: Set<K>
    get() {
        return object : AbstractSet<K>() {
            override fun iterator(): Iterator<K> {
                return this@keys.keys().toIterator()
            }

            override val size: Int
                get() = this@keys.size
        }
    }

val <K, V> JsCollection<K, V, *>.values: Collection<V>
    get() {
        return object : AbstractCollection<V>() {
            override fun iterator(): Iterator<V> {
                return this@values.values().toIterator()
            }

            override val size: Int
                get() = this@values.size
        }
    }

val <K, V> JsCollection<K, V, Tuple2<K, V>>.entries: Set<Map.Entry<K, V>>
    get() {
        return object : AbstractSet<Map.Entry<K, V>>() {
            override fun iterator(): Iterator<Map.Entry<K, V>> {
                return this@entries.entries().toIterator {
                    ImmutableEntry(it.component1(), it.component2())
                }
            }

            override val size: Int
                get() = this@entries.size
        }
    }
