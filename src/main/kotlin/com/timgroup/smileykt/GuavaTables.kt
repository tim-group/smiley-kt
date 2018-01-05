package com.timgroup.smileykt

import com.google.common.collect.HashBasedTable
import com.google.common.collect.ImmutableTable
import com.google.common.collect.Table

fun <R: Any, C: Any, V> emptyTable() = ImmutableTable.of<R, C, V>()

fun <R: Any, C: Any, V> mutableTableOf(vararg triple: Triple<R, C, V>): Table<R, C, V> = HashBasedTable.create<R, C, V>().apply {
    triple.forEach {
        put(it.first, it.second, it.third)
    }
}

fun <R: Any, C: Any, V> tableOf(vararg triple: Triple<R, C, V>): ImmutableTable<R, C, V> = ImmutableTable.builder<R, C, V>().apply {
    triple.forEach {
        put(it.first, it.second, it.third)
    }
}.build()

fun <R: Any, C: Any, V> Table<R, C, V>.toTable(): ImmutableTable<R, C, V> = ImmutableTable.copyOf(this)

fun <R: Any, C: Any, V> Table<R, C, V>.toMutableTable(): Table<R, C, V> = HashBasedTable.create(this)

fun <R: Any, C: Any, V> buildTable(builder: Table<R, C, V>.() -> Unit) =
        mutableTableOf<R, C, V>().apply(builder).toTable()
