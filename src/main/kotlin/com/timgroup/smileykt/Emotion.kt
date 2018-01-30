package com.timgroup.smileykt

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