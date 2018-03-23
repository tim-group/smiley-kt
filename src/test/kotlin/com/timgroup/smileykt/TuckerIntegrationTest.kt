package com.timgroup.smileykt

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.present
import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpGet
import org.araqnid.hamkrest.json.json
import org.araqnid.hamkrest.json.jsonArray
import org.araqnid.hamkrest.json.jsonObject
import org.junit.Rule
import org.junit.Test

class TuckerIntegrationTest {
    @get:Rule
    val server = ServerRule()

    @Test
    fun `shows application health`() {
        server.execute(HttpGet("/info/health")).apply {
            assertThat(statusLine.statusCode, equalTo(200))
            assertThat(entity, has(HttpEntity::mimeType, equalTo("text/plain")))
            assertThat(entity, has(HttpEntity::readText, equalTo("healthy")))
        }
    }

    @Test
    fun `shows application stoppable`() {
        server.execute(HttpGet("/info/stoppable")).apply {
            assertThat(statusLine.statusCode, equalTo(200))
            assertThat(entity, has(HttpEntity::mimeType, equalTo("text/plain")))
            assertThat(entity, has(HttpEntity::readText, equalTo("safe")))
        }
    }

    @Test
    fun `shows application version`() {
        server.execute(HttpGet("/info/version")).apply {
            assertThat(statusLine.statusCode, equalTo(200))
            assertThat(entity, has(HttpEntity::mimeType, equalTo("text/plain")))
            assertThat(entity, has(HttpEntity::readText, present()))
        }
    }

    @Test
    fun `shows application status`() {
        server.execute(HttpGet("/info/status.json")).apply {
            assertThat(statusLine.statusCode, equalTo(200))
            assertThat(entity, has(HttpEntity::mimeType, equalTo("application/json"))
                                and has(HttpEntity::readText, present(json(jsonObject()
                    .withProperty("id", "smiley-kt")
                    .withProperty("status", "ok")
                    .withProperty("health", "healthy")
                    .withProperty("components", jsonArray().including(
                            jsonObject().withProperty("id", "version").withProperty("status", "info").withAnyOtherProperties())
                    )
                    .withAnyOtherProperties()
            ))))
        }
    }
}
