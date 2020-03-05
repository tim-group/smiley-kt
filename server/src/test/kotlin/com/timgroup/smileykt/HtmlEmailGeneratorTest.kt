package com.timgroup.smileykt

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import org.junit.Test
import java.net.URI
import java.time.LocalDate


class HtmlEmailGeneratorTest {

    val generator = HtmlEmailGenerator(URI("https://happiness.example.com/"))

    @Test
    fun `generates html to request bob submits happiness for Monday`() {
        val emailHtml = generator.emailFor("bob@gmail.com", LocalDate.of(2018, 1, 11))
        assertThat(emailHtml, containsSubstring("bob@gmail.com"))
        assertThat(emailHtml, containsSubstring("2018-01-11"))
        assertThat(emailHtml, containsSubstring("https://happiness.example.com/submit_happiness?date=2018-01-11&amp;email=bob@gmail.com&amp;emotion=HAPPY"))
        assertThat(emailHtml, containsSubstring("https://happiness.example.com/submit_happiness?date=2018-01-11&amp;email=bob@gmail.com&amp;emotion=NEUTRAL"))
        assertThat(emailHtml, containsSubstring("https://happiness.example.com/submit_happiness?date=2018-01-11&amp;email=bob@gmail.com&amp;emotion=SAD"))
        assertThat(emailHtml, containsSubstring("cid:happy-face"))
        assertThat(emailHtml, containsSubstring("cid:neutral-face"))
        assertThat(emailHtml, containsSubstring("cid:sad-face"))
    }

}
