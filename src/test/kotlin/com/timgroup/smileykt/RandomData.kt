package com.timgroup.smileykt

import java.util.*

fun randomInteger() = random.nextInt()

fun randomString(length: Int = 10): String = buildString {
    (0 until length).forEach {
        val n = Math.abs(randomInteger()) % randomStringAlphabet.length
        append(randomStringAlphabet[n])
    }
}

fun randomize(prefix: String) = "$prefix-${randomString()}"

fun randomAddress(): String {
    val localPart = randomString()
    val subdomain = randomString()
    val suffixes = arrayOf("net", "com", "org")
    val suffix = suffixes[Math.abs(randomInteger()) % suffixes.size]
    return "$localPart@$subdomain.example.$suffix"
}

private val random = Random()
private const val randomStringAlphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
