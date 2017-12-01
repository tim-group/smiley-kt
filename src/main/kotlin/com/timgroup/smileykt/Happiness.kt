package com.timgroup.smileykt

data class Happiness(val email: String, val emotion: Emotion)

enum class Emotion {
    ECSTATIC, HAPPY, INDIFFERENT, SAD, SUICIDAL;
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