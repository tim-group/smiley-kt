package com.timgroup.smileykt

import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.util.EntityUtils
import org.junit.Test
import kotlin.test.assertEquals
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.HttpEntity
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.BasicNameValuePair
import java.util.ArrayList

class RecordHappinessServletIntegrationTest : IntegrationTest() {
    @Test
    fun `records happiness`() {
        val request = HttpPost("/record-happiness")

        val postParameters = ArrayList<NameValuePair>()
        postParameters.add(BasicNameValuePair("email", "test@example.com"))
        postParameters.add(BasicNameValuePair("happiness", "very happy"))
        request.setEntity(UrlEncodedFormEntity(postParameters, "UTF-8"))

        execute(request)

        assertEquals(200, response.statusLine.statusCode)
    }
}