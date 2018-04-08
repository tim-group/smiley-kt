package app

import react.*
import react.dom.*
import logo.*
import ticker.*

class App : RComponent<RProps, RState>() {
    override fun RBuilder.render() {
        h1 { + "User happiness"}
        child(HappinessTable::class){
            attrs.url = "http://office-smileyapp-001.mgmt.lon.net.local:8000/happiness"
        }
    }
}

fun RBuilder.app() = child(App::class) {}