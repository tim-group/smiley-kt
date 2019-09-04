package jsext

external interface Dictionary<V : Any>

inline operator fun <V : Any> Dictionary<V>.get(name: String) = this.asDynamic()[name].unsafeCast<V?>()

fun <V : Any> Dictionary<V>.asMap(): Map<String, V> = JsDictionaryMap(this)

private class JsDictionaryMap<V : Any>(@JsName("underlyingDictionary") private val dictionary: Dictionary<V>) : AbstractMap<String, V>() {
    override val keys
        get() = object : AbstractSet<String>() {
            override val size: Int
                get() = keysArray.size

            override fun iterator() = keysArray.iterator()
        }

    override val values: Collection<V>
        get() = object : AbstractCollection<V>() {
            override val size: Int
                get() = valuesArray.size

            override fun iterator() = valuesArray.iterator()
        }

    override val entries
        get() = object : AbstractSet<Map.Entry<String, V>>() {
            override val size: Int
                get() = keysArray.size

            override fun iterator() = object : Iterator<Map.Entry<String, V>> {
                private val underlying = keysArray.iterator()

                override fun hasNext(): Boolean {
                    return underlying.hasNext()
                }

                override fun next(): Map.Entry<String, V> {
                    val key = underlying.next()
                    return MapEntry(key, dictionary[key]!!)
                }
            }
        }

    override fun get(key: String) = dictionary[key]

    override val size: Int
        get() = keysArray.size

    private val keysArray: Array<String>
        get() = js("Object.keys(this.underlyingDictionary)").unsafeCast<Array<String>>()
    private val valuesArray: Array<V>
        get() = js("Object.values(this.underlyingDictionary)").unsafeCast<Array<V>>()
}

private data class MapEntry<V : Any>(override val key: String, override val value: V) : Map.Entry<String,  V> {
    override fun toString(): String = "$key=$value"
}
