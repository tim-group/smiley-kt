package immutablejs

import jsext.entries
import jsext.keys
import jsext.toIterator
import jsext.values

fun <K : Any, V : Any> PersistentMap<K, V>.toMap(): Map<K, V> {
    return object : AbstractMap<K, V>() {
        override fun get(key: K) = this@toMap[key]

        override fun containsKey(key: K) = this@toMap.has(key)

        override fun containsValue(value: V) = this@toMap.includes(value)

        override val keys: Set<K>
            get() = this@toMap.keys

        override val values: Collection<V>
            get() = this@toMap.values

        override val entries: Set<Map.Entry<K, V>>
            get() = this@toMap.entries

        override val size: Int
            get() = this@toMap.size

        override fun isEmpty() = this@toMap.isEmpty()
    }
}

fun <K : Any, V : Any> PersistentCollection<K, V>.toCollection(): Collection<V> {
    return object : AbstractCollection<V>() {
        override fun iterator(): Iterator<V> = this@toCollection.values().toIterator()

        override val size: Int
            get() = this@toCollection.size
    }
}

fun <E : Any> PersistentSet<E>.toSet(): Set<E> {
    return object : AbstractSet<E>() {
        override fun iterator(): Iterator<E> = this@toSet.values().toIterator()

        override val size: Int
            get() = this@toSet.size
    }
}

fun <E : Any> PersistentSet<E>.toList() = toSet().toList()

fun <E : Any> PersistentList<E>.toList(): List<E> {
    return object : AbstractList<E>() {
        override fun get(index: Int): E = this@toList[index]

        override val size: Int
            get() = this@toList.size
    }
}

fun <E : Any> PersistentList<E>.toSet() = toPersistentSet().toSet()
