package com.timgroup.smileykt

import com.google.common.io.MoreFiles
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.charset.StandardCharsets.UTF_8
import java.time.ZoneOffset

class AutoReloadingUserDefinitionsTest {
    @get:Rule val temporaryFolder = TemporaryFolder()

    @Test fun `automatically reloads properties file on change`() {
        val propertiesPath = temporaryFolder.newFile("config.properties").toPath()

        MoreFiles.asCharSink(propertiesPath, UTF_8).write("""
            users=some.user@example.com
        """.trimIndent())

        val userDefinitions = AutoReloadingUserDefinitions(propertiesPath)

        assertThat(userDefinitions, equalTo(setOf(UserDefinition("some.user@example.com", ZoneOffset.UTC))))

        Thread.sleep(200) // avoid problems with the test running so fast the on-disk mtime doesn't change

        MoreFiles.asCharSink(propertiesPath, UTF_8).write("""
            users=some.user@example.com some.other.user@example.com
        """.trimIndent())

        assertThat(userDefinitions, equalTo(setOf(
                UserDefinition("some.user@example.com", ZoneOffset.UTC),
                UserDefinition("some.other.user@example.com", ZoneOffset.UTC)
        )))
    }
}
