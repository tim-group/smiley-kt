package com.timgroup.smileykt

import com.natpryce.hamkrest.anything
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
        val response = server.execute(HttpGet("/info/health"))
        assertThat(response.statusLine.statusCode, equalTo(200))
        assertThat(response.entity, has(HttpEntity::mimeType, present(equalTo("text/plain"))))
        assertThat(response.entity, has(HttpEntity::readText, present(equalTo("healthy"))))
    }

    @Test
    fun `shows application stoppable`() {
        val response = server.execute(HttpGet("/info/stoppable"))
        assertThat(response.statusLine.statusCode, equalTo(200))
        assertThat(response.entity, has(HttpEntity::mimeType, present(equalTo("text/plain"))))
        assertThat(response.entity, has(HttpEntity::readText, present(equalTo("safe"))))
    }

    @Test
    fun `shows application version`() {
        val response = server.execute(HttpGet("/info/version"))
        assertThat(response.statusLine.statusCode, equalTo(200))
        assertThat(response.entity, has(HttpEntity::mimeType, present(equalTo("text/plain"))))
        assertThat(response.entity, has(HttpEntity::readText, present(anything)))
    }

    @Test
    fun `shows application status`() {
        val response = server.execute(HttpGet("/info/status.json"))
        assertThat(response.statusLine.statusCode, equalTo(200))
        assertThat(response.entity, has(HttpEntity::mimeType, present(equalTo("application/json"))))
        assertThat(response.entity, has(HttpEntity::readText, present(json(jsonObject()
                .withProperty("id", "smiley-kt")
                .withProperty("status", "ok")
                .withProperty("health", "healthy")
                .withProperty("components", jsonArray().of(
                        jsonObject().withProperty("id", "version").withProperty("status", "info").withAnyOtherProperties())
                )
                .withAnyOtherProperties()
        ))))
    }
}
