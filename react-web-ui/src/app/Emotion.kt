package app

import react.*
import react.dom.*
import kotlinx.html.classes

import org.w3c.fetch.RequestInit
import org.w3c.xhr.XMLHttpRequest

@JsModule("src/app/emotion/happy.png")
external val happy: dynamic
@JsModule("src/app/emotion/neutral.png")
external val neutral: dynamic
@JsModule("src/app/emotion/sad.png")
external val sad: dynamic

interface EmotionProps : RProps {
    var emotion: String
}

class Emotion(props: EmotionProps) : RComponent<EmotionProps, RState>(props) {

    override fun RBuilder.render() {
        img {
            attrs {
                src = props.emotion.toImg()
                height = "20"
            }
        }
    }

    fun String.toImg(): dynamic {
        return when (this) {
            "HAPPY" -> happy
            "NEUTRAL" -> neutral
            "SAD" -> sad
            else -> "unknown" // will this work?
        }
    }
}

fun RBuilder.emotion(emotion: String) = child(Emotion::class) {
    attrs.emotion = emotion
}