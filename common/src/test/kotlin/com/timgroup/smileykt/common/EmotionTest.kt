package com.timgroup.smileykt.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EmotionTest {
    @Test
    fun decodes_emotion_string() {
        assertEquals(Emotion.HAPPY, emotionOf("HAPPY"))
    }

    @Test
    fun decodes_unknown_string_as_null() {
        assertNull(emotionOf("UNKNOWN"))
    }
}
