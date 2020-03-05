@file:Suppress("unused")

package com.timgroup.smileykt

import com.google.common.collect.HashBasedTable
import com.google.common.collect.ImmutableTable
import com.google.common.collect.Table

fun <R: Any, C: Any, V> emptyTable(): ImmutableTable<R, C, V> = ImmutableTable.of<R, C, V>()

fun <R: Any, C: Any, V> mutableTableOf(): Table<R, C, V> = HashBasedTable.create<R, C, V>()

fun <R: Any, C: Any, V> mutableTableOf(vararg triple: Triple<R, C, V>): Table<R, C, V> = HashBasedTable.create<R, C, V>().apply {
    triple.forEach {
        put(it.first, it.second, it.third)
    }
}

fun <R: Any, C: Any, V> tableOf(): ImmutableTable<R, C, V> = emptyTable()

fun <R: Any, C: Any, V> tableOf(vararg triple: Triple<R, C, V>): ImmutableTable<R, C, V> = ImmutableTable.builder<R, C, V>().apply {
    triple.forEach {
        put(it.first, it.second, it.third)
    }
}.build()

fun <R: Any, C: Any, V> Table<R, C, V>.toTable(): ImmutableTable<R, C, V> = ImmutableTable.copyOf(this)

fun <R: Any, C: Any, V> Table<R, C, V>.toMutableTable(): Table<R, C, V> = HashBasedTable.create(this)

operator fun <R: Any, C: Any, V> Table.Cell<R, C, V>.component1() = rowKey as R
operator fun <R: Any, C: Any, V> Table.Cell<R, C, V>.component2() = columnKey as C
@Suppress("UNCHECKED_CAST")
operator fun <R: Any, C: Any, V> Table.Cell<R, C, V>.component3() = value as V

inline fun <R: Any, C: Any, V> Table<R, C, V>.forEach(action: (R, C, V) -> Unit) {
    cellSet().forEach { (row, column, value) ->
        action(row, column, value)
    }
}
