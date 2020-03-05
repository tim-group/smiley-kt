package com.timgroup.smileykt.common

enum class Emotion {
    HAPPY, NEUTRAL, SAD;
}

fun emotionOf(str: String): Emotion? {
    return try {
        Emotion.valueOf(str)
    } catch (e: Exception) {
        null
    }
}
