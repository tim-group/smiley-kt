package com.timgroup.smileykt.common

actual fun randomNumber() = js("Math.random()").unsafeCast<Double>()
