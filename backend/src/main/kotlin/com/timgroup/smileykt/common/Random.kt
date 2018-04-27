package com.timgroup.smileykt.common

import java.security.SecureRandom

private val generator = SecureRandom()

actual fun randomNumber() = generator.nextDouble()
