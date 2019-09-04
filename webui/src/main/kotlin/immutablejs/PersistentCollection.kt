@file:JsModule("immutable")

package immutablejs

import jsext.JsCollection
import jsext.JsIterator
import jsext.Tuple2

@JsName("Map")
external fun <K: Any, V: Any> PersistentMap() : PersistentMap<K, V>

@JsName("Collection")
external interface PersistentCollection<K : Any, V: Any> : JsCollection<K, V, Tuple2<K, V>> {
    override val size: Int
    fun isEmpty(): Boolean

    operator fun get(key: K): V?

    @JsName("toSet")
    fun toPersistentSet(): PersistentSet<V>
    @JsName("toList")
    fun toPersistentList(): PersistentList<V>
    @JsName("toMap")
    fun toPersistentMap(): PersistentMap<K, V>

    override fun has(key: K): Boolean
    fun includes(value: V): Boolean

    override fun keys(): JsIterator<K>
    override fun values(): JsIterator<V>
    override fun entries(): JsIterator<Tuple2<K, V>>
    fun first(): V?
    fun last(): V?

    fun toJS(): dynamic
    fun toJSON(): dynamic
    fun toArray(): Array<dynamic>
    fun toObject(): dynamic
}

@JsName("Map")
external interface PersistentMap<K : Any, V : Any> : PersistentCollection<K, V> {
    fun set(key: K, value: V): PersistentMap<K, V>
    override fun delete(key: K): PersistentMap<K, V>
    fun deleteAll(vararg keys: K): PersistentMap<K, V>
    override fun clear(): PersistentMap<K, V>
    fun update(key: K, notSetValue: V, updater: (V) -> V): PersistentMap<K, V>
    fun update(key: K, updater: (V?) -> V): PersistentMap<K, V>
}

@JsName("Set")
external fun <E : Any> PersistentSet() : PersistentSet<E>

@JsName("Set")
external interface PersistentSet<E : Any> : PersistentCollection<E, E> {
    fun add(key: E): PersistentSet<E>
    override fun delete(key: E): PersistentSet<E>
    override fun clear(): PersistentSet<E>
}

@JsName("List")
external fun <E : Any> PersistentList() : PersistentList<E>

@JsName("List")
external interface PersistentList<E : Any> : PersistentCollection<Int, E>  {
    override fun get(key: Int): E
    fun set(index: Int, value: E): PersistentList<E>
    override fun delete(index: Int): PersistentList<E>
    fun insert(index: Int, value: E): PersistentList<E>
    override fun clear(): PersistentList<E>
    fun push(vararg values: E): PersistentList<E>
    fun pop(): PersistentList<E>
    fun unshift(vararg values: E): PersistentList<E>
    fun shift(): PersistentList<E>
    fun update(index: Int, notSetValue: E, updater: (E) -> E): PersistentList<E>
    fun update(index: Int, updater: (E?) -> E): PersistentList<E>
    @JsName("setSize")
    fun withSize(size: Int): PersistentList<E>
    fun indexOf(value: E): Int
    fun lastIndexOf(value: E): Int
    fun findIndex(predicate: (value: E, index: Int, iter: PersistentList<E>) -> Boolean): Int
    fun findLastIndex(predicate: (value: E, index: Int, iter: PersistentList<E>) -> Boolean): Int
    fun find(predicate: (value: E, index: Int, iter: PersistentList<E>) -> Boolean): E?
    fun findLast(predicate: (value: E, index: Int, iter: PersistentList<E>) -> Boolean): E?
    fun sort(comparator : Comparator<E> = definedExternally): PersistentList<E>
    fun <C> sortBy(comparatorValueMapper: (value: E, index: Int, iter: PersistentList<E>) -> C, comparator : Comparator<C> = definedExternally): PersistentList<E>
}
