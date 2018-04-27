package com.timgroup.smileykt

enum class Emotion {
    HAPPY, NEUTRAL, SAD;
    companion object {
        fun valueOfOrNull(str: String): Emotion? {
            return try {
                Emotion.valueOf(str)
            } catch (e: Exception) {
                null
            }
        }
    }
}