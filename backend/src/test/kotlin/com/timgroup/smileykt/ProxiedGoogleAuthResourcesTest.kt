package com.timgroup.smileykt

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.present
import org.apache.http.HttpEntity
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import org.araqnid.hamkrest.json.json
import org.araqnid.hamkrest.json.jsonObject
import org.junit.Rule
import org.junit.Test

class ProxiedGoogleAuthResourcesTest {
    @get:Rule
    val server = ServerRule()

    @Test
    fun `gets empty data`() {
        server.execute(HttpGet("/gauth_user")).apply {
            assertThat(statusLine.statusCode, equalTo(HttpStatus.SC_OK))
            assertThat(entity, has(HttpEntity::mimeType, equalTo("application/json"))
                    and has(HttpEntity::readText, present(json(jsonObject()
            ))))
        }
    }

    @Test
    fun `reflects claim data`() {
        val request = HttpGet("/gauth_user")
        request.addHeader("OIDC_CLAIM_hd", "timgroup.com")
        server.execute(request).apply {
            assertThat(statusLine.statusCode, equalTo(HttpStatus.SC_OK))
            assertThat(entity, has(HttpEntity::mimeType, equalTo("application/json"))
                    and has(HttpEntity::readText, present(json(jsonObject()
                    .withProperty("hd", "timgroup.com")
            ))))
        }
    }
}