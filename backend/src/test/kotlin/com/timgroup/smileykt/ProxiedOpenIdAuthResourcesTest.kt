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

class ProxiedOpenIdAuthResourcesTest {
    @get:Rule
    val server = ServerRule()

    @Test
    fun `gets empty data`() {
        server.execute(HttpGet("/oidc_user")).apply {
            assertThat(statusLine.statusCode, equalTo(HttpStatus.SC_OK))
            assertThat(entity, has(HttpEntity::mimeType, equalTo("application/json"))
                    and has(HttpEntity::readText, present(json(jsonObject()
            ))))
        }
    }

    @Test
    fun `reflects gauth claim data`() {
        val request = HttpGet("/oidc_user")
        request.addHeader("X-OIDC-User", "0000000000000000@accounts.google.com")
        request.addHeader("OIDC_CLAIM_hd", "example.com")
        request.addHeader("OIDC_CLAIM_email", "user@example.com")
        request.addHeader("OIDC_CLAIM_email_verified", "1")
        request.addHeader("OIDC_CLAIM_iat", "1550330103")
        request.addHeader("OIDC_CLAIM_exp", "1550339802")
        request.addHeader("OIDC_CLAIM_iss", "https://accounts.google.com")
        request.addHeader("OIDC_CLAIM_name", "Joe Guser")
        request.addHeader("OIDC_CLAIM_given_name", "Joe")
        request.addHeader("OIDC_CLAIM_family_name", "Guser")
        request.addHeader("OIDC_CLAIM_picture", "https://example.net/content.jpg")
        request.addHeader("OIDC_CLAIM_sub", "0000000000000000")
        request.addHeader("OIDC_CLAIM_aud", "324522823945-7d5l1jeu4kafj3spegjadac8u7v9okco.apps.googleusercontent.com")
        server.execute(request).apply {
            assertThat(statusLine.statusCode, equalTo(HttpStatus.SC_OK))
            assertThat(entity, has(HttpEntity::mimeType, equalTo("application/json"))
                    and has(HttpEntity::readText, present(json(jsonObject()
                    .withProperty("userId", "0000000000000000@accounts.google.com")
                    .withProperty("claims", jsonObject()
                            .withProperty("hd", "example.com")
                            .withProperty("email", "user@example.com")
                            .withProperty("email_verified", true)
                            .withProperty("iat", "2019-02-16T15:15:03Z")
                            .withProperty("exp", "2019-02-16T17:56:42Z")
                            .withProperty("iss", "https://accounts.google.com")
                            .withProperty("name", "Joe Guser")
                            .withProperty("picture", "https://example.net/content.jpg")
                            .withProperty("sub", "0000000000000000")
                            .withProperty("family_name", "Guser")
                            .withProperty("given_name", "Joe")
                            .withProperty("aud", "324522823945-7d5l1jeu4kafj3spegjadac8u7v9okco.apps.googleusercontent.com"))
            ))))
        }
    }

    @Test
    fun `reflects onelogin claim data`() {
        val request = HttpGet("/oidc_user")
        request.addHeader("X-OIDC-User", "00000000@openid-connect.onelogin.com/oidc")
        request.addHeader("OIDC_CLAIM_email", "user@example.com")
        request.addHeader("OIDC_CLAIM_name", "Joe Luser")
        request.addHeader("OIDC_CLAIM_iat", "1550330103")
        request.addHeader("OIDC_CLAIM_exp", "1550339802")
        request.addHeader("OIDC_CLAIM_aud", "e65c5fe0-136e-0137-012d-066046afadd073388")
        request.addHeader("OIDC_CLAIM_preferred_username", "user@example.com")
        request.addHeader("OIDC_CLAIM_iss", "https://openid-connect.onelogin.com/oidc")
        request.addHeader("OIDC_CLAIM_sub", "00000000")
        server.execute(request).apply {
            assertThat(statusLine.statusCode, equalTo(HttpStatus.SC_OK))
            assertThat(entity, has(HttpEntity::mimeType, equalTo("application/json"))
                    and has(HttpEntity::readText, present(json(jsonObject()
                    .withProperty("userId", "00000000@openid-connect.onelogin.com/oidc")
                    .withProperty("claims", jsonObject()
                            .withProperty("email", "user@example.com")
                            .withProperty("name", "Joe Luser")
                            .withProperty("iat", "2019-02-16T15:15:03Z")
                            .withProperty("exp", "2019-02-16T17:56:42Z")
                            .withProperty("aud", "e65c5fe0-136e-0137-012d-066046afadd073388")
                            .withProperty("preferred_username", "user@example.com")
                            .withProperty("iss", "https://openid-connect.onelogin.com/oidc")
                            .withProperty("sub", "00000000"))
            ))))
        }
    }
}