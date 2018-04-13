package app

import react.*
import react.dom.*
import kotlinx.html.classes

import org.w3c.fetch.RequestInit
import org.w3c.xhr.XMLHttpRequest

interface HappinessTableProps : RProps {
    var url: String
}

interface HappinessTableState : RState {
    var loaded: Boolean
    var happiness: List<UserHappiness>
}

data class UserHappiness(val date: String, val email: String, val emotion: String)

class HappinessTable(props: HappinessTableProps) : RComponent<HappinessTableProps, HappinessTableState>(props) {
    override fun HappinessTableState.init(props: HappinessTableProps) {
        loaded = false
    }

    override fun RBuilder.render() {
        if (!state.loaded) {
            div { +"loading..." }
        } else {
            table {
                attrs.classes = setOf("table")
                attrs.jsStyle {
                    border = "solid black 1px"
                }
                tbody {
                    state.happiness.forEach { h ->
                        tr {
                            td { +h.email }
                            td { +h.date }
                            emotion(h.emotion)
                        }
                    }
                }
            }
        }
    }

    override fun componentDidMount() {
        load(props.url)
    }

    private fun load(url: String) {
        val xhr = XMLHttpRequest().apply {
            addEventListener("load", {
                val status = status.toInt()
                if (status == 200) {
                    setState {
                        loaded = true;
                        happiness = responseText.trim().split("\n").map { it.parseUserHappiness() }
                    }
                } else {
                    throw RuntimeException("$url: status=$status")
                }
            })
        }
        xhr.open("get", url)
        xhr.send()
    }

    fun String.parseUserHappiness(): UserHappiness {
        val parts = split(" ")
        return UserHappiness(parts[0], parts[1], parts[2])
    }
}
