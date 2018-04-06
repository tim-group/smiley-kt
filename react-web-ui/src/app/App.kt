package app

import react.*
import react.dom.*
import logo.*
import ticker.*

class App : RComponent<RProps, RState>() {
    override fun RBuilder.render() {
        table {
            thead {
                tr {
                    th { +"User" }
                    th { +"Date" }
                    th { +"Happiness" }
                }
            }
            tbody {
                happiness.forEach { h ->
                    tr {
                        td { + h.email }
                        td { + h.date }
                        td { + h.emotion }
                    }
                }
            }
        }
    }
}

fun RBuilder.app() = child(App::class) {}

data class UserHappiness(val email: String, val date: String, val emotion: String)

val happiness = listOf(
        UserHappiness("xyz@example.com", "2018-04-01", "HAPPY"),
        UserHappiness("abc@example.com", "2018-04-01", "SAD")
)