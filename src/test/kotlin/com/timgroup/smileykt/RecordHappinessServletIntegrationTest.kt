package com.timgroup.smileykt

import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.message.BasicNameValuePair
import org.junit.Rule
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class RecordHappinessServletIntegrationTest {
    @get:Rule
    val server = ServerRule()

    @Test
    fun `records happiness`() {
        val request = HttpPost("/record-happiness")

        val postParameters = ArrayList<NameValuePair>()
        postParameters.add(BasicNameValuePair("email", "test@example.com"))
        postParameters.add(BasicNameValuePair("happiness", "very happy"))
        request.setEntity(UrlEncodedFormEntity(postParameters, "UTF-8"))

        server.execute(request)

        assertEquals(200, server.response.statusLine.statusCode)
    }
}