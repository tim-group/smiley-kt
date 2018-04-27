package com.timgroup.smileykt

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import java.time.ZoneId
import java.time.ZoneOffset

class UserDefinitionTest {
    @Test
    fun `parses email addresses separated by whitespace or commas`() {
        assertThat(parseUserDefinitions("user@example.com, other@example.com grizzly@example.com"),
                equalTo(setOf(
                        UserDefinition("user@example.com", ZoneOffset.UTC),
                        UserDefinition("other@example.com", ZoneOffset.UTC),
                        UserDefinition("grizzly@example.com", ZoneOffset.UTC)
                )))
    }

    @Test
    fun `parses email addresses interspersed with timezone specifiers`() {
        assertThat(parseUserDefinitions("[Europe/London] user@example.com other@example.com [Antarctica/Troll] grizzly@example.com"),
                equalTo(setOf(
                        UserDefinition("user@example.com", ZoneId.of("Europe/London")),
                        UserDefinition("other@example.com", ZoneId.of("Europe/London")),
                        UserDefinition("grizzly@example.com", ZoneId.of("Antarctica/Troll"))
                )))
    }
}
